package com.nhb.common.predicate.numeric;

import com.nhb.common.predicate.value.NumberValue;

public class NotBetween extends TwoWayNumbericComparisionPredicate {

	private static final long serialVersionUID = -4975458195604574385L;

	public NotBetween(NumberValue value, NumberValue lowerBound, NumberValue upperBound) {
		super(value, lowerBound, upperBound);
	}

	@Override
	public Boolean get() {
		return getComparator().compare(this.getValue().get(), this.getLowerBound().get()) <= 0
				|| getComparator().compare(this.getValue().get(), this.getUpperBound().get()) >= 0;
	}

	@Override
	public String toString() {
		return this.getValue() + " not between " + this.getLowerBound().toString() + " and " + this.getUpperBound();
	}
}
