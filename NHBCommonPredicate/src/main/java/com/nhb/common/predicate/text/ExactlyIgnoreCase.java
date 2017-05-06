package com.nhb.common.predicate.text;

import com.nhb.common.predicate.value.Value;

public class ExactlyIgnoreCase extends Exactly {

	private static final long serialVersionUID = 1367008970712713050L;

	public ExactlyIgnoreCase(Value<String> value, Value<String> valueToCompareWith) {
		super(value, valueToCompareWith);
	}

	@Override
	public Boolean get() {
		String value = this.getValue().get();
		String valueToCompareWith = this.getAnchor().get();
		if (value != null && valueToCompareWith != null) {
			return value.equalsIgnoreCase(valueToCompareWith);
		}
		return false;
	}

	@Override
	public String toString() {
		return this.getValue().toString() + " = " + getAnchor();
	}
}
