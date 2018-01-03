package com.nhb.common.predicate.array;

import java.util.Collection;

import com.nhb.common.predicate.math.MathOperator;
import com.nhb.common.predicate.value.CollectionValue;
import com.nhb.common.predicate.value.Value;

public class NotIn extends ArrayPredicate {

	private static final long serialVersionUID = -2201186865744767582L;

	public NotIn(Value<?> value, Collection<?> collection) {
		super(value, collection);
	}

	public NotIn(Value<?> value, CollectionValue collection) {
		super(value, collection);
	}

	@Override
	public Boolean get() {
		return !this.isCollectionContainsValue();
	}

	@Override
	public String toString() {
		return (this.getValue() instanceof MathOperator ? "(" : "") + this.getValue().toString()
				+ (this.getValue() instanceof MathOperator ? ")" : "") + " not in " + this.getCollection().toString();
	}
}
