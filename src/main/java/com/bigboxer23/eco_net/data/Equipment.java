package com.bigboxer23.eco_net.data;

import com.squareup.moshi.Json;
import lombok.Data;

/** */
@Data
public class Equipment {
	@Json(name = "device_name")
	private String name;

	@Json(name = "device_type")
	private String type;

	@Json(name = "mac_address")
	private String macAddress;

	@Json(name = "serial_number")
	private String serialNumber;
}
