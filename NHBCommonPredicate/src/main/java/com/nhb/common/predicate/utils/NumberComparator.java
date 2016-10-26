package com.nhb.common.predicate.utils;

import java.math.BigDecimal;
import java.util.Comparator;

public class NumberComparator implements Comparator<Object> {

	@Override
	public int compare(Object val, Object other) {
		if (val != null && other != null) {
			return new BigDecimal(val.toString()).compareTo(new BigDecimal(other.toString()));
		}
		return 0;
	}
}
