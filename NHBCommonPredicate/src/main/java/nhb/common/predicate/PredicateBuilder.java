package nhb.common.predicate;

import java.util.ArrayList;
import java.util.List;

public final class PredicateBuilder {

	private static final Predicate DEFAULT_PREDICATE = new Predicate() {

		private static final long serialVersionUID = -3683985384796098717L;

		@Override
		public boolean apply(Object obj) {
			return true;
		}
	};

	private List<Predicate> predicates;
	private String attribute;

	PredicateBuilder() {
		this.predicates = new ArrayList<>();
	}

	public static FilteredObject newFilteredObject() {
		return new FilteredObject(new PredicateBuilder());
	}

	List<Predicate> getPredicates() {
		return this.predicates;
	}

	String getAttribute() {
		return attribute;
	}

	void setAttribute(String attribute) {
		this.attribute = attribute;
	}

	public PredicateBuilder and(PredicateBuilder pb) {
		if (pb != this) {
			throw new IllegalArgumentException("pb must be exactly this");
		}
		Predicate second = this.predicates.remove(this.predicates.size() - 1);
		Predicate first = this.predicates.remove(this.predicates.size() - 1);
		this.predicates.add(Predicates.and(first, second));
		return this;
	}

	public PredicateBuilder or(PredicateBuilder pb) {
		if (pb != this) {
			throw new IllegalArgumentException("pb must be the same with this");
		}
		Predicate second = this.predicates.remove(this.predicates.size() - 1);
		Predicate first = this.predicates.remove(this.predicates.size() - 1);
		this.predicates.add(Predicates.or(first, second));
		return this;
	}

	public void clear() {
		this.predicates = new ArrayList<>();
		this.attribute = null;
	}

	public Predicate build() {
		if (this.getAttribute() != null) {
			throw new IllegalStateException("The last attribute has been set but not used yet");
		} else if (this.predicates == null || this.predicates.size() == 0) {
			return DEFAULT_PREDICATE;
		}
		return this.predicates.get(0);
	}

	Predicate poll() {
		return this.getPredicates().remove(this.getPredicates().size() - 1);
	}

	void push(Predicate predicate) {
		this.getPredicates().add(predicate);
	}
}
