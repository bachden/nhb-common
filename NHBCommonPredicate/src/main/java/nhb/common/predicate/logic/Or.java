package nhb.common.predicate.logic;

import nhb.common.predicate.value.ObjectDependence;
import nhb.common.predicate.value.Value;

public class Or extends LogicPredicate {

	private static final long serialVersionUID = -7512761664686181908L;

	@SafeVarargs
	public Or(Value<Boolean>... values) {
		super(values);
	}

	@Override
	public boolean apply(Object obj) {
		for (Value<Boolean> value : this.values) {
			if (value instanceof ObjectDependence) {
				((ObjectDependence) value).fill(obj);
			}
			if (value.get()) {
				return true;
			}
		}
		return false;
	}

}
