package com.nhb.common.predicate.math;

import com.nhb.common.predicate.value.NumberValue;
import com.nhb.common.predicate.value.primitive.RawNumberValue;

public class Sqrt extends SingleMemberOperator {

	public Sqrt() {
		super();
	}

	public Sqrt(NumberValue value) {
		super(value);
	}

	public Sqrt(Number value) {
		this(new RawNumberValue(value));
	}

	@Override
	public Number get() {
		return Math.sqrt(this.getValue().get().doubleValue());
	}

	@Override
	public String getSymbol() {
		return "sqrt";
	}
}
