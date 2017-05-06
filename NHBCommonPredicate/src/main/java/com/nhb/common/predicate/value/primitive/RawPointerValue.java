package com.nhb.common.predicate.value.primitive;

import com.nhb.common.predicate.value.PointerValue;

import lombok.Setter;

public class RawPointerValue implements PointerValue {

	@Setter
	private Object value;

	public RawPointerValue(Object value) {
		this.value = value;
	}

	@Override
	public Object get() {
		return this.value;
	}

	@Override
	public String toString() {
		return this.value.toString();
	}
}
