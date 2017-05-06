package com.nhb.common.predicate.math;

import com.nhb.common.predicate.object.ObjectDependenceValue;

public abstract class MathOperator extends ObjectDependenceValue<Number> {

	@Override
	public final void fill(Object object) {
		super.fill(object);
		this.fill();
	}

	protected abstract void fill();

	public abstract String getSymbol();
}
