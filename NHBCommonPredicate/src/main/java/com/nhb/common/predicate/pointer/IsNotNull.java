package com.nhb.common.predicate.pointer;

import com.nhb.common.predicate.object.ObjectDependence;
import com.nhb.common.predicate.object.ObjectDependencePredicate;
import com.nhb.common.predicate.object.getter.PointerAttributeGetterValue;

import lombok.AccessLevel;
import lombok.Getter;

@Getter(AccessLevel.PROTECTED)
public class IsNotNull extends ObjectDependencePredicate {

	private static final long serialVersionUID = 7293242036653166493L;
	private PointerAttributeGetterValue value;

	public IsNotNull() {

	}

	public IsNotNull(PointerAttributeGetterValue value) {
		if (value == null) {
			throw new NullPointerException("Value cannot be null");
		}
		this.value = value;
	}

	@Override
	protected void fill() {
		if (value == null) {
			throw new NullPointerException("Value cannot be null");
		}
		if (this.value instanceof ObjectDependence) {
			this.value.fill(getObject());
		}
	}

	@Override
	public Boolean get() {
		if (value == null) {
			throw new NullPointerException("Value cannot be null");
		}
		return this.value.get() != null;
	}

	@Override
	public String toString() {
		return this.value.toString() + " is not null";
	}
}
