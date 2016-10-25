package nhb.common.predicate.array;

import java.util.Collection;

import nhb.common.predicate.Predicate;
import nhb.common.predicate.value.ObjectDependenceValue;

abstract class ArrayPredicate extends ObjectDependenceValue<Boolean> implements Predicate {

	private static final long serialVersionUID = 6508986421609289550L;

	protected Collection<?> collection;

	ArrayPredicate(Collection<?> collection) {
		this.collection = collection;
	}

	@Override
	public Boolean get() {
		return this.apply(this.getObject());
	}
}
