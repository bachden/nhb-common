package com.nhb.common.predicate.array;

import java.util.Collection;

import com.nhb.common.predicate.object.ObjectDependence;
import com.nhb.common.predicate.object.ObjectDependencePredicate;
import com.nhb.common.predicate.utils.NumberComparator;
import com.nhb.common.predicate.value.NumberValue;
import com.nhb.common.predicate.value.Value;

import lombok.AccessLevel;
import lombok.Getter;

@Getter(AccessLevel.PROTECTED)
abstract class ArrayPredicate extends ObjectDependencePredicate {

	private static final long serialVersionUID = 6508986421609289550L;

	private Value<?> value;
	protected Collection<?> collection;
	private NumberComparator numberComparator = new NumberComparator();

	ArrayPredicate(Value<?> value, Collection<?> collection) {
		this.collection = collection;
		this.value = value;
	}

	@Override
	protected void fill() {
		if (this.value instanceof ObjectDependence) {
			((ObjectDependence) this.value).fill(getObject());
		}
		for (Object obj : this.collection) {
			if (obj instanceof ObjectDependence) {
				((ObjectDependence) obj).fill(getObject());
			}
		}
	}

	protected boolean isIn(Object value) {
		if (value instanceof Number) {
			for (Object obj : this.collection) {
				if (obj instanceof NumberValue) {
					obj = ((NumberValue) obj).get();
				}
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
