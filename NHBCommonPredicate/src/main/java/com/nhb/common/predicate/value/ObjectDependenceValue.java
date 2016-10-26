package com.nhb.common.predicate.value;

public abstract class ObjectDependenceValue<Type> implements Value<Type>, ObjectDependence {

	private Object object;

	@Override
	public void fill(Object object) {
		this.object = object;
	}

	protected Object getObject() {
		return this.object;
	}

}
