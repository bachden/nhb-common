package com.nhb.common.predicate.math;

import com.nhb.common.predicate.value.NumberValue;
import com.nhb.common.predicate.value.primitive.RawNumberValue;

public class DivideToInteger extends TwoMemberOperator {

	public DivideToInteger() {
		super();
	}

	public DivideToInteger(NumberValue value1, NumberValue value2) {
		super(value1, value2);
	}

	public DivideToInteger(Number value1, Number value2) {
		this(new RawNumberValue(value1), new RawNumberValue(value2));
	}

	@Override
	public Number get() {
		long number1 = this.getValue1().get().longValue();
		long number2 = this.getValue2().get().longValue();
		return (long) number1 / number2;
	}

	public static void main(String[] args) {
		DivideToInteger op = new DivideToInteger(new RawNumberValue(123.34f), new RawNumberValue(1.234f));
		System.out.println(op.get());
	}

	@Override
	public String getSymbol() {
		return "/";
	}
}
