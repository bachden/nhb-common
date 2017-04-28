package com.nhb.common.predicate;

import java.util.Collection;

import com.nhb.common.predicate.logic.Not;
import com.nhb.common.predicate.value.Value;
import com.nhb.common.predicate.value.primitive.NumberValue;

public class FilteredObject {

	private PredicateBuilder filterBuilder;

	FilteredObject(PredicateBuilder pb) {
		this.filterBuilder = pb;
	}

	private PredicateBuilder addPredicate(Predicate predicate) {
		filterBuilder.getPredicates().add(predicate);
		this.filterBuilder.setAttribute(null);
		return filterBuilder;
	}

	public FilteredObject get(String attribute) {
		if (this.filterBuilder.getAttribute() != null) {
			throw new IllegalStateException("cannot get another attribute when the previous has not been used");
		}
		this.filterBuilder.setAttribute(attribute);
		return this;
	}

	public PredicateBuilder is(String attribute) {
		if (this.filterBuilder.getAttribute() != null) {
			throw new IllegalStateException("Last attribute has been set and not used yet");
		}
		return this.addPredicate(Predicates.is(attribute));
	}

	public PredicateBuilder isNot(String attribute) {
		if (this.filterBuilder.getAttribute() != null) {
			throw new IllegalStateException("Last attribute has been set and not used yet");
		}
		return this.addPredicate(Predicates.not(attribute));
	}

	public PredicateBuilder lessThan(Number value) {
		if (this.filterBuilder.getAttribute() == null) {
			throw new IllegalStateException(
					"Attribute must be set before compare with other value, use `EntryObject.get(attribute)` to do that");
		}
		return this.addPredicate(Predicates.lessThan(this.filterBuilder.getAttribute(), value));
	}

	public PredicateBuilder lessOrEquals(Number value) {
		if (this.filterBuilder.getAttribute() == null) {
			throw new IllegalStateException(
					"Attribute must be set before compare with other value, use `EntryObject.get(attribute)` to do that");
		}
		return this.addPredicate(Predicates.lessEquals(this.filterBuilder.getAttribute(), value));
	}

	public PredicateBuilder greaterThan(Number value) {
		if (this.filterBuilder.getAttribute() == null) {
			throw new IllegalStateException(
					"Attribute must be set before compare with other value, use `EntryObject.get(attribute)` to do that");
		}
		return this.addPredicate(Predicates.greaterThan(this.filterBuilder.getAttribute(), value));
	}

	public PredicateBuilder greaterOrEquals(Number value) {
		if (this.filterBuilder.getAttribute() == null) {
			throw new IllegalStateException(
					"Attribute must be set before compare with other value, use `EntryObject.get(attribute)` to do that");
		}
		return this.addPredicate(Predicates.greaterOrEquals(this.filterBuilder.getAttribute(), value));
	}

	public PredicateBuilder equal(Number value) {
		if (this.filterBuilder.getAttribute() == null) {
			throw new IllegalStateException(
					"Attribute must be set before compare with other value, use `EntryObject.get(attribute)` to do that");
		}
		return this.addPredicate(Predicates.equals(this.filterBuilder.getAttribute(), value));
	}

	public PredicateBuilder exactly(String value) {
		if (this.filterBuilder.getAttribute() == null) {
			throw new IllegalStateException(
					"Attribute must be set before compare with other value, use `EntryObject.get(attribute)` to do that");
		}
		return this.addPredicate(Predicates.exactly(this.filterBuilder.getAttribute(), value));
	}

	public PredicateBuilder exactlyIgnoreCase(String value) {
		if (this.filterBuilder.getAttribute() == null) {
			throw new IllegalStateException(
					"Attribute must be set before compare with other value, use `EntryObject.get(attribute)` to do that");
		}
		return this.addPredicate(Predicates.exactlyIgnoreCase(this.filterBuilder.getAttribute(), value));
	}

	public PredicateBuilder match(String pattern) {
		if (this.filterBuilder.getAttribute() == null) {
			throw new IllegalStateException(
					"Attribute must be set before compare with other value, use `EntryObject.get(attribute)` to do that");
		}
		return this.addPredicate(Predicates.match(this.filterBuilder.getAttribute(), pattern));
	}

	public PredicateBuilder notMatch(String pattern) {
		if (this.filterBuilder.getAttribute() == null) {
			throw new IllegalStateException(
					"Attribute must be set before compare with other value, use `EntryObject.get(attribute)` to do that");
		}
		return this.addPredicate(Predicates.notMatch(this.filterBuilder.getAttribute(), pattern));
	}

	public PredicateBuilder contain(String value) {
		if (this.filterBuilder.getAttribute() == null) {
			throw new IllegalStateException(
					"Attribute must be set before compare with other value, use `EntryObject.get(attribute)` to do that");
		}
		return this.addPredicate(Predicates.contains(this.filterBuilder.getAttribute(), value));
	}

	@SuppressWarnings("unchecked")
	public PredicateBuilder notContain(String string) {
		return this.addPredicate(new Not((Value<Boolean>) Predicates.contains(this.filterBuilder.getAttribute(), string)));
	}

	public PredicateBuilder isNull() {
		if (this.filterBuilder.getAttribute() == null) {
			return this.addPredicate(Predicates.isNull());
		}
		return this.addPredicate(Predicates.isNull(this.filterBuilder.getAttribute()));
	}

	public PredicateBuilder isNotNull() {
		if (this.filterBuilder.getAttribute() == null) {
			return this.addPredicate(Predicates.notNull());
		}
		return this.addPredicate(Predicates.notNull(this.filterBuilder.getAttribute()));
	}

	public PredicateBuilder notEqual(Number value) {
		if (this.filterBuilder.getAttribute() == null) {
			throw new IllegalStateException(
					"Attribute must be set before compare with other value, use `EntryObject.get(attribute)` to do that");
		}
		return this.addPredicate(Predicates.notEquals(this.filterBuilder.getAttribute(), value));
	}

	public PredicateBuilder between(Number lowerBound, Number upperBound) {
		return this.between(lowerBound, upperBound, false, false);
	}

	public PredicateBuilder between(Number lowerBound, Number upperBound, boolean includeLeft, boolean includeRight) {
		if (this.filterBuilder.getAttribute() == null) {
			throw new IllegalStateException(
					"Attribute must be set before compare with other value, use `EntryObject.get(attribute)` to do that");
		}
		return this.addPredicate(Predicates.between(this.filterBuilder.getAttribute(), new NumberValue(lowerBound),
				new NumberValue(upperBound), includeLeft, includeRight));
	}

	public PredicateBuilder in(Collection<?> collection) {
		if (this.filterBuilder.getAttribute() == null) {
			throw new IllegalStateException(
					"Attribute must be set before compare with other value, use `EntryObject.get(attribute)` to do that");
		}
		return this.addPredicate(Predicates.in(this.filterBuilder.getAttribute(), collection));
	}

	public PredicateBuilder notIn(Collection<?> collection) {
		if (this.filterBuilder.getAttribute() == null) {
			throw new IllegalStateException(
					"Attribute must be set before compare with other value, use `EntryObject.get(attribute)` to do that");
		}
		return this.addPredicate(Predicates.notIn(this.filterBuilder.getAttribute(), collection));
	}

	public PredicateBuilder clear() {
		this.filterBuilder.clear();
		return this.filterBuilder;
	}
}
