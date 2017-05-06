package com.nhb.common.predicate;

import com.nhb.common.predicate.math.Add;
import com.nhb.common.predicate.math.Divide;
import com.nhb.common.predicate.math.DivideToInteger;
import com.nhb.common.predicate.math.Mod;
import com.nhb.common.predicate.math.Multiply;
import com.nhb.common.predicate.math.Pow;
import com.nhb.common.predicate.math.Sqrt;
import com.nhb.common.predicate.math.Subtract;
import com.nhb.common.predicate.object.getter.NumberAttributeGetterValue;
import com.nhb.common.predicate.value.NumberValue;
import com.nhb.common.predicate.value.primitive.RawNumberValue;
import com.nhb.common.utils.StringUtils;

public class NumberValues {

	// ADD
	public static NumberValue add(Object value1, Object value2) {
		if (value1 == null || value2 == null) {
			throw new NullPointerException("Value1 and value2 cannot be null");
		}

		if (value1 instanceof String) {
			if (StringUtils.isRepresentNumber((String) value1)) {
				value1 = new RawNumberValue(Double.valueOf((String) value1));
			} else {
				value1 = new NumberAttributeGetterValue((String) value1);
			}
		} else if (value1 instanceof Number) {
			value1 = new RawNumberValue((Number) value1);
		} else if (!(value1 instanceof NumberValue)) {
			throw new IllegalArgumentException(
					"Value1 has type " + value1.getClass().getName() + " is not supported by add operator");
		}

		if (value2 instanceof String) {
			if (StringUtils.isRepresentNumber((String) value2)) {
				value2 = new RawNumberValue(Double.valueOf((String) value2));
			} else {
				value2 = new NumberAttributeGetterValue((String) value2);
			}
		} else if (value1 instanceof Number) {
			value2 = new RawNumberValue((Number) value2);
		} else if (!(value2 instanceof NumberValue)) {
			throw new IllegalArgumentException(
					"Value2 has type " + value1.getClass().getName() + " is not supported by add operator");
		}

		return new Add((NumberValue) value1, (NumberValue) value2);
	}

	// SUBTRACT
	public static NumberValue subtract(Object value1, Object value2) {
		if (value1 == null || value2 == null) {
			throw new NullPointerException("Value1 and value2 cannot be null");
		}

		if (value1 instanceof String) {
			if (StringUtils.isRepresentNumber((String) value1)) {
				value1 = new RawNumberValue(Double.valueOf((String) value1));
			} else {
				value1 = new NumberAttributeGetterValue((String) value1);
			}
		} else if (value1 instanceof Number) {
			value1 = new RawNumberValue((Number) value1);
		} else if (!(value1 instanceof NumberValue)) {
			throw new IllegalArgumentException(
					"Value1 has type " + value1.getClass().getName() + " is not supported by add operator");
		}

		if (value2 instanceof String) {
			if (StringUtils.isRepresentNumber((String) value2)) {
				value2 = new RawNumberValue(Double.valueOf((String) value2));
			} else {
				value2 = new NumberAttributeGetterValue((String) value2);
			}
		} else if (value1 instanceof Number) {
			value2 = new RawNumberValue((Number) value2);
		} else if (!(value2 instanceof NumberValue)) {
			throw new IllegalArgumentException(
					"Value2 has type " + value1.getClass().getName() + " is not supported by add operator");
		}

		return new Subtract((NumberValue) value1, (NumberValue) value2);
	}

	// MULTIPLY
	public static NumberValue multiply(Object value1, Object value2) {
		if (value1 == null || value2 == null) {
			throw new NullPointerException("Value1 and value2 cannot be null");
		}

		if (value1 instanceof String) {
			if (StringUtils.isRepresentNumber((String) value1)) {
				value1 = new RawNumberValue(Double.valueOf((String) value1));
			} else {
				value1 = new NumberAttributeGetterValue((String) value1);
			}
		} else if (value1 instanceof Number) {
			value1 = new RawNumberValue((Number) value1);
		} else if (!(value1 instanceof NumberValue)) {
			throw new IllegalArgumentException(
					"Value1 has type " + value1.getClass().getName() + " is not supported by add operator");
		}

		if (value2 instanceof String) {
			if (StringUtils.isRepresentNumber((String) value2)) {
				value2 = new RawNumberValue(Double.valueOf((String) value2));
			} else {
				value2 = new NumberAttributeGetterValue((String) value2);
			}
		} else if (value1 instanceof Number) {
			value2 = new RawNumberValue((Number) value2);
		} else if (!(value2 instanceof NumberValue)) {
			throw new IllegalArgumentException(
					"Value2 has type " + value1.getClass().getName() + " is not supported by add operator");
		}

		return new Multiply((NumberValue) value1, (NumberValue) value2);
	}

	// DIVIDE
	public static NumberValue divide(Object value1, Object value2) {
		if (value1 == null || value2 == null) {
			throw new NullPointerException("Value1 and value2 cannot be null");
		}

		if (value1 instanceof String) {
			if (StringUtils.isRepresentNumber((String) value1)) {
				value1 = new RawNumberValue(Double.valueOf((String) value1));
			} else {
				value1 = new NumberAttributeGetterValue((String) value1);
			}
		} else if (value1 instanceof Number) {
			value1 = new RawNumberValue((Number) value1);
		} else if (!(value1 instanceof NumberValue)) {
			throw new IllegalArgumentException(
					"Value1 has type " + value1.getClass().getName() + " is not supported by add operator");
		}

		if (value2 instanceof String) {
			if (StringUtils.isRepresentNumber((String) value2)) {
				value2 = new RawNumberValue(Double.valueOf((String) value2));
			} else {
				value2 = new NumberAttributeGetterValue((String) value2);
			}
		} else if (value1 instanceof Number) {
			value2 = new RawNumberValue((Number) value2);
		} else if (!(value2 instanceof NumberValue)) {
			throw new IllegalArgumentException(
					"Value2 has type " + value1.getClass().getName() + " is not supported by add operator");
		}

		return new Divide((NumberValue) value1, (NumberValue) value2);
	}

	// DIVIDE TO INTEGER
	public static NumberValue divideToInteger(Object value1, Object value2) {
		if (value1 == null || value2 == null) {
			throw new NullPointerException("Value1 and value2 cannot be null");
		}

		if (value1 instanceof String) {
			if (StringUtils.isRepresentNumber((String) value1)) {
				value1 = new RawNumberValue(Double.valueOf((String) value1));
			} else {
				value1 = new NumberAttributeGetterValue((String) value1);
			}
		} else if (value1 instanceof Number) {
			value1 = new RawNumberValue((Number) value1);
		} else if (!(value1 instanceof NumberValue)) {
			throw new IllegalArgumentException(
					"Value1 has type " + value1.getClass().getName() + " is not supported by add operator");
		}

		if (value2 instanceof String) {
			if (StringUtils.isRepresentNumber((String) value2)) {
				value2 = new RawNumberValue(Double.valueOf((String) value2));
			} else {
				value2 = new NumberAttributeGetterValue((String) value2);
			}
		} else if (value1 instanceof Number) {
			value2 = new RawNumberValue((Number) value2);
		} else if (!(value2 instanceof NumberValue)) {
			throw new IllegalArgumentException(
					"Value2 has type " + value1.getClass().getName() + " is not supported by add operator");
		}

		return new DivideToInteger((NumberValue) value1, (NumberValue) value2);
	}

	// MOD
	public static NumberValue mod(Object value1, Object value2) {
		if (value1 == null || value2 == null) {
			throw new NullPointerException("Value1 and value2 cannot be null");
		}

		if (value1 instanceof String) {
			if (StringUtils.isRepresentNumber((String) value1)) {
				value1 = new RawNumberValue(Double.valueOf((String) value1));
			} else {
				value1 = new NumberAttributeGetterValue((String) value1);
			}
		} else if (value1 instanceof Number) {
			value1 = new RawNumberValue((Number) value1);
		} else if (!(value1 instanceof NumberValue)) {
			throw new IllegalArgumentException(
					"Value1 has type " + value1.getClass().getName() + " is not supported by add operator");
		}

		if (value2 instanceof String) {
			if (StringUtils.isRepresentNumber((String) value2)) {
				value2 = new RawNumberValue(Double.valueOf((String) value2));
			} else {
				value2 = new NumberAttributeGetterValue((String) value2);
			}
		} else if (value1 instanceof Number) {
			value2 = new RawNumberValue((Number) value2);
		} else if (!(value2 instanceof NumberValue)) {
			throw new IllegalArgumentException(
					"Value2 has type " + value1.getClass().getName() + " is not supported by add operator");
		}

		return new Mod((NumberValue) value1, (NumberValue) value2);
	}

	// POW
	public static NumberValue pow(Object value1, Object value2) {
		if (value1 == null || value2 == null) {
			throw new NullPointerException("Value1 and value2 cannot be null");
		}

		if (value1 instanceof String) {
			if (StringUtils.isRepresentNumber((String) value1)) {
				value1 = new RawNumberValue(Double.valueOf((String) value1));
			} else {
				value1 = new NumberAttributeGetterValue((String) value1);
			}
		} else if (value1 instanceof Number) {
			value1 = new RawNumberValue((Number) value1);
		} else if (!(value1 instanceof NumberValue)) {
			throw new IllegalArgumentException(
					"Value1 has type " + value1.getClass().getName() + " is not supported by add operator");
		}

		if (value2 instanceof String) {
			if (StringUtils.isRepresentNumber((String) value2)) {
				value2 = new RawNumberValue(Double.valueOf((String) value2));
			} else {
				value2 = new NumberAttributeGetterValue((String) value2);
			}
		} else if (value1 instanceof Number) {
			value2 = new RawNumberValue((Number) value2);
		} else if (!(value2 instanceof NumberValue)) {
			throw new IllegalArgumentException(
					"Value2 has type " + value1.getClass().getName() + " is not supported by add operator");
		}

		return new Pow((NumberValue) value1, (NumberValue) value2);
	}

	// SQRT
	public static NumberValue sqrt(Object value) {
		if (value == null) {
			throw new NullPointerException("Value cannot be null");
		}

		if (value instanceof String) {
			if (StringUtils.isRepresentNumber((String) value)) {
				value = new RawNumberValue(Double.valueOf((String) value));
			} else {
				value = new NumberAttributeGetterValue((String) value);
			}
		} else if (value instanceof Number) {
			value = new RawNumberValue((Number) value);
		} else if (!(value instanceof NumberValue)) {
			throw new IllegalArgumentException(
					"Value1 has type " + value.getClass().getName() + " is not supported by add operator");
		}

		return new Sqrt((NumberValue) value);
	}

}
