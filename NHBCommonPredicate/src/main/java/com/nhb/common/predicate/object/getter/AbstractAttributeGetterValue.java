package com.nhb.common.predicate.object.getter;

import com.nhb.common.predicate.object.AttributeGetter;
import com.nhb.common.predicate.value.Value;
import com.nhb.common.utils.ObjectUtils;

public class AbstractAttributeGetterValue<Type> implements AttributeGetter, Value<Type> {

	private String attribute;
	private Object object;

	public AbstractAttributeGetterValue() {
		// do nothing
	}

	public AbstractAttributeGetterValue(String attribute) {
		this();
		this.setAttribute(attribute);
	}

	public AbstractAttributeGetterValue(String attribute, Object object) {
		this(attribute);
		this.fill(object);
	}

	@Override
	public void fill(Object object) {
		this.object = object;
	}

	@Override
	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}

	protected String getAttribute() {
		return this.attribute;
	}

	protected Object getObject() {
		return this.object;
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
