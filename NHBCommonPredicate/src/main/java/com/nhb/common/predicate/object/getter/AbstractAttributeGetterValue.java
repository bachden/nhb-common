package com.nhb.common.predicate.object.getter;

import com.nhb.common.predicate.object.AttributeGetter;
import com.nhb.common.predicate.object.ObjectDependenceValue;
import com.nhb.common.utils.ObjectUtils;

import lombok.Getter;
import lombok.Setter;

public class AbstractAttributeGetterValue<Type> extends ObjectDependenceValue<Type> implements AttributeGetter {

	@Setter
	@Getter
	private String attribute;

	public AbstractAttributeGetterValue() {
		// do nothing
	}

	public AbstractAttributeGetterValue(String attribute) {
		this();
		this.setAttribute(attribute);
	}

	@Override
	protected void fill() {

	}

	@Override
	public Type get() {
		return ObjectUtils.getValueByPath(this.getObject(), this.getAttribute());
	}

	@Override
	public String toString() {
		return this.attribute;
	}
}
