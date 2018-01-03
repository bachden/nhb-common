package com.nhb.common.predicate.numeric;

import com.nhb.common.predicate.value.NumberValue;

public class GreaterOrEquals extends GreaterThan {

	private static final long serialVersionUID = -715457138661262264L;

	public GreaterOrEquals(NumberValue value, NumberValue lowerBound) {
		super(value, lowerBound);
	}

	@Override
	public Boolean get() {
		return getComparator().compare(this.getValue().get(), this.getAnchorValue().get()) >= 0;
	}

	@Override
	public String toString() {
		return this.getValue().toString() + " >= " + this.getAnchorValue().toString();
	}
}
