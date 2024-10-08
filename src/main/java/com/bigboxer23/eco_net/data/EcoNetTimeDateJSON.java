package com.bigboxer23.eco_net.data;

import com.squareup.moshi.Json;
import lombok.Data;

/** */
@Data
public class EcoNetTimeDateJSON {
	private String format = "daily";

	private int month;

	@Json(name = "period")
	private int day;

	private int year;

	public EcoNetTimeDateJSON(int day, int month, int year) {
		setDay(day);
		setMonth(month);
		setYear(year);
	}
}
