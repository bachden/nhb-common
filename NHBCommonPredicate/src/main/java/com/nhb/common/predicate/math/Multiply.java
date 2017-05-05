package com.nhb.common.predicate.math;

import java.math.BigDecimal;

import com.nhb.common.predicate.value.NumberValue;
import com.nhb.common.predicate.value.primitive.RawNumberValue;

public class Multiply extends TwoMemberOperator {

	public Multiply() {
		super();
	}

	public Multiply(NumberValue value1, NumberValue value2) {
		super(value1, value2);
	}

	public Multiply(Number value1, Number value2) {
		this(new RawNumberValue(value1), new RawNumberValue(value2));
	}

	@Override
	public Number get() {
		BigDecimal number1 = new BigDecimal(this.getValue1().get().toString());
		BigDecimal number2 = new BigDecimal(this.getValue2().get().toString());
		return number1.multiply(number2);
	}

	@Override
	public String getSymbol() {
		return "*";
	}
}
