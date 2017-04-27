package com.nhb.common.predicate.value.primitive;

import com.nhb.common.predicate.value.Value;

public class NumberValue implements Value<Number> {

	private Number value;

	public NumberValue(Number value) {
		this.value = value;
	}

	@Override
	public Number get() {
		return this.value;
	}

	@Override
	public String toString() {
		return this.value.toString();
	}
}
