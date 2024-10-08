package com.bigboxer23.eco_net.data;

import com.squareup.moshi.Json;
import lombok.Data;

/** */
@Data
public class FetchUsageCommand {

	@Json(name = "ACTION")
	private String action = "waterheaterUsageReportView";

	@Json(name = "device_name")
	private String deviceName;

	@Json(name = "serial_number")
	private String serialNumber;

	@Json(name = "usage_type")
	private String usageType = "energyUsage";

	@Json(name = "graph_data")
	private EcoNetTimeDateJSON graphData;

	public FetchUsageCommand(String deviceId, String serialNumber, int day, int month, int year) {
		setDeviceName(deviceId);
		setSerialNumber(serialNumber);
		setGraphData(new EcoNetTimeDateJSON(day, month, year));
	}
}
