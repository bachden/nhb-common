package com.nhb.common.predicate.math;

import com.nhb.common.predicate.value.NumberValue;
import com.nhb.common.predicate.value.primitive.RawNumberValue;

public class Mod extends TwoMemberOperator {

	public Mod() {
		super();
	}

	public Mod(NumberValue value1, NumberValue value2) {
		super(value1, value2);
	}

	public Mod(Number value1, Number value2) {
		this(new RawNumberValue(value1), new RawNumberValue(value2));
	}

	@Override
	public Number get() {
		long number1 = this.getValue1().get().longValue();
		long number2 = this.getValue2().get().longValue();
		return number1 % number2;
	}

	public static void main(String[] args) {
		Mod op = new Mod(new RawNumberValue(5), new RawNumberValue(2));
		System.out.println(op.get());
	}

	@Override
	public String getSymbol() {
		return "%";
	}
}