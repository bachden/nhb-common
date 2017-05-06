package com.nhb.common.predicate.object.getter;

import com.nhb.common.predicate.value.BooleanValue;

public class BooleanAttributeGetterValue extends AbstractAttributeGetterValue<Boolean> implements BooleanValue {

	public BooleanAttributeGetterValue() {
		super();
	}

	public BooleanAttributeGetterValue(String attribute) {
		super(attribute);
	}

	public BooleanAttributeGetterValue(String attribute, Object object) {
		super(attribute, object);
	}
}
