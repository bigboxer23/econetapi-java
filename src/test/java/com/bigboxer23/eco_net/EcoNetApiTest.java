package com.bigboxer23.eco_net;

import static org.junit.jupiter.api.Assertions.*;

import com.bigboxer23.eco_net.data.UserData;
import com.bigboxer23.utils.properties.PropertyUtils;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/** Need to define environment variables for econet_email/econet_password to run tests */
public class EcoNetApiTest {
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
}
