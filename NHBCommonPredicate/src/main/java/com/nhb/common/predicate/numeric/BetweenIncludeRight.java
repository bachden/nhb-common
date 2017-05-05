package com.nhb.common.predicate.numeric;

import com.nhb.common.predicate.Predicate;
import com.nhb.common.predicate.object.ObjectDependence;
import com.nhb.common.predicate.utils.NumberComparator;
import com.nhb.common.predicate.value.NumberValue;

public class BetweenIncludeRight implements Predicate {

	private static final long serialVersionUID = 4810722470454943505L;

	private NumberComparator comparator = new NumberComparator();
	private NumberValue lowerBound;
	private NumberValue upperBound;
	private NumberValue value;

	public BetweenIncludeRight(NumberValue value, NumberValue lowerBound, NumberValue upperBound) {
		this.value = value;
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
	}

	@Override
	public boolean apply(Object object) {
		if (this.value instanceof ObjectDependence) {
			((ObjectDependence) this.value).fill(object);
		}
		return comparator.compare(this.value.get(), this.lowerBound.get()) > 0
				&& comparator.compare(this.value.get(), this.upperBound.get()) <= 0;
	}

	@Override
	public String toString() {
		return this.value + " between " + this.lowerBound.toString() + " and " + this.upperBound;
	}
}
