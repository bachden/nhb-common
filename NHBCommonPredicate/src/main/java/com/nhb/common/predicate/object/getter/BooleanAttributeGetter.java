package com.nhb.common.predicate.object.getter;

import com.nhb.common.predicate.value.BooleanValue;

public class BooleanAttributeGetter extends AbstractAttributeGetter<Boolean> implements BooleanValue {

	public BooleanAttributeGetter() {
		super();
	}

	public BooleanAttributeGetter(String attribute) {
		super(attribute);
	}

	public BooleanAttributeGetter(String attribute, Object object) {
		super(attribute, object);
	}
}
