package com.bigboxer23.eco_net.data;

import com.squareup.moshi.Json;
import lombok.Data;

/** */
@Data
public class EcoNetLoginData {
	@Json(name = "user_token")
	private String userToken;

	private EcoNetLoginOptionsData options;
}
