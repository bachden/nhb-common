package com.nhb.common.predicate.numeric;

import com.nhb.common.predicate.Predicate;
import com.nhb.common.predicate.object.ObjectDependence;
import com.nhb.common.predicate.utils.NumberComparator;
import com.nhb.common.predicate.value.NumberValue;
import com.nhb.common.predicate.value.Value;

abstract class NumericComparisonPredicate implements Predicate, Value<Boolean>, ObjectDependence {

	private static final long serialVersionUID = 6544419560407137153L;

	private NumberValue anchorValue;
	private NumberComparator comparator = new NumberComparator();
	private Object object;

	@Override
	public void fill(Object object) {
		this.object = object;
	}

	protected Object getObject() {
		return this.object;
	}

	@Override
	public Boolean get() {
		return this.apply(getObject());
	}

	public NumericComparisonPredicate(NumberValue value) {
		this.anchorValue = value;
	}

	protected NumberValue getAnchorValue() {
		return anchorValue;
	}

	protected NumberComparator getComparator() {
		return this.comparator;
	}
}
