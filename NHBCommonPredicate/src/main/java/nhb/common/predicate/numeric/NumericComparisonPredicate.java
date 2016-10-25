package nhb.common.predicate.numeric;

import nhb.common.predicate.Predicate;
import nhb.common.predicate.utils.NumberComparator;
import nhb.common.predicate.value.ObjectDependence;
import nhb.common.predicate.value.Value;

abstract class NumericComparisonPredicate implements Predicate, Value<Boolean>, ObjectDependence {

	private static final long serialVersionUID = 6544419560407137153L;

	private Value<? extends Number> anchorValue;
	private NumberComparator comparator = new NumberComparator();
	private Object object;

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

	public NumericComparisonPredicate(Value<? extends Number> value) {
		this.anchorValue = value;
	}

	protected Value<? extends Number> getAnchorValue() {
		return anchorValue;
	}

	protected NumberComparator getComparator() {
		return this.comparator;
	}
}
