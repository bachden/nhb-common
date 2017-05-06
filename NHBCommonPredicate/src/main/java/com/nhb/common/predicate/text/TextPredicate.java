package com.nhb.common.predicate.text;

import com.nhb.common.predicate.object.ObjectDependence;
import com.nhb.common.predicate.object.ObjectDependencePredicate;
import com.nhb.common.predicate.value.Value;

import lombok.AccessLevel;
import lombok.Getter;

@Getter(AccessLevel.PROTECTED)
abstract class TextPredicate extends ObjectDependencePredicate {

	private static final long serialVersionUID = 2927039166426942119L;

	private Value<String> value;

	private Value<String> anchor;

	public TextPredicate(Value<String> value, Value<String> anchor) {
		this.value = value;
		this.anchor = anchor;
	}

	@Override
	protected final void fill() {
		if (this.value instanceof ObjectDependence) {
			((ObjectDependence) this.value).fill(getObject());
		}
		if (this.anchor instanceof ObjectDependence) {
			((ObjectDependence) anchor).fill(getObject());
		}
		this._fill();
	}

	protected void _fill() {
		// do nothing
	}

}
