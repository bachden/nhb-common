package com.nhb.common.predicate.numeric;

import com.nhb.common.predicate.value.NumberValue;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter(AccessLevel.PROTECTED)
public class Equals extends OneWayNumericComparisonPredicate {

	private static final long serialVersionUID = 562211748204617410L;

	public Equals(NumberValue value, NumberValue anchorValue) {
		super(value, anchorValue);
	}

	@Override
	public Boolean get() {
		return getComparator().compare(this.getValue().get(), this.getAnchorValue().get()) == 0;
	}

	@Override
	public String toString() {
		return this.getValue().toString() + " = " + this.getAnchorValue().toString();
	}
}
