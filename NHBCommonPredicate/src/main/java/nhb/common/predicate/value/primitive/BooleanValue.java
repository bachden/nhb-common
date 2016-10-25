package nhb.common.predicate.value.primitive;

import nhb.common.predicate.value.Value;

public class BooleanValue implements Value<Boolean> {

	private boolean value;

	@Override
	public Boolean get() {
		return this.value;
	}

}
