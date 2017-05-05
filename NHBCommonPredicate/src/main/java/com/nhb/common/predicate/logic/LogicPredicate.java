package com.nhb.common.predicate.logic;

import java.util.Arrays;
import java.util.List;

import com.nhb.common.predicate.Predicate;
import com.nhb.common.predicate.object.ObjectDependence;
import com.nhb.common.predicate.value.Value;

abstract class LogicPredicate implements Predicate, ObjectDependence, Value<Boolean> {

	private static final long serialVersionUID = 2093960563027580604L;

	protected List<Value<Boolean>> values;
	private Object object;

	@SafeVarargs
	public LogicPredicate(Value<Boolean>... values) {
		this.values = Arrays.asList(values);
	}

	@Override
	public void fill(Object object) {
		this.object = object;
	}

	protected Object getObject() {
		return this.object;
	}

	@Override
	public Boolean get() {
		return this.apply(getObject());
	}
}
