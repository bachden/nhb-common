package com.nhb.common.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.nhb.common.vo.ByteArrayWrapper;

@SuppressWarnings("deprecation")
public class MapUtils {

	/**
	 * This method use for java's default map class (HashMap, ConcurrentHashMap...),
	 * when the key is byte[]
	 * 
	 * @param map
	 *            to be converted
	 * @return converted map with key is instance of ByteArray using java safe hash
	 *         code calculator, null if map == null
	 */
	public static final <V> Map<ByteArrayWrapper, V> convertMapWithByteArrayKey(Map<byte[], V> map) {
		if (map != null) {
			Map<ByteArrayWrapper, V> result = (map instanceof ConcurrentHashMap)
					? new ConcurrentHashMap<ByteArrayWrapper, V>()
					: new HashMap<ByteArrayWrapper, V>();
			for (Map.Entry<byte[], V> entry : map.entrySet()) {
				result.put(ByteArrayWrapper.newInstanceWithJavaSafeHashCodeCalculator(entry.getKey()),
						entry.getValue());
			}
			return result;
		}
		return null;
	}

}
