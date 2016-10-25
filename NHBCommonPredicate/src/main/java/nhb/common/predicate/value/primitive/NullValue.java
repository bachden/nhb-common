package nhb.common.predicate.value.primitive;

import nhb.common.predicate.value.Value;

public class NullValue implements Value<Object> {

	@Override
	public Object get() {
		return null;
	}

}
