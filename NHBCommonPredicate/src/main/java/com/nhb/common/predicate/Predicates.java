package com.nhb.common.predicate;

import java.util.Collection;

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
import com.nhb.common.predicate.numeric.LessEqual;
import com.nhb.common.predicate.numeric.LessThan;
import com.nhb.common.predicate.numeric.NotBetween;
import com.nhb.common.predicate.numeric.NotBetweenIncludeBoth;
import com.nhb.common.predicate.numeric.NotBetweenIncludeLeft;
import com.nhb.common.predicate.numeric.NotBetweenIncludeRight;
import com.nhb.common.predicate.numeric.NotEqual;
import com.nhb.common.predicate.pointer.IsNotNull;
import com.nhb.common.predicate.pointer.IsNull;
import com.nhb.common.predicate.sql.SqlPredicateParser;
import com.nhb.common.predicate.text.Contains;
import com.nhb.common.predicate.text.Exactly;
import com.nhb.common.predicate.text.ExactlyIgnoreCase;
import com.nhb.common.predicate.text.Match;
import com.nhb.common.predicate.text.NotMatch;
import com.nhb.common.predicate.value.ObjectDependenceValue;
import com.nhb.common.predicate.value.Value;
import com.nhb.common.predicate.value.getter.BooleanAttributeGetter;
import com.nhb.common.predicate.value.getter.NumberAttributeGetter;
import com.nhb.common.predicate.value.getter.PointerAttributeGetter;
import com.nhb.common.predicate.value.getter.StringAttributeGetter;
import com.nhb.common.predicate.value.primitive.NumberValue;
import com.nhb.common.predicate.value.primitive.StringValue;

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
			v1 = new ObjectDependenceValue<Boolean>() {

				@Override
				public Boolean get() {
					return p1.apply(this.getObject());
				}

				@Override
				public String toString() {
					return p1.toString();
				}

			};
		}
		if (p2 instanceof Value) {
			v2 = (Value<Boolean>) p2;
		} else {
			v2 = new ObjectDependenceValue<Boolean>() {

				@Override
				public Boolean get() {
					return p2.apply(this.getObject());
				}

				@Override
				public String toString() {
					return p2.toString();
				}
			};
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
			v1 = new ObjectDependenceValue<Boolean>() {

				@Override
				public Boolean get() {
					return p1.apply(this.getObject());
				}

				@Override
				public String toString() {
					return p1.toString();
				}
			};
		}
		if (p2 instanceof Value) {
			v2 = (Value<Boolean>) p2;
		} else {
			v2 = new ObjectDependenceValue<Boolean>() {

				@Override
				public Boolean get() {
					return p2.apply(this.getObject());
				}

				@Override
				public String toString() {
					return p2.toString();
				}
			};
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
			return new Not(new ObjectDependenceValue<Boolean>() {

				@Override
				public Boolean get() {
					return predicate.apply(this.getObject());
				}

				@Override
				public String toString() {
					return predicate.toString();
				}
			});
		}
	}

	public static PredicateBuilder not(final PredicateBuilder predicateBuilder) {
		predicateBuilder.push(not(predicateBuilder.poll()));
		return predicateBuilder;
	}

	public static Predicate not(String attribute) {
		return not(new BooleanAttributeGetter(attribute));
	}

	public static Predicate is(Value<Boolean> value) {
		return new Is(value);
	}

	public static Predicate is(String attribute) {
		return is(new BooleanAttributeGetter(attribute));
	}

	public static Predicate is(final Predicate predicate) {
		if (predicate instanceof Value) {
			return new Is((Value<Boolean>) predicate);
		} else {
			return new Is(new ObjectDependenceValue<Boolean>() {

				@Override
				public Boolean get() {
					return predicate.apply(this.getObject());
				}
			});
		}
	}

	public static PredicateBuilder is(final PredicateBuilder predicateBuilder) {
		predicateBuilder.push(is(predicateBuilder.poll()));
		return predicateBuilder;
	}

	// *******
	public static Predicate greaterThan(Value<? extends Number> value, Value<? extends Number> lowerBound) {
		return new GreaterThan(value, lowerBound);
	}

	public static Predicate greaterThan(String attribute, Value<? extends Number> lowerBound) {
		return greaterThan(new NumberAttributeGetter(attribute), lowerBound);
	}

	public static Predicate greaterThan(String attribute, Number value) {
		return greaterThan(new NumberAttributeGetter(attribute), new NumberValue(value));
	}

	public static Predicate greaterOrEquals(Value<? extends Number> value, Value<? extends Number> lowerBound) {
		return new GreaterOrEquals(value, lowerBound);
	}

	public static Predicate greaterEqual(String attribute, Value<? extends Number> lowerBound) {
		return greaterOrEquals(new NumberAttributeGetter(attribute), lowerBound);
	}

	public static Predicate greaterOrEquals(String attribute, Number value) {
		return greaterOrEquals(new NumberAttributeGetter(attribute), new NumberValue(value));
	}

	// *******
	public static Predicate lessThan(Value<? extends Number> value, Value<? extends Number> lowerBound) {
		return new LessThan(value, lowerBound);
	}

	public static Predicate lessThan(String attribute, Value<? extends Number> upperBound) {
		return lessThan(new NumberAttributeGetter(attribute), upperBound);
	}

	public static Predicate lessThan(String attribute, Number value) {
		return lessThan(new NumberAttributeGetter(attribute), new NumberValue(value));
	}

	public static Predicate lessEquals(Value<? extends Number> value, Value<? extends Number> upperBound) {
		return new LessEqual(value, upperBound);
	}

	public static Predicate lessEquals(String attribute, Value<? extends Number> upperBound) {
		return lessEquals(new NumberAttributeGetter(attribute), upperBound);
	}

	public static Predicate lessEquals(String attribute, Number value) {
		return lessEquals(new NumberAttributeGetter(attribute), new NumberValue(value));
	}

	// *******
	public static Predicate between(Value<? extends Number> value, Value<? extends Number> lowerBound,
			Value<? extends Number> upperBound, boolean includeLeft, boolean includeRight) {
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

	public static Predicate between(String attribute, Value<? extends Number> lowerBound,
			Value<? extends Number> upperBound, boolean includeLeft, boolean includeRight) {
		return between(new NumberAttributeGetter(attribute), lowerBound, upperBound, includeLeft, includeRight);
	}

	public static Predicate between(String attribute, Number lowerBound, Number upperBound, boolean includeLeft,
			boolean includeRight) {
		return between(new NumberAttributeGetter(attribute), new NumberValue(lowerBound), new NumberValue(upperBound),
				includeLeft, includeRight);
	}

	// *******
	public static Predicate notBetween(Value<? extends Number> value, Value<? extends Number> lowerBound,
			Value<? extends Number> upperBound, boolean includeLeft, boolean includeRight) {
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

	public static Predicate notBetween(String attribute, Value<? extends Number> lowerBound,
			Value<? extends Number> upperBound, boolean includeLeft, boolean includeRight) {
		return notBetween(new NumberAttributeGetter(attribute), lowerBound, upperBound, includeLeft, includeRight);
	}

	public static Predicate notBetween(String attribute, Number lowerBound, Number upperBound, boolean includeLeft,
			boolean includeRight) {
		return notBetween(new NumberAttributeGetter(attribute), new NumberValue(lowerBound),
				new NumberValue(upperBound), includeLeft, includeRight);
	}

	public static Predicate isNull(String attribute) {
		return new IsNull(new PointerAttributeGetter(attribute));
	}

	public static Predicate notNull(String attribute) {
		return new IsNotNull(new PointerAttributeGetter(attribute));
	}

	public static Predicate isNull() {
		return new IsNull();
	}

	public static Predicate notNull() {
		return new IsNotNull();
	}

	public static Predicate exactly(String attribute, String value) {
		return new Exactly(new StringAttributeGetter(attribute), new StringValue(value));
	}

	public static Predicate exactlyIgnoreCase(String attribute, String value) {
		return new ExactlyIgnoreCase(new StringAttributeGetter(attribute), new StringValue(value));
	}

	public static Predicate match(String attribute, String pattern) {
		return new Match(new StringAttributeGetter(attribute), new StringValue(pattern));
	}

	public static Predicate notMatch(String attribute, String pattern) {
		return new NotMatch(new StringAttributeGetter(attribute), new StringValue(pattern));
	}

	public static Predicate contains(String attribute, String value) {
		return new Contains(new StringAttributeGetter(attribute), new StringValue(value));
	}

	public static Predicate equals(String attribute, Number value) {
		return new Equals(new NumberAttributeGetter(attribute), new NumberValue(value));
	}

	public static Predicate notEquals(String attribute, Number value) {
		return new NotEqual(new NumberAttributeGetter(attribute), new NumberValue(value));
	}

	public static Predicate in(String attribute, Collection<?> collection) {
		return new In(new PointerAttributeGetter(attribute), collection);
	}

	public static Predicate notIn(String attribute, Collection<?> collection) {
		return new NotIn(new PointerAttributeGetter(attribute), collection);
	}

	public static Predicate fromSQL(String sql) {
		return SqlPredicateParser.parse(sql);
	}
}
