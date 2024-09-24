package com.bigboxer23.eco_net.data;

import com.squareup.moshi.Json;
import lombok.Data;

/** */
@Data
public class EcoNetLoginOptionsData {
	@Json(name = "account_id")
	private String accountId;

	private boolean success;

	private String message;
}
