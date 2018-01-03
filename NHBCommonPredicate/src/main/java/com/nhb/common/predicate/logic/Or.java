package com.nhb.common.predicate.logic;

import com.nhb.common.predicate.object.ObjectDependence;
import com.nhb.common.predicate.value.Value;

public class Or extends LogicPredicate {

	private static final long serialVersionUID = -7512761664686181908L;

	@SafeVarargs
	public Or(Value<Boolean>... values) {
		super(values);
	}

	@Override
	public Boolean get() {
		for (Value<Boolean> value : this.getValues()) {
			if (value instanceof ObjectDependence) {
				((ObjectDependence) value).fill(getObject());
			}
			if (value.get()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Value<Boolean> value : this.getValues()) {
			if (sb.length() > 0) {
				sb.append(" or ");
			}
			sb.append(value.toString());
		}
		return sb.toString();
	}
}
