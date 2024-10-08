package com.bigboxer23.eco_net.data;

import lombok.Data;

/** */
@Data
public class EnergyResults {
	private EnergyData results;

	private boolean success;

	private String logs;

	private String stack;
}
