package nhb.common.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import nhb.common.vo.ByteArrayWrapper;

public class MapUtils {

	/**
	 * This method use for java's default map class (HashMap,
	 * ConcurrentHashMap...), when the key is byte[]
	 * 
	 * @param map
	 *            to be converted
	 * @return converted map with key is instance of ByteArrayWrapper, null if
	 *         map == null
	 */
	public static final <V> Map<ByteArrayWrapper, V> convertMapWithByteArrayKey(Map<byte[], V> map) {
		if (map != null) {
			Map<ByteArrayWrapper, V> result = (map instanceof ConcurrentHashMap)
					? new ConcurrentHashMap<ByteArrayWrapper, V>() : new HashMap<ByteArrayWrapper, V>();
			for (Map.Entry<byte[], V> entry : map.entrySet()) {
				result.put(new ByteArrayWrapper(entry.getKey()), entry.getValue());
			}
			return result;
		}
		return null;
	}
}
