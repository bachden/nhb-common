package com.nhb.common.predicate.text;

import com.nhb.common.predicate.value.Value;

public class NotExactly extends Exactly {

	private static final long serialVersionUID = 2233578312936754365L;

	public NotExactly(Value<String> value, Value<String> valueToCompareWith) {
		super(value, valueToCompareWith);
	}

	@Override
	public boolean apply(Object object) {
		return !super.apply(object);
	}

	@Override
	public String toString() {
		return this.value.toString() + " != " + valueToCompareWith;
	}

}
