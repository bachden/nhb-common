package com.nhb.common.predicate.numeric;

import com.nhb.common.predicate.object.ObjectDependence;
import com.nhb.common.predicate.object.ObjectDependencePredicate;
import com.nhb.common.predicate.utils.NumberComparator;
import com.nhb.common.predicate.value.NumberValue;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter(AccessLevel.PROTECTED)
abstract class OneWayNumericComparisonPredicate extends ObjectDependencePredicate {

	private static final long serialVersionUID = 6544419560407137153L;

	private NumberComparator comparator = new NumberComparator();

	private NumberValue value;
	private NumberValue anchorValue;

	@Override
	public void fill() {
		if (this.value instanceof ObjectDependence) {
			((ObjectDependence) this.value).fill(getObject());
		}
		if (this.anchorValue instanceof ObjectDependence) {
			((ObjectDependence) this.anchorValue).fill(getObject());
		}
	}

	protected OneWayNumericComparisonPredicate() {

	}

	protected OneWayNumericComparisonPredicate(NumberValue value, NumberValue anchorValue) {
		this.value = value;
		this.anchorValue = anchorValue;
	}
}
