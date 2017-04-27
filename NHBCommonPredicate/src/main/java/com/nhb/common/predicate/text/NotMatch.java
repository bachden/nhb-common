package com.nhb.common.predicate.text;

import com.nhb.common.predicate.value.ObjectDependence;
import com.nhb.common.predicate.value.Value;
import com.nhb.common.utils.StringUtils;

public class NotMatch extends TextPredicate {

	private static final long serialVersionUID = -8800586368806132191L;

	private Value<String> pattern;

	public NotMatch(Value<String> value, Value<String> pattern) {
		super(value);
		this.pattern = pattern;
	}

	@Override
	public boolean apply(Object object) {
		if (this.value instanceof ObjectDependence) {
			((ObjectDependence) this.value).fill(object);
		}
		if (this.pattern instanceof ObjectDependence) {
			((ObjectDependence) this.pattern).fill(object);
		}
		String value = this.value.get();
		String pattern = this.pattern.get();
		if (value != null && pattern != null) {
			return !StringUtils.match(value, pattern);
		}
		return false;
	}

	@Override
	public String toString() {
		return this.value.toString() + " not like " + pattern.toString();
	}

}
