package com.nhb.common.predicate.value.primitive;

import java.util.Collection;

import com.nhb.common.predicate.object.ObjectDependence;
import com.nhb.common.predicate.object.ObjectDependenceValue;
import com.nhb.common.predicate.value.CollectionValue;
import com.nhb.common.predicate.value.RawValue;

public class RawCollectionValue extends ObjectDependenceValue<Collection<?>> implements CollectionValue, RawValue {

	private Collection<?> collection;

	public RawCollectionValue() {
		// do nothing
	}

	public RawCollectionValue(Collection<?> collection) {
		this.collection = collection;
	}

	@Override
	public Collection<?> get() {
		return this.collection;
	}

	@Override
	protected void fill() {
		if (this.collection != null) {
			for (Object entry : this.collection) {
				if (entry instanceof ObjectDependence) {
					((ObjectDependence) entry).fill(getObject());
				}
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		for (Object obj : this.get()) {
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
		return sb.toString();
	}
}
