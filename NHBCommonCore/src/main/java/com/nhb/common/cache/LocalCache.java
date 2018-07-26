package com.nhb.common.cache;

import java.util.Map.Entry;

public interface LocalCache<K, V> extends Iterable<Entry<K, V>> {

	static <K, V> LocalCache<K, V> newDefault() {
		return new DefaultLocalCache<>();
	}

	static <K, V> LocalCache<K, V> newDefault(long ttlMillis, long interval) {
		LocalCache<K, V> result = newDefault();
		result.setTimeToLive(ttlMillis);
		result.setInterval(interval);
		return result;
	}

	V get(K key);

	V put(K key, V value);

	boolean containsKey(K key);

	/**
	 * set time interval to check for TTL and clear cache
	 * 
	 * @param intervalMillis
	 */
	void setInterval(long intervalMillis);

	/**
	 * time to live should be multiple by intervalMillis
	 * 
	 * @param ttlMillis
	 */
	void setTimeToLive(long ttlMillis);

	void start();

	void stop();
}
