package com.nhb.common.predicate.pointer;

import com.nhb.common.predicate.object.getter.PointerAttributeGetterValue;

public class IsNull extends IsNotNull {

	private static final long serialVersionUID = 2915699187242181169L;

	public IsNull() {

	}

	public IsNull(PointerAttributeGetterValue value) {
		super(value);
	}

	@Override
	public Boolean get() {
		return !super.get();
	}

	@Override
	public String toString() {
		return this.getValue().toString() + " is null";
	}
}
