package com.nhb.common.predicate.math;

import com.nhb.common.predicate.object.AttributeGetter;
import com.nhb.common.predicate.object.ObjectDependence;
import com.nhb.common.predicate.value.NumberValue;
import com.nhb.common.predicate.value.primitive.RawNumberValue;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public abstract class TwoMemberOperator extends MathOperator implements NumberValue {

	private NumberValue value1;
	private NumberValue value2;

	protected TwoMemberOperator() {
		// do nothing
	}

	protected TwoMemberOperator(NumberValue value1, NumberValue value2) {
		this.setValue1(value1);
		this.setValue2(value2);
	}

	@Override
	protected void fill() {
		if (this.value1 instanceof ObjectDependence) {
			((ObjectDependence) this.value1).fill(getObject());
		}
		if (this.value2 instanceof ObjectDependence) {
			((ObjectDependence) this.value2).fill(getObject());
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
				+ this.getSymbol() //
				+ " " //
				+ ((value2 instanceof RawNumberValue || value2 instanceof AttributeGetter) ? "" : "(") //
				+ this.value2.toString() //
				+ ((value2 instanceof RawNumberValue || value2 instanceof AttributeGetter) ? "" : ")");
	}
}
