package com.bigboxer23.eco_net.data;

import lombok.Data;

/** */
@Data
public class ValueHolder<T> {
	private String name;
	private T value;
}
