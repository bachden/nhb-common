package com.nhb.common.predicate.logic;

import com.nhb.common.predicate.value.Value;

public class Not extends LogicPredicate {

	private static final long serialVersionUID = 1462652965842662052L;

	public Not(Value<Boolean> value) {
		super(value);
	}

	@Override
	public Boolean get() {
		return !this.getValues().get(0).get();
	}

	@Override
	public String toString() {
		return "not " + this.getValues().get(0).toString();
	}
}
