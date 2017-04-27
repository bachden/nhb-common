package com.nhb.common.predicate.text;

import com.nhb.common.predicate.value.ObjectDependence;
import com.nhb.common.predicate.value.Value;

public class Exactly extends TextPredicate {

	private static final long serialVersionUID = 2233578312936754365L;
	
	protected Value<String> valueToCompareWith;

	public Exactly(Value<String> value, Value<String> valueToCompareWith) {
		super(value);
		this.valueToCompareWith = valueToCompareWith;
	}

	@Override
	public boolean apply(Object object) {
		if (this.value instanceof ObjectDependence) {
			((ObjectDependence) this.value).fill(object);
		}
		String value = this.value.get();
		String valueToCompareWith = this.valueToCompareWith.get();
		if (value != null && valueToCompareWith != null) {
			return value.equals(valueToCompareWith);
		}
		return false;
	}

	@Override
	public String toString() {
		return this.value.toString() + " = " + valueToCompareWith;
	}

}
