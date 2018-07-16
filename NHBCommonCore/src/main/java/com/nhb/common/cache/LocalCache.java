package com.nhb.common.cache;

public interface LocalCache<K, V> {

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
