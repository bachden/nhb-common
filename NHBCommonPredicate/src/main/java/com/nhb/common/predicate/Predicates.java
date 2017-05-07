package com.nhb.common.predicate;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.nhb.common.annotations.ThreadSafe;
import com.nhb.common.predicate.array.In;
import com.nhb.common.predicate.array.NotIn;
import com.nhb.common.predicate.logic.And;
import com.nhb.common.predicate.logic.Is;
import com.nhb.common.predicate.logic.Not;
import com.nhb.common.predicate.logic.Or;
import com.nhb.common.predicate.numeric.Between;
import com.nhb.common.predicate.numeric.BetweenIncludeBoth;
import com.nhb.common.predicate.numeric.BetweenIncludeLeft;
import com.nhb.common.predicate.numeric.BetweenIncludeRight;
import com.nhb.common.predicate.numeric.Equals;
import com.nhb.common.predicate.numeric.GreaterOrEquals;
import com.nhb.common.predicate.numeric.GreaterThan;
import com.nhb.common.predicate.numeric.LessOrEquals;
import com.nhb.common.predicate.numeric.LessThan;
import com.nhb.common.predicate.numeric.NotBetween;
import com.nhb.common.predicate.numeric.NotBetweenIncludeBoth;
import com.nhb.common.predicate.numeric.NotBetweenIncludeLeft;
import com.nhb.common.predicate.numeric.NotBetweenIncludeRight;
import com.nhb.common.predicate.numeric.NotEquals;
import com.nhb.common.predicate.object.getter.BooleanAttributeGetterValue;
import com.nhb.common.predicate.object.getter.NumberAttributeGetterValue;
import com.nhb.common.predicate.object.getter.PointerAttributeGetterValue;
import com.nhb.common.predicate.object.getter.StringAttributeGetterValue;
import com.nhb.common.predicate.object.value.PredicateWrapperBooleanValue;
import com.nhb.common.predicate.pointer.IsNotNull;
import com.nhb.common.predicate.pointer.IsNull;
import com.nhb.common.predicate.sql.SqlPredicateThreadLocal;
import com.nhb.common.predicate.text.Contains;
import com.nhb.common.predicate.text.Exactly;
import com.nhb.common.predicate.text.ExactlyIgnoreCase;
import com.nhb.common.predicate.text.Match;
import com.nhb.common.predicate.text.NotMatch;
import com.nhb.common.predicate.value.CollectionValue;
import com.nhb.common.predicate.value.NumberValue;
import com.nhb.common.predicate.value.StringValue;
import com.nhb.common.predicate.value.Value;
import com.nhb.common.predicate.value.primitive.RawNumberValue;
import com.nhb.common.predicate.value.primitive.RawStringValue;

@SuppressWarnings("unchecked")
public final class Predicates {

	public static Predicate and(Value<Boolean>... values) {
		return new And(values);
	}

	public static Predicate and(final Predicate p1, final Predicate p2) {
		Value<Boolean> v1 = null;
		Value<Boolean> v2 = null;
		if (p1 instanceof Value) {
			v1 = (Value<Boolean>) p1;
		} else {
			v1 = new PredicateWrapperBooleanValue(p1);
		}
		if (p2 instanceof Value) {
			v2 = (Value<Boolean>) p2;
		} else {
			v2 = new PredicateWrapperBooleanValue(p2);
		}
		return new And(v1, v2);
	}

	public static Predicate or(Value<Boolean>... values) {
		return new Or(values);
	}

	public static Predicate or(final Predicate p1, final Predicate p2) {
		Value<Boolean> v1 = null;
		Value<Boolean> v2 = null;
		if (p1 instanceof Value) {
			v1 = (Value<Boolean>) p1;
		} else {
			v1 = new PredicateWrapperBooleanValue(p1);
		}
		if (p2 instanceof Value) {
			v2 = (Value<Boolean>) p2;
		} else {
			v2 = new PredicateWrapperBooleanValue(p2);
		}
		return new Or(v1, v2);
	}

	public static Predicate not(Value<Boolean> value) {
		return new Not(value);
	}

	public static Predicate not(final Predicate predicate) {
		if (predicate instanceof Value) {
			return new Not((Value<Boolean>) predicate);
		} else {
			return new Not(new PredicateWrapperBooleanValue(predicate));
		}
	}

	public static PredicateBuilder not(final PredicateBuilder predicateBuilder) {
		predicateBuilder.push(not(predicateBuilder.poll()));
		return predicateBuilder;
	}

	public static Predicate not(String attribute) {
		return not(new BooleanAttributeGetterValue(attribute));
	}

	public static Predicate is(Value<Boolean> value) {
		return new Is(value);
	}

	public static Predicate is(String attribute) {
		return is(new BooleanAttributeGetterValue(attribute));
	}

	public static Predicate is(final Predicate predicate) {
		if (predicate instanceof Value) {
			return new Is((Value<Boolean>) predicate);
		} else {
			return new Is(new PredicateWrapperBooleanValue(predicate));
		}
	}

	public static PredicateBuilder is(final PredicateBuilder predicateBuilder) {
		predicateBuilder.push(is(predicateBuilder.poll()));
		return predicateBuilder;
	}

	// *******
	public static Predicate greaterThan(NumberValue value, NumberValue lowerBound) {
		return new GreaterThan(value, lowerBound);
	}

	public static Predicate greaterThan(String attribute, NumberValue lowerBound) {
		return greaterThan(new NumberAttributeGetterValue(attribute), lowerBound);
	}

	public static Predicate greaterThan(String attribute, Number value) {
		return greaterThan(new NumberAttributeGetterValue(attribute), new RawNumberValue(value));
	}

	public static Predicate greaterOrEquals(NumberValue value, NumberValue lowerBound) {
		return new GreaterOrEquals(value, lowerBound);
	}

	public static Predicate greaterEqual(String attribute, NumberValue lowerBound) {
		return greaterOrEquals(new NumberAttributeGetterValue(attribute), lowerBound);
	}

	public static Predicate greaterOrEquals(String attribute, Number value) {
		return greaterOrEquals(new NumberAttributeGetterValue(attribute), new RawNumberValue(value));
	}

	// *******
	public static Predicate lessThan(NumberValue value, NumberValue lowerBound) {
		return new LessThan(value, lowerBound);
	}

	public static Predicate lessThan(String attribute, NumberValue upperBound) {
		return lessThan(new NumberAttributeGetterValue(attribute), upperBound);
	}

	public static Predicate lessThan(String attribute, Number value) {
		return lessThan(new NumberAttributeGetterValue(attribute), new RawNumberValue(value));
	}

	public static Predicate lessEquals(NumberValue value, NumberValue upperBound) {
		return new LessOrEquals(value, upperBound);
	}

	public static Predicate lessEquals(String attribute, NumberValue upperBound) {
		return lessEquals(new NumberAttributeGetterValue(attribute), upperBound);
	}

	public static Predicate lessEquals(String attribute, Number value) {
		return lessEquals(new NumberAttributeGetterValue(attribute), new RawNumberValue(value));
	}

	// *******
	public static Predicate between(NumberValue value, NumberValue lowerBound, NumberValue upperBound,
			boolean includeLeft, boolean includeRight) {
		if (includeLeft) {
			if (includeRight) {
				return new BetweenIncludeBoth(value, lowerBound, upperBound);
			} else {
				return new BetweenIncludeLeft(value, lowerBound, upperBound);
			}
		} else {
			if (includeRight) {
				return new BetweenIncludeRight(value, lowerBound, upperBound);
			} else {
				return new Between(value, lowerBound, upperBound);
			}
		}
	}

	public static Predicate between(String attribute, NumberValue lowerBound, NumberValue upperBound,
			boolean includeLeft, boolean includeRight) {
		return between(new NumberAttributeGetterValue(attribute), lowerBound, upperBound, includeLeft, includeRight);
	}

	public static Predicate between(String attribute, Number lowerBound, Number upperBound, boolean includeLeft,
			boolean includeRight) {
		return between(new NumberAttributeGetterValue(attribute), new RawNumberValue(lowerBound),
				new RawNumberValue(upperBound), includeLeft, includeRight);
	}

	public static Predicate between(NumberValue value, Number lowerBound, Number upperBound, boolean includeLeft,
			boolean includeRight) {
		return between(value, new RawNumberValue(lowerBound), new RawNumberValue(upperBound), includeLeft,
				includeRight);
	}

	// *******
	public static Predicate notBetween(NumberValue value, NumberValue lowerBound, NumberValue upperBound,
			boolean includeLeft, boolean includeRight) {
		if (includeLeft) {
			if (includeRight) {
				return new NotBetweenIncludeBoth(value, lowerBound, upperBound);
			} else {
				return new NotBetweenIncludeLeft(value, lowerBound, upperBound);
			}
		} else {
			if (includeRight) {
				return new NotBetweenIncludeRight(value, lowerBound, upperBound);
			} else {
				return new NotBetween(value, lowerBound, upperBound);
			}
		}
	}

	public static Predicate notBetween(String attribute, NumberValue lowerBound, NumberValue upperBound,
			boolean includeLeft, boolean includeRight) {
		return notBetween(new NumberAttributeGetterValue(attribute), lowerBound, upperBound, includeLeft, includeRight);
	}

	public static Predicate notBetween(String attribute, Number lowerBound, Number upperBound, boolean includeLeft,
			boolean includeRight) {
		return notBetween(new NumberAttributeGetterValue(attribute), new RawNumberValue(lowerBound),
				new RawNumberValue(upperBound), includeLeft, includeRight);
	}

	public static Predicate notBetween(NumberValue value, Number lowerBound, Number upperBound, boolean includeLeft,
			boolean includeRight) {
		return notBetween(value, new RawNumberValue(lowerBound), new RawNumberValue(upperBound), includeLeft,
				includeRight);
	}

	public static Predicate isNull(String attribute) {
		return new IsNull(new PointerAttributeGetterValue(attribute));
	}

	public static Predicate notNull(String attribute) {
		return new IsNotNull(new PointerAttributeGetterValue(attribute));
	}

	public static Predicate isNull() {
		return new IsNull();
	}

	public static Predicate notNull() {
		return new IsNotNull();
	}

	public static Predicate exactly(String attribute, String value) {
		return new Exactly(new StringAttributeGetterValue(attribute), new RawStringValue(value));
	}

	public static Predicate exactly(Value<String> value1, Value<String> value2) {
		return new Exactly(value1, value2);
	}

	public static Predicate exactly(Value<String> value1, String value2) {
		return new Exactly(value1, new RawStringValue(value2));
	}

	public static Predicate exactlyIgnoreCase(String attribute, String value) {
		return new ExactlyIgnoreCase(new StringAttributeGetterValue(attribute), new RawStringValue(value));
	}

	public static Predicate match(String attribute, String pattern) {
		return new Match(new StringAttributeGetterValue(attribute), new RawStringValue(pattern));
	}

	public static Predicate notMatch(String attribute, String pattern) {
		return new NotMatch(new StringAttributeGetterValue(attribute), new RawStringValue(pattern));
	}

	public static Predicate contains(String attribute, String value) {
		return new Contains(new StringAttributeGetterValue(attribute), new RawStringValue(value));
	}

	public static Predicate equals(String attribute, Number value) {
		return new Equals(new NumberAttributeGetterValue(attribute), new RawNumberValue(value));
	}

	public static Predicate equals(StringValue value1, StringValue value2) {
		return new Exactly(value1, value2);
	}

	public static Predicate notEquals(String attribute, Number value) {
		return new NotEquals(new NumberAttributeGetterValue(attribute), new RawNumberValue(value));
	}

	public static Predicate in(String attribute, Collection<?> collection) {
		return new In(new PointerAttributeGetterValue(attribute), collection);
	}

	public static Predicate in(String attribute, CollectionValue collection) {
		return new In(new PointerAttributeGetterValue(attribute), collection);
	}

	public static Predicate in(Number value, CollectionValue collection) {
		return new In(new RawNumberValue(value), collection);
	}

	public static Predicate in(Value<?> value, Collection<?> collection) {
		return new In(value, collection);
	}

	public static Predicate in(Value<?> value, CollectionValue collection) {
		return new In(value, collection);
	}

	public static Predicate in(Number value, Collection<?> collection) {
		return new In(new RawNumberValue(value), collection);
	}

	public static Predicate notIn(String attribute, Collection<?> collection) {
		return new NotIn(new PointerAttributeGetterValue(attribute), collection);
	}

	public static Predicate notIn(Number attribute, CollectionValue collection) {
		return new NotIn(new RawNumberValue(attribute), collection);
	}

	public static Predicate notIn(String attribute, CollectionValue collection) {
		return new NotIn(new PointerAttributeGetterValue(attribute), collection);
	}

	public static Predicate notIn(Value<?> value, CollectionValue collection) {
		return new NotIn(value, collection);
	}

	public static Predicate notIn(Value<?> value, Collection<?> collection) {
		return new NotIn(value, collection);
	}

	public static Predicate notIn(Number value, Collection<?> collection) {
		return new NotIn(new RawNumberValue(value), collection);
	}

	private static final Map<String, SqlPredicateThreadLocal> sqlPredicateThreadLocalBySQL = new ConcurrentHashMap<>();

	private static final SqlPredicateThreadLocal getSqlPredicateThreadLocal(String sql) {
		if (!sqlPredicateThreadLocalBySQL.containsKey(sql)) {
			synchronized (sqlPredicateThreadLocalBySQL) {
				sqlPredicateThreadLocalBySQL.put(sql, new SqlPredicateThreadLocal(sql));
			}
		}
		return sqlPredicateThreadLocalBySQL.get(sql);
	}

	@ThreadSafe
	public static Predicate fromSQL(String sql) {
		return getSqlPredicateThreadLocal(sql).get();
	}
}
