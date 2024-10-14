package com.bigboxer23.eco_net.data;

import com.squareup.moshi.Json;
import lombok.Data;

/** */
@Data
public class Modes {
	public static final int HEAT_PUMP_MODE = 0;
	public static final int VACATION_MODE = 1;

	private Constraints constraints;

	@Json(name = "status")
	private String activeMode;

	@Json(name = "value")
	private int activeValue;
}
