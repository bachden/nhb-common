package com.nhb.common.predicate.logic;

import com.nhb.common.predicate.value.Value;

public class Is extends LogicPredicate {

	private static final long serialVersionUID = 807015730137332579L;

	public Is(Value<Boolean> value) {
		super(value);
	}

	@Override
	public Boolean get() {
		if (this.getValues().size() == 1) {
			return this.getValues().get(0).get();
		}
		return false;
	}

	@Override
	public String toString() {
		return this.getValues().get(0).toString();
	}
}
