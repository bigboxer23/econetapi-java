package com.bigboxer23.eco_net.data;

import java.util.List;
import lombok.Data;

/** */
@Data
public class EnergyDataResults {
	private List<ValueHolder<Float>> data;
	private List<ValueHolder<Float>> historyData;
	private String lastUpdated;
	private String message;
	private String totalUsageMessageInfo;
}
