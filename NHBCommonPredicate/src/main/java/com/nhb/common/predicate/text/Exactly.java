package com.nhb.common.predicate.text;

import com.nhb.common.predicate.value.Value;

import lombok.AccessLevel;
import lombok.Getter;

@Getter(AccessLevel.PROTECTED)
public class Exactly extends TextPredicate {

	private static final long serialVersionUID = 2233578312936754365L;

	public Exactly(Value<String> value, Value<String> valueToCompareWith) {
		super(value, valueToCompareWith);
	}

	@Override
	public Boolean get() {
		String value = this.getValue().get();
		String valueToCompareWith = this.getAnchor().get();
		if (value != null && valueToCompareWith != null) {
			return value.equals(valueToCompareWith);
		}
		return false;
	}

	@Override
	public String toString() {
		return this.getValue().toString() + " = " + getAnchor();
	}

}
