package com.nhb.common.predicate.text;

import com.nhb.common.predicate.value.Value;
import com.nhb.common.utils.StringUtils;

import lombok.AccessLevel;
import lombok.Getter;

@Getter(AccessLevel.PROTECTED)
public class Match extends TextPredicate {

	private static final long serialVersionUID = 500702453637229972L;

	public Match(Value<String> value, Value<String> pattern) {
		super(value, pattern);
	}

	@Override
	public Boolean get() {
		String value = this.getValue().get();
		String pattern = this.getAnchor().get();
		if (value != null && pattern != null) {
			return StringUtils.match(value, pattern);
		}
		return false;
	}

	@Override
	public String toString() {
		return this.getValue().toString() + " like " + getAnchor().toString();
	}
}
