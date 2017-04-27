package com.nhb.common.predicate.numeric;

import com.nhb.common.predicate.Predicate;
import com.nhb.common.predicate.utils.NumberComparator;
import com.nhb.common.predicate.value.ObjectDependence;
import com.nhb.common.predicate.value.Value;

public class NotBetweenIncludeBoth implements Predicate {

	private static final long serialVersionUID = 7119288510614891978L;

	private NumberComparator comparator = new NumberComparator();
	private Value<? extends Number> lowerBound;
	private Value<? extends Number> upperBound;
	private Value<? extends Number> value;

	public NotBetweenIncludeBoth(Value<? extends Number> value, Value<? extends Number> lowerBound,
			Value<? extends Number> upperBound) {
		this.value = value;
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
	}

	@Override
	public boolean apply(Object object) {
		if (this.value instanceof ObjectDependence) {
			((ObjectDependence) this.value).fill(object);
		}
		return comparator.compare(this.value.get(), this.lowerBound.get()) < 0
				|| comparator.compare(this.value.get(), this.upperBound.get()) > 0;
	}

	@Override
	public String toString() {
		return this.value + " not between " + this.lowerBound.toString() + " and " + this.upperBound;
	}
}
