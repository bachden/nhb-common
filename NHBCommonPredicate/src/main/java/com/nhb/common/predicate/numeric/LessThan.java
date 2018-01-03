package com.nhb.common.predicate.numeric;

import com.nhb.common.predicate.value.NumberValue;

public class LessThan extends OneWayNumericComparisonPredicate {

	private static final long serialVersionUID = 6928552597913868348L;

	public LessThan(NumberValue value, NumberValue upperBound) {
		super(value, upperBound);
	}

	@Override
	public Boolean get() {
		return getComparator().compare(this.getValue().get(), this.getAnchorValue().get()) < 0;
	}

	@Override
	public String toString() {
		return this.getValue().toString() + " < " + this.getAnchorValue().toString();
	}
}
