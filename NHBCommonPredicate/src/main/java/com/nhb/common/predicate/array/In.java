package com.nhb.common.predicate.array;

import java.util.Collection;

import com.nhb.common.predicate.math.MathOperator;
import com.nhb.common.predicate.value.CollectionValue;
import com.nhb.common.predicate.value.Value;

public class In extends ArrayPredicate {

	private static final long serialVersionUID = -6072954289185444508L;

	public In(Value<?> value, Collection<?> collection) {
		super(value, collection);
	}

	public In(Value<?> value, CollectionValue collection) {
		super(value, collection);
	}

	@Override
	public Boolean get() {
		return this.isCollectionContainsValue();
	}

	@Override
	public String toString() {
		return (this.getValue() instanceof MathOperator ? "(" : "") + this.getValue().toString()
				+ (this.getValue() instanceof MathOperator ? ")" : "") + " in " + this.getCollection().toString();
	}
}
