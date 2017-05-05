package com.nhb.common.predicate.math;

import com.nhb.common.predicate.object.AttributeGetter;
import com.nhb.common.predicate.object.ObjectDependence;
import com.nhb.common.predicate.object.ObjectDependenceValue;
import com.nhb.common.predicate.value.NumberValue;
import com.nhb.common.predicate.value.primitive.RawNumberValue;

import lombok.Getter;
import lombok.Setter;

public abstract class TwoMemberOperator extends ObjectDependenceValue<Number> implements NumberValue, MathOperator {

	@Setter
	@Getter
	private NumberValue value1;

	@Setter
	@Getter
	private NumberValue value2;

	protected TwoMemberOperator() {
		// do nothing
	}

	protected TwoMemberOperator(NumberValue value1, NumberValue value2) {
		this.setValue1(value1);
		this.setValue2(value2);
	}

	@Override
	public void fill(Object object) {
		super.fill(object);
		if (this.value1 instanceof ObjectDependence) {
			((ObjectDependence) this.value1).fill(object);
		}
		if (this.value2 instanceof ObjectDependence) {
			((ObjectDependence) this.value2).fill(object);
		}
	}

	protected TwoMemberOperator(Number value1, Number value2) {
		this(new RawNumberValue(value1), new RawNumberValue(value2));
	}

	@Override
	public String toString() {
		return ((value1 instanceof RawNumberValue || value1 instanceof AttributeGetter) ? "" : "(") //
				+ this.value1.toString() //
				+ ((value1 instanceof RawNumberValue || value1 instanceof AttributeGetter) ? "" : ")")//
				+ " " //
				+ ((value2 instanceof RawNumberValue || value2 instanceof AttributeGetter) ? "" : "(") //
				+ this.getSymbol() //
				+ " " //
				+ this.value2.toString() //
				+ ((value2 instanceof RawNumberValue || value2 instanceof AttributeGetter) ? "" : ")");
	}
}
