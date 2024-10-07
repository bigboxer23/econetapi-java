package com.bigboxer23.eco_net.data;

import com.squareup.moshi.Json;
import lombok.Data;

/** */
@Data
public class EcoNetMQTTEvent {
	private String transactionId;

	private String topic;

	private String payload;

	@Json(name = "device_name")
	private String deviceName;

	@Json(name = "serial_number")
	private String serialNumber;

	@Json(name = "@SETPOINT")
	private int setpoint;

	@Json(name = "@SIGNAL")
	private int wifiSignalStrength;

	@Json(name = "@ACTIVE")
	private boolean active;

	@Json(name = "@STATUS")
	private String status;
}
