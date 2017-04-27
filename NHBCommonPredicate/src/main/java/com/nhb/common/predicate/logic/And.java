package com.nhb.common.predicate.logic;

import com.nhb.common.predicate.value.ObjectDependence;
import com.nhb.common.predicate.value.Value;

public class And extends LogicPredicate {

	private static final long serialVersionUID = -7229372524604330677L;

	@SafeVarargs
	public And(Value<Boolean>... values) {
		super(values);
	}

	@Override
	public boolean apply(Object obj) {
		for (Value<Boolean> value : this.values) {
			if (value instanceof ObjectDependence) {
				((ObjectDependence) value).fill(obj);
			}
			if (!value.get()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Value<Boolean> value : this.values) {
			if (sb.length() > 0) {
				sb.append(" and ");
			}
			sb.append(value.toString());
		}
		return sb.toString();
	}
}
