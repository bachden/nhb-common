package com.nhb.common.predicate.object.getter;

import com.nhb.common.predicate.value.StringValue;

public class StringAttributeGetter extends AbstractAttributeGetter<String> implements StringValue {

	public StringAttributeGetter() {
		super();
	}

	public StringAttributeGetter(String attribute) {
		super(attribute);
	}

	public StringAttributeGetter(String attribute, Object object) {
		super(attribute, object);
	}
}
