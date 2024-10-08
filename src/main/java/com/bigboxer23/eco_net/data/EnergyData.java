package com.bigboxer23.eco_net.data;

import com.squareup.moshi.Json;
import lombok.Data;

/** */
@Data
public class EnergyData {
	@Json(name = "energy_usage")
	public EnergyDataResults energyUsage;
}
