package com.nhb.common.predicate.object;

import com.nhb.common.predicate.value.Value;

public abstract class ObjectDependenceValue<Type> implements Value<Type>, ObjectDependence {

	private Object object;

	@Override
	public final void fill(Object object) {
		this.object = object;
		this.fill();
	}

	protected abstract void fill();

	protected Object getObject() {
		return this.object;
	}

}
