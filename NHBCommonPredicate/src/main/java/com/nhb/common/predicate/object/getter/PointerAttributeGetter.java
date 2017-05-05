package com.nhb.common.predicate.object.getter;

import com.nhb.common.predicate.value.PointerValue;

public class PointerAttributeGetter extends AbstractAttributeGetter<Object> implements PointerValue {

	public PointerAttributeGetter() {
		super();
	}

	public PointerAttributeGetter(String attribute) {
		super(attribute);
	}

	public PointerAttributeGetter(String attribute, Object object) {
		super(attribute, object);
	}
}
