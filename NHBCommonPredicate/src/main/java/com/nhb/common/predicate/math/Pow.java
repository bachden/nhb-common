package com.nhb.common.predicate.math;

import java.math.BigDecimal;

import com.nhb.common.predicate.value.NumberValue;
import com.nhb.common.predicate.value.primitive.RawNumberValue;

public class Pow extends TwoMemberOperator {

	public Pow() {
		super();
	}

	public Pow(NumberValue value1, NumberValue value2) {
		super(value1, value2);
	}

	public Pow(Number value1, Number value2) {
		this(new RawNumberValue(value1), new RawNumberValue(value2));
	}

	@Override
	public Number get() {
		BigDecimal number1 = new BigDecimal(this.getValue1().get().toString());
		return number1.pow(this.getValue2().get().intValue());
	}

	@Override
	public String getSymbol() {
		return "^";
	}
}
