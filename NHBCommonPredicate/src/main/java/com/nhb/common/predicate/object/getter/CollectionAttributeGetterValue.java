package com.nhb.common.predicate.object.getter;

import java.util.Collection;

import com.nhb.common.predicate.value.CollectionValue;

public class CollectionAttributeGetterValue extends AbstractAttributeGetterValue<Collection<?>>
		implements CollectionValue {

	public CollectionAttributeGetterValue() {
		super();
	}

	public CollectionAttributeGetterValue(String attribute) {
		super(attribute);
	}
}
