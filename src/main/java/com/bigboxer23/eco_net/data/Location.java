package com.bigboxer23.eco_net.data;

import com.squareup.moshi.Json;
import java.util.List;
import lombok.Data;

/** */
@Data
public class Location {
	@Json(name = "equiptments")
	private List<Equipment> equipments;
}
