package com.nhb.common.predicate.value.primitive;

import com.nhb.common.predicate.value.BooleanValue;

import lombok.Setter;

public class RawBooleanValue implements BooleanValue {

	@Setter
	private boolean value;

	public RawBooleanValue() {
	}

	public RawBooleanValue(boolean value) {
		this.value = value;
	}

	@Override
	public Boolean get() {
		return this.value;
	}

	@Override
	public String toString() {
		return String.valueOf(this.value);
	}
}
