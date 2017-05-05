package com.nhb.common.predicate.value.getter;

import java.util.Map;

import com.nhb.common.predicate.value.AttributeGetter;
import com.nhb.common.predicate.value.Value;
import com.nhb.common.utils.ObjectUtils;

public class AbstractAttributeGetter<Type> implements AttributeGetter, Value<Type> {

	private String attribute;
	private Object object;
	private boolean allowMapDetect = true;

	public AbstractAttributeGetter() {
		// do nothing
	}

	public AbstractAttributeGetter(String attribute) {
		this();
		this.setAttribute(attribute);
	}

	public AbstractAttributeGetter(String attribute, Object object) {
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

	public boolean isAllowMapDetect() {
		return allowMapDetect;
	}

	public void setAllowMapDetect(boolean allowMapDetect) {
		this.allowMapDetect = allowMapDetect;
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Type get() {
		if (this.getObject() instanceof Map && this.isAllowMapDetect()) {
			return (Type) ((Map) this.getObject()).get(this.getAttribute());
		}
		return ObjectUtils.getValueByPath(this.getObject(), this.getAttribute());
	}

	@Override
	public String toString() {
		return this.attribute;
	}
}
