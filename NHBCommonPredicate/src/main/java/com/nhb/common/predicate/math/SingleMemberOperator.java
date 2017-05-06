package com.nhb.common.predicate.math;

import com.nhb.common.predicate.object.ObjectDependence;
import com.nhb.common.predicate.value.NumberValue;
import com.nhb.common.predicate.value.primitive.RawNumberValue;

import lombok.AccessLevel;
import lombok.Getter;

public abstract class SingleMemberOperator extends MathOperator implements NumberValue {

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
	protected void fill() {
		if (this.value instanceof ObjectDependence) {
			((ObjectDependence) this.value).fill(getObject());
		}
	}

	@Override
	public String toString() {
		return this.getSymbol() + "(" + this.value.toString() + ")";
	}
}
