package com.nhb.common.predicate.array;

import java.util.Collection;

import com.nhb.common.predicate.object.ObjectDependence;
import com.nhb.common.predicate.object.ObjectDependencePredicate;
import com.nhb.common.predicate.utils.NumberComparator;
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

	protected boolean isCollectionContainsValue() {
		Object value = this.value.get();
		for (Object entry : this.collection) {
			if (entry instanceof Value<?>) {
				entry = ((Value<?>) entry).get();
			}
			getLogger().debug("Checking equals: {} and {}", value, entry);
			if (value == entry) {
				return true;
			}
			if (value instanceof Number && entry instanceof Number) {
				if (numberComparator.compare(value, entry) == 0) {
					return true;
				}
			} else if (value.equals(entry)) {
				return true;
			}
		}
		return false;
	}
}
