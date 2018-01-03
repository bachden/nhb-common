package com.nhb.common.predicate.logic;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.NullArgumentException;

import com.nhb.common.predicate.object.ObjectDependence;
import com.nhb.common.predicate.object.ObjectDependencePredicate;
import com.nhb.common.predicate.value.Value;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter(AccessLevel.PROTECTED)
abstract class LogicPredicate extends ObjectDependencePredicate {

	private static final long serialVersionUID = 2093960563027580604L;

	private final List<Value<Boolean>> values = new ArrayList<>();

	@SafeVarargs
	public LogicPredicate(Value<Boolean>... values) {
		if (values != null) {
			for (Value<Boolean> value : values) {
				if (value == null) {
					throw new NullArgumentException("Value for LogicPredicate cannot be null");
				}
				this.values.add(value);
			}
		} else {
			throw new NullArgumentException("Value for LogicPredicate cannot be null");
		}
	}

	@Override
	protected final void fill() {
		for (Value<Boolean> value : this.getValues()) {
			if (value instanceof ObjectDependence) {
				((ObjectDependence) value).fill(getObject());
			}
		}
	}
}
