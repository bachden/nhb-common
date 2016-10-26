package com.nhb.common.predicate.array;

import java.util.Collection;

import com.nhb.common.predicate.value.ObjectDependence;
import com.nhb.common.predicate.value.Value;

public class In extends ArrayPredicate {

	private static final long serialVersionUID = -6072954289185444508L;
	
	private Value<?> value;

	public In(Value<?> value, Collection<?> collection) {
		super(collection);
		this.value = value;
	}

	@Override
	public boolean apply(Object object) {
		if (this.value instanceof ObjectDependence) {
			((ObjectDependence) this.value).fill(object);
		}
		return this.collection.contains(this.value.get());
	}

}
