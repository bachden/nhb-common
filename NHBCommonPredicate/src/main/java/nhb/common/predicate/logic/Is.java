package nhb.common.predicate.logic;

import nhb.common.predicate.Predicate;
import nhb.common.predicate.value.ObjectDependence;
import nhb.common.predicate.value.Value;

public class Is implements Predicate {

	private static final long serialVersionUID = 807015730137332579L;
	
	private Value<Boolean> value;

	public Is(Value<Boolean> value) {
		this.value = value;
	}

	@Override
	public boolean apply(Object obj) {
		if (this.value instanceof ObjectDependence) {
			((ObjectDependence) this.value).fill(obj);
		}
		return this.value.get();
	}
}
