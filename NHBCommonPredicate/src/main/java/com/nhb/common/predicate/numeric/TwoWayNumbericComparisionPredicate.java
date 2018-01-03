package com.nhb.common.predicate.numeric;

import com.nhb.common.predicate.object.ObjectDependence;
import com.nhb.common.predicate.object.ObjectDependencePredicate;
import com.nhb.common.predicate.utils.NumberComparator;
import com.nhb.common.predicate.value.NumberValue;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter(AccessLevel.PROTECTED)
@Setter
abstract class TwoWayNumbericComparisionPredicate extends ObjectDependencePredicate {

	private static final long serialVersionUID = -4802602205044966238L;

	private NumberComparator comparator = new NumberComparator();
	private NumberValue lowerBound;
	private NumberValue upperBound;
	private NumberValue value;

	protected TwoWayNumbericComparisionPredicate() {
	}

	protected TwoWayNumbericComparisionPredicate(NumberValue value) {
		this();
		this.value = value;
	}

	protected TwoWayNumbericComparisionPredicate(NumberValue value, NumberValue lowerBound, NumberValue upperBound) {
		this(value);
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
	}

	@Override
	protected void fill() {
		if (this.getValue() instanceof ObjectDependence) {
			((ObjectDependence) this.getValue()).fill(getObject());
		}
		if (this.getUpperBound() instanceof ObjectDependence) {
			((ObjectDependence) this.getUpperBound()).fill(getObject());
		}
		if (this.getLowerBound() instanceof ObjectDependence) {
			((ObjectDependence) this.getLowerBound()).fill(getObject());
		}
	}

}
