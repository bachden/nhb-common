package com.nhb.common.predicate.logic;

import com.nhb.common.predicate.value.Value;

public class And extends LogicPredicate {

	private static final long serialVersionUID = -7229372524604330677L;

	@SafeVarargs
	public And(Value<Boolean>... values) {
		super(values);
	}

	@Override
	public Boolean get() {
		for (Value<Boolean> value : this.getValues()) {
			if (!value.get()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Value<Boolean> value : this.getValues()) {
			if (sb.length() > 0) {
				sb.append(" and ");
			}
			sb.append(value.toString());
		}
		return sb.toString();
	}
}
