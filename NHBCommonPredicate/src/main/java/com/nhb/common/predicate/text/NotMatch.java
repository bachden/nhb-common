package com.nhb.common.predicate.text;

import com.nhb.common.predicate.value.Value;

public class NotMatch extends Match {

	private static final long serialVersionUID = -8800586368806132191L;

	public NotMatch(Value<String> value, Value<String> pattern) {
		super(value, pattern);
	}

	@Override
	public Boolean get() {
		return !super.get();
	}

	@Override
	public String toString() {
		return this.getValue().toString() + " not like " + getAnchor().toString();
	}

}
