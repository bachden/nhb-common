package nhb.common.predicate;

import java.util.Collection;

import nhb.common.predicate.array.In;
import nhb.common.predicate.array.NotIn;
import nhb.common.predicate.logic.And;
import nhb.common.predicate.logic.Is;
import nhb.common.predicate.logic.Not;
import nhb.common.predicate.logic.Or;
import nhb.common.predicate.numeric.Between;
import nhb.common.predicate.numeric.BetweenIncludeBoth;
import nhb.common.predicate.numeric.BetweenIncludeLeft;
import nhb.common.predicate.numeric.BetweenIncludeRight;
import nhb.common.predicate.numeric.Equal;
import nhb.common.predicate.numeric.GreaterEqual;
import nhb.common.predicate.numeric.GreaterThan;
import nhb.common.predicate.numeric.LessEqual;
import nhb.common.predicate.numeric.LessThan;
import nhb.common.predicate.numeric.NotEqual;
import nhb.common.predicate.pointer.IsNotNull;
import nhb.common.predicate.pointer.IsNull;
import nhb.common.predicate.text.Contain;
import nhb.common.predicate.text.Exactly;
import nhb.common.predicate.text.ExactlyIgnoreCase;
import nhb.common.predicate.text.Match;
import nhb.common.predicate.text.NotMatch;
import nhb.common.predicate.value.ObjectDependenceValue;
import nhb.common.predicate.value.Value;
import nhb.common.predicate.value.getter.BooleanAttributeGetter;
import nhb.common.predicate.value.getter.NumberAttributeGetter;
import nhb.common.predicate.value.getter.PointerAttributeGetter;
import nhb.common.predicate.value.getter.StringAttributeGetter;
import nhb.common.predicate.value.primitive.NumberValue;
import nhb.common.predicate.value.primitive.StringValue;

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
			};
		}
		return new Or(v1, v2);
	}

	public static Predicate isNot(Value<Boolean> value) {
		return new Not(value);
	}

	public static Predicate isNot(final Predicate filter) {
		if (filter instanceof Value) {
			return new Not((Value<Boolean>) filter);
		} else {
			return new Not(new ObjectDependenceValue<Boolean>() {

				@Override
				public Boolean get() {
					return filter.apply(this.getObject());
				}
			});
		}
	}

	public static PredicateBuilder isNot(final PredicateBuilder predicateBuilder) {
		predicateBuilder.push(isNot(predicateBuilder.poll()));
		return predicateBuilder;
	}

	public static Predicate isNot(String attribute) {
		return isNot(new BooleanAttributeGetter(attribute));
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

	public static Predicate greaterEqual(Value<? extends Number> value, Value<? extends Number> lowerBound) {
		return new GreaterEqual(value, lowerBound);
	}

	public static Predicate greaterEqual(String attribute, Value<? extends Number> lowerBound) {
		return greaterEqual(new NumberAttributeGetter(attribute), lowerBound);
	}

	public static Predicate greaterEqual(String attribute, Number value) {
		return greaterEqual(new NumberAttributeGetter(attribute), new NumberValue(value));
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

	public static Predicate lessEqual(Value<? extends Number> value, Value<? extends Number> upperBound) {
		return new LessEqual(value, upperBound);
	}

	public static Predicate lessEqual(String attribute, Value<? extends Number> upperBound) {
		return lessEqual(new NumberAttributeGetter(attribute), upperBound);
	}

	public static Predicate lessEqual(String attribute, Number value) {
		return lessEqual(new NumberAttributeGetter(attribute), new NumberValue(value));
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

	public static Predicate isNull(String attribute) {
		return new IsNull(new PointerAttributeGetter(attribute));
	}

	public static Predicate isNotNull(String attribute) {
		return new IsNotNull(new PointerAttributeGetter(attribute));
	}

	public static Predicate isNull() {
		return new IsNull();
	}

	public static Predicate isNotNull() {
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

	public static Predicate contain(String attribute, String value) {
		return new Contain(new StringAttributeGetter(attribute), new StringValue(value));
	}

	public static Predicate equal(String attribute, Number value) {
		return new Equal(new NumberAttributeGetter(attribute), new NumberValue(value));
	}

	public static Predicate notEqual(String attribute, Number value) {
		return new NotEqual(new NumberAttributeGetter(attribute), new NumberValue(value));
	}

	public static Predicate in(String attribute, Collection<?> collection) {
		return new In(new PointerAttributeGetter(attribute), collection);
	}

	public static Predicate notIn(String attribute, Collection<?> collection) {
		return new NotIn(new PointerAttributeGetter(attribute), collection);
	}

}
