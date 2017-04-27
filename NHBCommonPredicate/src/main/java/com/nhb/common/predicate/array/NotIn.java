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
		Object value = this.value.get();
		return !this.isIn(value);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		for (Object obj : this.collection) {
			if (sb.length() > 1) {
				sb.append(", ");
			}
			String element = obj.toString();
			if (obj instanceof String) {
				element = element.replaceAll("\\'", "\\\\'");
				sb.append("'");
			}
			sb.append(element);
			if (obj instanceof String) {
				sb.append("'");
			}
		}
		sb.append(")");
		return this.value.toString() + " not in " + sb.toString();
	}
}
