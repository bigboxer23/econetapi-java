package com.bigboxer23.eco_net;

import com.bigboxer23.eco_net.data.*;
import com.bigboxer23.eco_net.mqtt.EcoNetMQTTConnectOptions;
import com.bigboxer23.eco_net.mqtt.IEventSubscriber;
import com.bigboxer23.utils.http.OkHttpRequestBodyUtils;
import com.bigboxer23.utils.http.OkHttpUtil;
import com.bigboxer23.utils.http.RequestBuilderCallback;
import com.bigboxer23.utils.json.JsonMapBuilder;
import java.io.IOException;
import java.util.*;
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
				OkHttpRequestBodyUtils.createBodyFromString(new JsonMapBuilder()
						.put("email", email)
						.put("password", password)
						.toJson()),
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
				OkHttpRequestBodyUtils.createBodyFromString(new JsonMapBuilder()
						.put("location_only", false)
						.put("type", "com.econet.econetconsumerandroid")
						.put("version", "6.0.0-375-01b4870e")
						.toJson()),
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

	public Optional<EnergyResults> fetchEnergyUsage(
			String deviceId, String serialNumber, int day, int month, int year) {
		try (Response response = OkHttpUtil.postSynchronous(
				baseUrl + "/code/" + CLEAR_BLADE_SYSTEM_KEY + "/dynamicAction",
				OkHttpRequestBodyUtils.createBodyFromJsonObject(
						new FetchUsageCommand(deviceId, serialNumber, day, month, year), FetchUsageCommand.class),
				getHeaders(Collections.singletonMap("ClearBlade-UserToken", userToken)))) {
			Optional<EnergyResults> body = OkHttpUtil.getBody(response, EnergyResults.class);
			if (body.isPresent() && !body.get().isSuccess()) {
				logger.error("fetchEnergyUsage: " + body.get().getLogs());
				return Optional.empty();
			}
			return body;
		} catch (IOException e) {
			logger.error("fetchEnergyUsage", e);
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

	/**
	 * Sets the devices mode
	 *
	 * @param deviceId device id (name)
	 * @param serialNumber serial number of device
	 * @param mode See {@link Modes} for possible mode values
	 */
	public void setMode(String deviceId, String serialNumber, int mode) {
		sendCommand(deviceId, serialNumber, "@MODE", mode);
	}

	/**
	 * Set the heater's temperature setpoint
	 *
	 * @param deviceId device id (name)
	 * @param serialNumber serial number of device
	 * @param setpoint setpoint to heat to in deg fahrenheit
	 */
	public void setTemperatureSetPoint(String deviceId, String serialNumber, int setpoint) {
		sendCommand(deviceId, serialNumber, "@SETPOINT", setpoint);
	}

	private void sendCommand(String deviceId, String serialNumber, String command, int value) {
		initMQTTConnection();
		try {
			MqttMessage message = new MqttMessage();
			message.setPayload(new JsonMapBuilder()
					.put("transactionId", "ANDROID_" + System.currentTimeMillis())
					.put("device_name", deviceId)
					.put("serial_number", serialNumber)
					.put(command, value)
					.toJson()
					.getBytes());
			mqttClient.publish("user/" + accountId + "/device/desired", message);
		} catch (MqttException e) {
			logger.error("sendCommand " + command, e);
		}
	}
}
