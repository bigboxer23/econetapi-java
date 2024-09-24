package com.bigboxer23.eco_net;

import com.bigboxer23.eco_net.data.EcoNetLoginData;
import com.bigboxer23.utils.http.OkHttpUtil;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** */
public class EcoNetAPI {
	private static final Logger logger = LoggerFactory.getLogger(EcoNetAPI.class);
	protected static final String baseUrl = "https://rheem.clearblade.com/api/v/1/";

	private static EcoNetAPI instance;

	private final String userToken;
	private final String accountId;

	private static final String CLEAR_BLADE_SYSTEM_KEY = "e2e699cb0bb0bbb88fc8858cb5a401";
	private static final String CLEAR_BLADE_SYSTEM_SECRET = "E2E699CB0BE6C6FADDB1B0BC9A20";

	private EcoNetAPI(String accountId, String userToken) {
		this.accountId = accountId;
		this.userToken = userToken;
	}

	public static EcoNetAPI getInstance(String email, String password) {
		if (StringUtils.isBlank(email) || StringUtils.isBlank(password)) {
			logger.error("need to define email and password values.");
			throw new RuntimeException("need to define email and password values.");
		}

		return Optional.ofNullable(instance).orElseGet(() -> {
			instance = getAccountIDAndToken(email, password)
					.map(data -> new EcoNetAPI(data.getOptions().getAccountId(), data.getUserToken()))
					.orElse(null);
			return instance;
		});
	}

	private static Optional<EcoNetLoginData> getAccountIDAndToken(String email, String password) {
		try (Response response = OkHttpUtil.postSynchronous(
				baseUrl + "user/auth",
				RequestBody.create(URLDecoder.decode(
								"{\"email\": \"" + email + "\", \"password\": \"" + password + "\"}",
								StandardCharsets.UTF_8.displayName())
						.getBytes(StandardCharsets.UTF_8)),
				(builder) -> {
					builder.addHeader("ClearBlade-SystemKey", CLEAR_BLADE_SYSTEM_KEY)
							.addHeader("ClearBlade-SystemSecret", CLEAR_BLADE_SYSTEM_SECRET)
							.addHeader("Content-Type", "application/json; charset=UTF-8");
					return builder;
				})) {
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
}
