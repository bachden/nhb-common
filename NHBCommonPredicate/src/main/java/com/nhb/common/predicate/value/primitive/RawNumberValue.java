package com.nhb.common.predicate.value.primitive;

import com.nhb.common.predicate.value.NumberValue;

public class RawNumberValue implements NumberValue {

	private Number value;

	public RawNumberValue() {

	}

	public RawNumberValue(Number value) {
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
