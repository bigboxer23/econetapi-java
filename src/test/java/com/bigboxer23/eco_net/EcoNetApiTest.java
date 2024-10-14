package com.bigboxer23.eco_net;

import static org.junit.jupiter.api.Assertions.*;

import com.bigboxer23.eco_net.data.EnergyResults;
import com.bigboxer23.eco_net.data.Equipment;
import com.bigboxer23.eco_net.data.Location;
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
				.subscribeToEvents((message) -> logger.info(message.getTopic() + " " + message.getPayload()));
		int count = 0;
		while (count < 7) {
			Thread.sleep(10000);
			logger.info("sleeping " + (count++ * 10));
		}
	}

	@Test
	public void fetchEnergyUsage() {
		EcoNetAPI api = EcoNetAPI.getInstance(email, password);
		api.fetchUserData().ifPresent(userData -> {
			Location location = userData.getResults().getLocations().get(0);
			Equipment equipment = location.getEquipments().get(0);
			Optional<EnergyResults> data =
					api.fetchEnergyUsage(equipment.getDeviceName(), equipment.getSerialNumber(), 8, 10, 2024);
			assertTrue(data.isPresent());
			assertNotNull(data.get().getResults());
		});
	}

	@Test
	public void setTemperatureSetPoint() {
		EcoNetAPI api = EcoNetAPI.getInstance(email, password);
		api.fetchUserData().ifPresent(userData -> {
			Location location = userData.getResults().getLocations().get(0);
			Equipment equipment = location.getEquipments().get(0);
			int currentSetpoint = equipment.getSetpoint().getValue();
			assertNotEquals(132, currentSetpoint);
			api.setTemperatureSetPoint(equipment.getDeviceName(), equipment.getSerialNumber(), 132);
			try {
				Thread.sleep(5000);
			} catch (InterruptedException theE) {
			}
			api.fetchUserData().ifPresent(updatedUserData -> {
				assertEquals(
						132,
						updatedUserData
								.getResults()
								.getLocations()
								.get(0)
								.getEquipments()
								.get(0)
								.getSetpoint()
								.getValue());
			});
			api.setTemperatureSetPoint(equipment.getDeviceName(), equipment.getSerialNumber(), currentSetpoint);
		});
	}
}
