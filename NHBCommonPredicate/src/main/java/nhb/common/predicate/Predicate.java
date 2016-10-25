package nhb.common.predicate;

import java.io.Serializable;

public interface Predicate extends Serializable {

	boolean apply(Object obj);
}
