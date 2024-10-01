package com.bigboxer23.eco_net.data;

import com.squareup.moshi.Json;
import lombok.Data;

/** */
@Data
public class Equipment {
	@Json(name = "device_name")
	private String deviceName;

	@Json(name = "device_type")
	private String type;

	@Json(name = "mac_address")
	private String macAddress;

	@Json(name = "serial_number")
	private String serialNumber;

	@Json(name = "@SETPOINT")
	private ValueHolder<Integer> setpoint;

	@Json(name = "@NAME")
	private ValueHolder<String> name;

	@Json(name = "@MODE")
	private Modes modes;
}
