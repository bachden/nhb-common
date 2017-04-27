package com.nhb.common.predicate.value.primitive;

import com.nhb.common.predicate.value.Value;

public class StringValue implements Value<String> {

	private String value;

	public StringValue(String value) {
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
