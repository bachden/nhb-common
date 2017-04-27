package com.nhb.common.predicate.numeric;

import com.nhb.common.predicate.value.ObjectDependence;
import com.nhb.common.predicate.value.Value;

public class NotEqual extends NumericComparisonPredicate {

	private static final long serialVersionUID = 9116064549816891158L;
	
	private Value<? extends Number> value;

	public NotEqual(Value<? extends Number> value, Value<? extends Number> anchorValue) {
		super(anchorValue);
		this.value = value;
	}

	@Override
	public boolean apply(Object object) {
		if (this.getValue() instanceof ObjectDependence) {
			((ObjectDependence) this.getValue()).fill(object);
		}
		return getComparator().compare(this.getValue().get(), this.getAnchorValue().get()) != 0;
	}

	protected Value<? extends Number> getValue() {
		return value;
	}

	@Override
	public String toString() {
		return this.value.toString() + " != " + this.getAnchorValue().toString();
	}
}
