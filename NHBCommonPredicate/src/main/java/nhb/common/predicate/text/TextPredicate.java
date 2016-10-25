package nhb.common.predicate.text;

import nhb.common.predicate.Predicate;
import nhb.common.predicate.value.ObjectDependence;
import nhb.common.predicate.value.Value;

abstract class TextPredicate implements Predicate, Value<Boolean>, ObjectDependence {

	private static final long serialVersionUID = 2927039166426942119L;

	protected Value<String> value;
	private Object object;

	public TextPredicate(Value<String> value) {
		this.value = value;
	}

	@Override
	public void fill(Object object) {
		this.object = object;
	}

	protected Object getObject() {
		return this.object;
	}

	@Override
	public Boolean get() {
		return this.apply(getObject());
	}
}
