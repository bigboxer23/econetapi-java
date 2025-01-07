package com.bigboxer23.eco_net.data;

import org.apache.commons.lang3.StringUtils;

/** */
public class TankStatus {
	private static final String EMPTY = "ic_tank_zero_percent_v2.png.png";
	private static final String ONE_THIRD = "ic_tank_ten_percent_v2.png";
	private static final String TWO_THIRD = "ic_tank_fourty_percent_v2.png";
	private static final String FULL = "ic_tank_hundread_percent_v2.png";

	public static float getTankStatus(String tankStatusUrl) {
		return switch (StringUtils.defaultIfEmpty(tankStatusUrl, "")) {
			case EMPTY -> 0.0f;
			case ONE_THIRD -> .33f;
			case TWO_THIRD -> .66f;
			case FULL -> 1f;
			default -> -1f;
		};
	}
}
