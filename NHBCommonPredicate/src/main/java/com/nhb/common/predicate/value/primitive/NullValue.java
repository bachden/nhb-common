package com.nhb.common.predicate.value.primitive;

import com.nhb.common.predicate.value.Value;

public class NullValue implements Value<Object> {

	@Override
	public Object get() {
		return null;
	}

	@Override
	public String toString() {
		return "null";
	}
}
