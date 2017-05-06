package com.nhb.common.predicate.numeric;

import com.nhb.common.predicate.value.NumberValue;

public class GreaterThan extends OneWayNumericComparisonPredicate {

	private static final long serialVersionUID = -3217505531609333605L;

	public GreaterThan(NumberValue value, NumberValue lowerBound) {
		super(value, lowerBound);
	}

	@Override
	public Boolean get() {
		return getComparator().compare(this.getValue().get(), this.getAnchorValue().get()) > 0;
	}

	@Override
	public String toString() {
		return this.getValue().toString() + " > " + this.getAnchorValue().toString();
	}
}
