package com.nhb.common.predicate.value.primitive;

import com.nhb.common.predicate.value.RawValue;
import com.nhb.common.predicate.value.Value;

public final class NullValue implements Value<Object>, RawValue {

	@Override
	public Object get() {
		return null;
	}

	@Override
	public String toString() {
		return "null";
	}
}
