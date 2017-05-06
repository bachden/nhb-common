package com.nhb.common.predicate.numeric;

import com.nhb.common.predicate.value.NumberValue;

public class NotEquals extends OneWayNumericComparisonPredicate {

	private static final long serialVersionUID = 9116064549816891158L;

	public NotEquals(NumberValue value, NumberValue anchorValue) {
		super(value, anchorValue);
	}

	@Override
	public Boolean get() {
		return getComparator().compare(this.getValue().get(), this.getAnchorValue().get()) != 0;
	}

	@Override
	public String toString() {
		return this.getValue().toString() + " != " + this.getAnchorValue().toString();
	}
}
