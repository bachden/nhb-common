package com.nhb.common.predicate.text;

import com.nhb.common.predicate.value.ObjectDependence;
import com.nhb.common.predicate.value.Value;

public class ExactlyIgnoreCase extends Exactly {

	private static final long serialVersionUID = 1367008970712713050L;

	public ExactlyIgnoreCase(Value<String> value, Value<String> valueToCompareWith) {
		super(value, valueToCompareWith);
	}

	@Override
	public boolean apply(Object object) {
		if (this.value instanceof ObjectDependence) {
			((ObjectDependence) this.value).fill(object);
		}
		String value = this.value.get();
		String valueToCompareWith = this.valueToCompareWith.get();
		if (value != null && valueToCompareWith != null) {
			return value.equalsIgnoreCase(valueToCompareWith);
		}
		return false;
	}

	@Override
	public String toString() {
		return this.value.toString() + " = " + valueToCompareWith;
	}
}
