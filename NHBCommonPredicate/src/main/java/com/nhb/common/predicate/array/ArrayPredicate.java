package com.nhb.common.predicate.array;

import java.util.Collection;

import com.nhb.common.predicate.Predicate;
import com.nhb.common.predicate.utils.NumberComparator;
import com.nhb.common.predicate.value.ObjectDependenceValue;

abstract class ArrayPredicate extends ObjectDependenceValue<Boolean> implements Predicate {

	private static final long serialVersionUID = 6508986421609289550L;

	protected Collection<?> collection;
	private NumberComparator numberComparator = new NumberComparator();

	ArrayPredicate(Collection<?> collection) {
		this.collection = collection;
	}

	@Override
	public Boolean get() {
		return this.apply(this.getObject());
	}

	protected boolean isIn(Object value) {
		if (value instanceof Number) {
			for (Object obj : this.collection) {
				if (obj instanceof Number) {
					if (numberComparator.compare(obj, value) == 0) {
						return true;
					}
				}
			}
		}
		return this.collection.contains(value);
	}
}
