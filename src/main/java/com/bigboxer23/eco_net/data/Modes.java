package com.bigboxer23.eco_net.data;

import com.squareup.moshi.Json;
import lombok.Data;

/** */
@Data
public class Modes {
	private Constraints constraints;

	@Json(name = "status")
	private String activeMode;
}
