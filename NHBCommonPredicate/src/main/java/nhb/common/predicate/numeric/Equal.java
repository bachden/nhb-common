package nhb.common.predicate.numeric;

import nhb.common.predicate.value.ObjectDependence;
import nhb.common.predicate.value.Value;

public class Equal extends NumericComparisonPredicate {

	private static final long serialVersionUID = 562211748204617410L;

	private Value<? extends Number> value;

	public Equal(Value<? extends Number> value, Value<? extends Number> anchorValue) {
		super(anchorValue);
		this.value = value;
	}

	@Override
	public boolean apply(Object object) {
		if (this.getValue() instanceof ObjectDependence) {
			((ObjectDependence) this.getValue()).fill(object);
		}
		return getComparator().compare(this.getValue().get(), this.getAnchorValue().get()) == 0;
	}

	protected Value<? extends Number> getValue() {
		return value;
	}
}
