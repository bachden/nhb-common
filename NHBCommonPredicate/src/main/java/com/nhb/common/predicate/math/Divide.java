package com.nhb.common.predicate.math;

import com.nhb.common.predicate.value.NumberValue;
import com.nhb.common.predicate.value.primitive.RawNumberValue;

public class Divide extends TwoMemberOperator {

	public Divide() {
		super();
	}

	public Divide(NumberValue value1, NumberValue value2) {
		super(value1, value2);
	}

	public Divide(Number value1, Number value2) {
		this(new RawNumberValue(value1), new RawNumberValue(value2));
	}

	@Override
	public Number get() {
		double number1 = this.getValue1().get().doubleValue();
		double number2 = this.getValue2().get().doubleValue();
		return number1 / number2;
	}

	@Override
	public String getSymbol() {
		return "-";
	}

}
