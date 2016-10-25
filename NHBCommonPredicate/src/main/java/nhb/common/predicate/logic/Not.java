package nhb.common.predicate.logic;

import nhb.common.predicate.Predicate;
import nhb.common.predicate.value.ObjectDependence;
import nhb.common.predicate.value.Value;

public class Not implements Predicate {

	private static final long serialVersionUID = 1462652965842662052L;
	
	private Value<Boolean> value;

	public Not(Value<Boolean> value) {
		this.value = value;
	}

	@Override
	public boolean apply(Object obj) {
		if (value instanceof ObjectDependence) {
			((ObjectDependence) value).fill(obj);
		}
		return !value.get();
	}
}
