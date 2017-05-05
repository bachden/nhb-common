package com.nhb.common.predicate.numeric;

import com.nhb.common.predicate.object.ObjectDependence;
import com.nhb.common.predicate.value.NumberValue;

public class Equals extends NumericComparisonPredicate {

	private static final long serialVersionUID = 562211748204617410L;

	private NumberValue value;

	public Equals(NumberValue value, NumberValue anchorValue) {
		super(anchorValue);
		this.value = value;
	}

	@Override
	public boolean apply(Object object) {
		if (this.getValue() instanceof ObjectDependence) {
			((ObjectDependence) this.getValue()).fill(object);
		}
		return getComparator().compare(this.getValue().get(), this.getAnchorValue().get()) == 0;
	}

	protected NumberValue getValue() {
		return value;
	}

	@Override
	public String toString() {
		return this.value.toString() + " = " + this.getAnchorValue().toString();
	}
}
