package com.nhb.common.predicate.array;

import java.util.Collection;

import com.nhb.common.predicate.math.MathOperator;
import com.nhb.common.predicate.value.Value;

public class NotIn extends ArrayPredicate {

	private static final long serialVersionUID = -2201186865744767582L;

	public NotIn(Value<?> value, Collection<?> collection) {
		super(value, collection);
	}

	@Override
	public Boolean get() {
		return !this.isIn(this.getValue().get());
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		for (Object obj : this.collection) {
			if (sb.length() > 1) {
				sb.append(", ");
			}
			String element = obj.toString();
			if (obj instanceof String) {
				element = element.replaceAll("\\'", "\\\\'");
				sb.append("'");
			}
			sb.append(element);
			if (obj instanceof String) {
				sb.append("'");
			}
		}
		sb.append(")");
		return (this.getValue() instanceof MathOperator ? "(" : "") + this.getValue().toString()
				+ (this.getValue() instanceof MathOperator ? ")" : "") + " not in " + sb.toString();
	}
}
