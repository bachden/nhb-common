package com.nhb.common.predicate.object.getter;

import com.nhb.common.predicate.value.NumberValue;

public class NumberAttributeGetter extends AbstractAttributeGetter<Number> implements NumberValue {

	public NumberAttributeGetter() {
		super();
	}

	public NumberAttributeGetter(String attribute) {
		super(attribute);
	}

	public NumberAttributeGetter(String attribute, Object object) {
		super(attribute, object);
	}
}
