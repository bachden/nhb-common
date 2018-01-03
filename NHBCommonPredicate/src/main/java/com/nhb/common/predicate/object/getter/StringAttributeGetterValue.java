package com.nhb.common.predicate.object.getter;

import com.nhb.common.predicate.value.StringValue;

public class StringAttributeGetterValue extends AbstractAttributeGetterValue<String> implements StringValue {

	public StringAttributeGetterValue() {
		super();
	}

	public StringAttributeGetterValue(String attribute) {
		super(attribute);
	}
}
