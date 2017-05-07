package com.nhb.common.predicate.object.value;

import com.nhb.common.predicate.Predicate;
import com.nhb.common.predicate.object.ObjectDependence;
import com.nhb.common.predicate.value.BooleanValue;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public class PredicateWrapperBooleanValue extends AbstractObjectDependenceValue<Boolean> implements BooleanValue {

	@Setter
	@Getter(AccessLevel.PROTECTED)
	private Predicate predicate;

	public PredicateWrapperBooleanValue() {
		// do nothing
	}

	public PredicateWrapperBooleanValue(Predicate predicate) {
		this.predicate = predicate;
	}

	@Override
	protected void fill() {
		if (this.predicate instanceof ObjectDependence) {
			((ObjectDependence) this.predicate).fill(getObject());
		}
	}

	@Override
	public Boolean get() {
		return this.predicate.apply(getObject());
	}

	@Override
	public String toString() {
		return this.predicate.toString();
	}
}
