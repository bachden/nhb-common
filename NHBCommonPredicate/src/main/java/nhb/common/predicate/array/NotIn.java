package nhb.common.predicate.array;

import java.util.Collection;

import nhb.common.predicate.value.ObjectDependence;
import nhb.common.predicate.value.Value;

public class NotIn extends ArrayPredicate {

	private static final long serialVersionUID = -2201186865744767582L;

	private Value<?> value;

	public NotIn(Value<?> value, Collection<?> collection) {
		super(collection);
		this.value = value;
	}

	@Override
	public boolean apply(Object object) {
		if (this.value instanceof ObjectDependence) {
			((ObjectDependence) this.value).fill(object);
		}
		return !this.collection.contains(this.value.get());
	}

}
