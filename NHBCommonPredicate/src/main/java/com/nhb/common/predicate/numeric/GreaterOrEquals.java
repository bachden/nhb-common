package com.nhb.common.predicate.numeric;

import com.nhb.common.predicate.value.ObjectDependence;
import com.nhb.common.predicate.value.Value;

public class GreaterOrEquals extends GreaterThan {

	private static final long serialVersionUID = -715457138661262264L;

	public GreaterOrEquals(Value<? extends Number> value, Value<? extends Number> lowerBound) {
		super(value, lowerBound);
	}

	@Override
	public boolean apply(Object object) {
		if (this.getValue() instanceof ObjectDependence) {
			((ObjectDependence) this.getValue()).fill(object);
		}
		return getComparator().compare(this.getValue().get(), this.getAnchorValue().get()) >= 0;
	}
}
