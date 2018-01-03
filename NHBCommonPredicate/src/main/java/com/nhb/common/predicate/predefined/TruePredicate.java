package com.nhb.common.predicate.predefined;

import com.nhb.common.predicate.Predicate;

public class TruePredicate implements Predicate {

	private static final long serialVersionUID = 3311741422316515199L;

	@Override
	public boolean apply(Object obj) {
		return true;
	}

	@Override
	public String toString() {
		return "TRUE";
	}
}
