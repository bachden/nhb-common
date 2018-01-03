package com.nhb.common.predicate.object.getter;

import com.nhb.common.predicate.value.BooleanValue;

public class BooleanAttributeGetterValue extends AbstractAttributeGetterValue<Boolean> implements BooleanValue {

	public BooleanAttributeGetterValue() {
		super();
	}

	public BooleanAttributeGetterValue(String attribute) {
		super(attribute);
	}
}
