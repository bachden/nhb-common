package com.nhb.common.predicate.predefined;

import com.nhb.common.predicate.Predicate;

public class FalsePredicate implements Predicate {

	private static final long serialVersionUID = 3311741422316515199L;

	@Override
	public boolean apply(Object obj) {
		return false;
	}
	
	@Override
	public String toString() {
		return "FALSE";
	}
}
