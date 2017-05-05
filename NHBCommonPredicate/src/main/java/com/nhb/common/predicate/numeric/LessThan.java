package com.nhb.common.predicate.numeric;

import com.nhb.common.predicate.object.ObjectDependence;
import com.nhb.common.predicate.value.NumberValue;

public class LessThan extends NumericComparisonPredicate {

	private static final long serialVersionUID = 6928552597913868348L;

	private NumberValue value;

	public LessThan(NumberValue value, NumberValue upperBound) {
		super(upperBound);
		this.value = value;
	}

	@Override
	public boolean apply(Object object) {
		if (this.value instanceof ObjectDependence) {
			((ObjectDependence) this.value).fill(object);
		}
		return getComparator().compare(this.value.get(), this.getAnchorValue().get()) < 0;
	}

	protected NumberValue getValue() {
		return this.value;
	}

	@Override
	public String toString() {
		return this.value.toString() + " <= " + this.getAnchorValue().toString();
	}
}
