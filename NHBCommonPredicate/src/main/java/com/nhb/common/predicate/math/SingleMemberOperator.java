package com.nhb.common.predicate.math;

import com.nhb.common.predicate.object.ObjectDependence;
import com.nhb.common.predicate.object.ObjectDependenceValue;
import com.nhb.common.predicate.value.NumberValue;
import com.nhb.common.predicate.value.primitive.RawNumberValue;

import lombok.AccessLevel;
import lombok.Getter;

public abstract class SingleMemberOperator extends ObjectDependenceValue<Number> implements NumberValue, MathOperator {

	@Getter(AccessLevel.PROTECTED)
	private NumberValue value;

	protected SingleMemberOperator() {

	}

	protected SingleMemberOperator(NumberValue value) {
		this.setValue(value);
	}

	protected SingleMemberOperator(Number value) {
		this.setValue(new RawNumberValue(value));
	}

	public void setValue(NumberValue value) {
		this.value = value;
	}

	public void setValue(Number number) {
		this.value = new RawNumberValue(number);
	}

	@Override
	public void fill(Object object) {
		super.fill(object);
		if (this.value instanceof ObjectDependence) {
			((ObjectDependence) this.value).fill(object);
		}
	}

	@Override
	public String toString() {
		return this.getSymbol() + "(" + this.value.toString() + ")";
	}
}
