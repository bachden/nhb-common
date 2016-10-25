package nhb.common.predicate.value.primitive;

import nhb.common.predicate.value.Value;

public class StringValue implements Value<String> {

	private String value;

	public StringValue(String value) {
		this.value = value;
	}

	@Override
	public String get() {
		return this.value;
	}

}
