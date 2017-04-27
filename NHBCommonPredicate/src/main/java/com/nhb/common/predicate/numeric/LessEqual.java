package com.nhb.common.predicate.numeric;

import com.nhb.common.predicate.value.ObjectDependence;
import com.nhb.common.predicate.value.Value;

public class LessEqual extends LessThan {

	private static final long serialVersionUID = -6691599929614100373L;

	public LessEqual(Value<? extends Number> value, Value<? extends Number> upperBound) {
		super(value, upperBound);
	}

	@Override
	public boolean apply(Object object) {
		if (this.getValue() instanceof ObjectDependence) {
			((ObjectDependence) this.getValue()).fill(object);
		}
		return getComparator().compare(this.getValue().get(), this.getAnchorValue().get()) <= 0;
	}

	@Override
	public String toString() {
		return this.getValue().toString() + " <= " + this.getAnchorValue().toString();
	}
}
