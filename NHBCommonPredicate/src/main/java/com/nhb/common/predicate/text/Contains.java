package com.nhb.common.predicate.text;

import com.nhb.common.predicate.value.Value;

public class Contains extends TextPredicate {

	private static final long serialVersionUID = -361022097741037086L;

	public Contains(Value<String> value, Value<String> searchString) {
		super(value, searchString);
	}

	@Override
	public Boolean get() {
		String value = this.getValue().get();
		String searchString = this.getAnchor().get();
		if (value != null && searchString != null) {
			return value.indexOf(searchString) >= 0;
		}
		return false;
	}

	@Override
	public String toString() {
		return this.getValue().toString() + " contains " + this.getAnchor().toString();
	}
}
