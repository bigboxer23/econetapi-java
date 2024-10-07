package com.bigboxer23.eco_net;

import com.bigboxer23.eco_net.data.EcoNetLoginData;
import com.bigboxer23.eco_net.data.EcoNetMQTTEvent;
import com.bigboxer23.eco_net.data.UserData;
import com.bigboxer23.eco_net.mqtt.EcoNetMQTTConnectOptions;
import com.bigboxer23.eco_net.mqtt.IEventSubscriber;
import com.bigboxer23.utils.http.OkHttpUtil;
import com.bigboxer23.utils.http.RequestBuilderCallback;
import com.bigboxer23.utils.json.JsonMapBuilder;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** */
public class EcoNetAPI implements IEcoNetConstants {
	private static final Logger logger = LoggerFactory.getLogger(EcoNetAPI.class);

	private static EcoNetAPI instance;

	private static MqttAsyncClient mqttClient;

	private List<IEventSubscriber> subscribers = new ArrayList<>();
	private final String userToken;
	private final String accountId;

	private final String email;

	private EcoNetAPI(String accountId, String userToken, String email) {
		this.accountId = accountId;
		this.userToken = userToken;
		this.email = email;
	}

	public static EcoNetAPI getInstance(String email, String password) {
		if (StringUtils.isBlank(email) || StringUtils.isBlank(password)) {
			logger.error("need to define email and password values.");
			throw new RuntimeException("need to define email and password values.");
		}

		return Optional.ofNullable(instance).orElseGet(() -> {
			instance = getAccountIDAndToken(email, password)
					.map(data -> new EcoNetAPI(data.getOptions().getAccountId(), data.getUserToken(), email))
					.orElse(null);
			return instance;
		});
	}

	private static Optional<EcoNetLoginData> getAccountIDAndToken(String email, String password) {
		try (Response response = OkHttpUtil.postSynchronous(
				baseUrl + "user/auth",
				RequestBody.create(URLDecoder.decode(
								new JsonMapBuilder()
										.put("email", email)
										.put("password", password)
										.toJson(),
								StandardCharsets.UTF_8.displayName())
						.getBytes(StandardCharsets.UTF_8)),
				getHeaders(null))) {
			Optional<EcoNetLoginData> body = OkHttpUtil.getBody(response, EcoNetLoginData.class);
			if (body.isPresent() && !body.get().getOptions().isSuccess()) {
				logger.error("getAccountIDAndToken: " + body.get().getOptions().getMessage());
				return Optional.empty();
			}
			return body;
		} catch (IOException e) {
			logger.error("getAccountIDAndToken", e);
			return Optional.empty();
		}
	}

	public Optional<UserData> fetchUserData() {
		try (Response response = OkHttpUtil.postSynchronous(
				baseUrl + "/code/" + CLEAR_BLADE_SYSTEM_KEY + "/getUserDataForApp",
				RequestBody.create(URLDecoder.decode(
								new JsonMapBuilder()
										.put("location_only", false)
										.put("type", "com.econet.econetconsumerandroid")
										.put("version", "6.0.0-375-01b4870e")
										.toJson(),
								StandardCharsets.UTF_8.displayName())
						.getBytes(StandardCharsets.UTF_8)),
				getHeaders(Collections.singletonMap("ClearBlade-UserToken", userToken)))) {
			Optional<UserData> body = OkHttpUtil.getBody(response, UserData.class);
			if (body.isPresent() && !body.get().isSuccess()) {
				logger.error("fetchUserData: ");
				return Optional.empty();
			}
			return body;
		} catch (IOException e) {
			logger.error("fetchUserData", e);
			return Optional.empty();
		}
	}

	private static RequestBuilderCallback getHeaders(Map<String, String> headers) {
		return builder -> {
			builder.addHeader("ClearBlade-SystemKey", CLEAR_BLADE_SYSTEM_KEY)
					.addHeader("ClearBlade-SystemSecret", CLEAR_BLADE_SYSTEM_SECRET)
					.addHeader("Content-Type", "application/json; charset=UTF-8");
			if (headers != null) {
				headers.forEach(builder::addHeader);
			}
			return builder;
		};
	}

	private String getClientId() {
		return email + System.currentTimeMillis() + "_android";
	}

	private void initMQTTConnection() {
		if (mqttClient == null) {
			try {
				mqttClient = new MqttAsyncClient(MQTT_URL, getClientId(), new MemoryPersistence());
				mqttClient.setCallback(new MqttCallback() {

					@Override
					public void connectionLost(Throwable cause) {
						logger.warn("connectionLost: ", cause);
					}

					@Override
					public void messageArrived(String topic, MqttMessage message) {
						subscribers.forEach(s -> {
							try {
								Optional.ofNullable(OkHttpUtil.getMoshi()
												.adapter(EcoNetMQTTEvent.class)
												.fromJson(new String(message.getPayload())))
										.map(e -> {
											e.setTopic(topic);
											e.setPayload(message.toString());
											return e;
										})
										.ifPresent(e -> {
											try {
												s.messageReceived(e);
											} catch (IOException ioE) {
												logger.error("messageArrived: ", ioE);
											}
										});
							} catch (IOException ioE) {
								logger.error("messageArrived: ", ioE);
							}
						});
					}

					@Override
					public void deliveryComplete(IMqttDeliveryToken token) {
						logger.warn("deliveryComplete: " + token.isComplete());
					}
				});
				logger.info("Connecting to broker: " + MQTT_URL);
				IMqttToken connectToken = mqttClient.connect(new EcoNetMQTTConnectOptions(userToken));
				connectToken.waitForCompletion(TIMEOUT);
				logger.info("Connected");
				mqttClient
						.subscribe("user/" + accountId + "/device/reported", QOS)
						.waitForCompletion(TIMEOUT);
				mqttClient
						.subscribe("user/" + accountId + "/device/desired", QOS)
						.waitForCompletion(TIMEOUT);
				logger.info("Subscribed");

			} catch (MqttException me) {
				logger.error("subscribeToEvents", me);
			}
			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				logger.info("disconnecting mqtt client");
				Optional.ofNullable(mqttClient).ifPresent(client -> {
					try {
						client.disconnect();
					} catch (MqttException e) {
						logger.error("disconnect", e);
					}
				});
				mqttClient = null;
			}));
		}
	}

	public void subscribeToEvents(IEventSubscriber subscriber) {
		initMQTTConnection(); // Init if necessary
		subscribers.add(subscriber);
	}
}
