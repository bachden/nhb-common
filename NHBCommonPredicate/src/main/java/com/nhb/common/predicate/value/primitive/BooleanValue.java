package com.nhb.common.predicate.value.primitive;

import com.nhb.common.predicate.value.Value;

public class BooleanValue implements Value<Boolean> {

	private boolean value;

	@Override
	public Boolean get() {
		return this.value;
	}

	@Override
	public String toString() {
		return String.valueOf(this.value);
	}
}
