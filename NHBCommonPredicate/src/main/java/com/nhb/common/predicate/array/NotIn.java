package com.nhb.common.predicate.array;

import java.util.Collection;

import com.nhb.common.predicate.value.ObjectDependence;
import com.nhb.common.predicate.value.Value;

public class NotIn extends ArrayPredicate {

	private static final long serialVersionUID = -2201186865744767582L;

	private Value<?> value;

	public NotIn(Value<?> value, Collection<?> collection) {
		super(collection);
		this.value = value;
	}

	@Override
	public boolean apply(Object object) {
		if (this.value instanceof ObjectDependence) {
			((ObjectDependence) this.value).fill(object);
		}
		return !this.collection.contains(this.value.get());
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		for (Object obj : this.collection) {
			if (sb.length() > 1) {
				sb.append(", ");
			}
			sb.append(obj.toString());
		}
		sb.append(")");
		return this.value.toString() + " not in " + sb.toString();
	}
}
