package nhb.common.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapTuple<K, V> extends HashMap<K, V> {

	private static final long serialVersionUID = 1170411444597602921L;

	public MapTuple() {
		super();
	}

	@SuppressWarnings("unchecked")
	public MapTuple(Object... keyValues) {
		if (keyValues != null && (keyValues.length & 1) == 0) {
			for (int i = 0; i < keyValues.length - 1; i += 2) {
				this.put((K) keyValues[i], (V) keyValues[i + 1]);
			}
		}
	}

	public Object[] toTuple() {
		List<Object> result = new ArrayList<Object>();
		for (Entry<K, V> entry : this.entrySet()) {
			result.add(entry.getKey());
			result.add(entry.getValue());
		}
		return result.toArray(new Object[result.size()]);
	}
}
