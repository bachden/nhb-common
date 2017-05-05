package com.nhb.common.predicate.value.primitive;

import com.nhb.common.predicate.value.StringValue;

import lombok.Setter;

public class RawStringValue implements StringValue {

	@Setter
	private String value;

	public RawStringValue(String value) {
		this.value = value;
	}

	@Override
	public String get() {
		return this.value;
	}

	@Override
	public String toString() {
		return "'" + this.value.replaceAll("\\'", "\\\\'") + "'";
	}
}
