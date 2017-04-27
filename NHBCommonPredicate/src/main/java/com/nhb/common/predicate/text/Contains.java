package com.nhb.common.predicate.text;

import com.nhb.common.predicate.value.ObjectDependence;
import com.nhb.common.predicate.value.Value;

public class Contains extends TextPredicate {

	private static final long serialVersionUID = -361022097741037086L;

	private Value<String> searchString;

	public Contains(Value<String> value, Value<String> searchString) {
		super(value);
		this.searchString = searchString;
	}

	@Override
	public boolean apply(Object object) {
		if (this.value instanceof ObjectDependence) {
			((ObjectDependence) this.value).fill(object);
		}
		String value = this.value.get();
		String searchString = this.searchString.get();
		if (value != null && searchString != null) {
			return value.indexOf(searchString) >= 0;
		}
		return false;
	}

	@Override
	public String toString() {
		return this.value.toString() + " contains " + this.searchString.toString();
	}
}
