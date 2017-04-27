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
		Object value = this.value.get();
		return this.isIn(value);
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
		return this.value.toString() + " in " + sb.toString();
	}
}
