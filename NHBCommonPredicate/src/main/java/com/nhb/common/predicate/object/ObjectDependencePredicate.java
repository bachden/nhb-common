package com.nhb.common.predicate.object;

import com.nhb.common.Loggable;
import com.nhb.common.predicate.Predicate;
import com.nhb.common.predicate.value.BooleanValue;

public abstract class ObjectDependencePredicate extends ObjectDependenceValue<Boolean>
		implements Predicate, BooleanValue, Loggable {

	private static final long serialVersionUID = 4665949298760960451L;

	@Override
	public final boolean apply(Object obj) {
		// getLogger().debug("Applying object on predicate: {}",
		// this.toString());
		this.fill(obj);
		return this.get();
	}
}
