package com.nhb.common.predicate.array;

import java.util.Collection;

import com.nhb.common.predicate.object.ObjectDependence;
import com.nhb.common.predicate.object.ObjectDependencePredicate;
import com.nhb.common.predicate.utils.NumberComparator;
import com.nhb.common.predicate.value.CollectionValue;
import com.nhb.common.predicate.value.Value;
import com.nhb.common.predicate.value.primitive.RawCollectionValue;

import lombok.AccessLevel;
import lombok.Getter;

@Getter(AccessLevel.PROTECTED)
abstract class ArrayPredicate extends ObjectDependencePredicate {

	private static final long serialVersionUID = 6508986421609289550L;

	private Value<?> value;
	private CollectionValue collection;
	private NumberComparator numberComparator = new NumberComparator();

	ArrayPredicate(Value<?> value, Collection<?> collection) {
		this(value, new RawCollectionValue(collection));
	}

	public ArrayPredicate(Value<?> value, CollectionValue collection) {
		this.collection = collection;
		this.value = value;
	}

	@Override
	protected void fill() {
		if (this.value instanceof ObjectDependence) {
			((ObjectDependence) this.value).fill(getObject());
		}
		if (this.collection instanceof ObjectDependence) {
			((ObjectDependence) this.collection).fill(getObject());
		}
	}

	protected boolean isCollectionContainsValue() {
		Object value = this.value.get();
		for (Object entry : this.collection.get()) {
			if (entry instanceof Value<?>) {
				entry = ((Value<?>) entry).get();
			}
			// getLogger().debug("Checking equals: {} and {}", value, entry);
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
