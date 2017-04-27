package com.nhb.common.predicate.pointer;

import com.nhb.common.predicate.Predicate;
import com.nhb.common.predicate.value.ObjectDependence;
import com.nhb.common.predicate.value.Value;
import com.nhb.common.predicate.value.getter.PointerAttributeGetter;

public class IsNotNull implements Predicate, ObjectDependence, Value<Boolean> {

	private static final long serialVersionUID = 7293242036653166493L;

	private Object object;
	private PointerAttributeGetter value;

	public IsNotNull() {
		// do nothing
	}

	public IsNotNull(PointerAttributeGetter value) {
		this.value = value;
	}

	@Override
	public Boolean get() {
		return this.apply(this.object);
	}

	@Override
	public boolean apply(Object obj) {
		if (this.value != null) {
			if (this.value instanceof ObjectDependence) {
				this.value.fill(obj);
			}
			return this.value.get() != null;
		}
		return obj != null;
	}

	@Override
	public void fill(Object object) {
		this.object = object;
	}

	@Override
	public String toString() {
		return this.value.toString() + " is not null";
	}
}
