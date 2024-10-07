package com.bigboxer23.eco_net;

import static org.junit.jupiter.api.Assertions.*;

import com.bigboxer23.eco_net.data.UserData;
import com.bigboxer23.utils.properties.PropertyUtils;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Need to define environment variables for econet_email/econet_password to run tests */
public class EcoNetApiTest {

	private static final Logger logger = LoggerFactory.getLogger(EcoNetApiTest.class);

	private static final String email = PropertyUtils.getProperty("econet_email");

	private static final String password = PropertyUtils.getProperty("econet_password");

	private static final EcoNetAPI instance = EcoNetAPI.getInstance(email, password);

	@Test
	public void testAuth() {
		testInvalidLogins(null, null);
		testInvalidLogins("", null);
		testInvalidLogins("", "");
		testInvalidLogins(null, "");
		testInvalidLogins(email, "blah");
		testInvalidLogins("invalid", password);
	}

	private void testInvalidLogins(String email, String password) {
		try {
			EcoNetAPI api = EcoNetAPI.getInstance(email, password);
			if (api != null) {
				fail();
			}
		} catch (RuntimeException e) {

		}
	}

	@Test
	public void getUserData() {
		Optional<UserData> result = EcoNetAPI.getInstance(email, password).fetchUserData();
		assertTrue(result.isPresent());
		assertFalse(result.get().getResults().getLocations().isEmpty());
		assertFalse(
				result.get().getResults().getLocations().get(0).getEquipments().isEmpty());
		assertNotNull(result.get()
				.getResults()
				.getLocations()
				.get(0)
				.getEquipments()
				.get(0)
				.getType());
		assertNotNull(result.get()
				.getResults()
				.getLocations()
				.get(0)
				.getEquipments()
				.get(0)
				.getSetpoint()
				.getValue());
	}

	@Test
	public void subscribeToEvents() throws InterruptedException {
		EcoNetAPI.getInstance(email, password)
				.subscribeToEvents((topic, message) -> logger.info(topic + ": " + message.toString()));
		int count = 0;
		while (count < 7) {
			Thread.sleep(10000);
			logger.info("sleeping " + (count++ * 10));
		}
	}
}
