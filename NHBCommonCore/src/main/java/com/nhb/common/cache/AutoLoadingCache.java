package com.nhb.common.cache;

import java.util.function.Function;

public interface AutoLoadingCache<K, V> extends LocalCache<K, V> {

	static <K, V> AutoLoadingCache<K, V> newDefault() {
		return new DefaultAutoLoadingCache<K, V>();
	}

	static <K, V> AutoLoadingCache<K, V> newDefault(Function<K, V> dataSupplier) {
		AutoLoadingCache<K, V> result = newDefault();
		result.setLoader(dataSupplier);
		return result;
	}

	static <K, V> AutoLoadingCache<K, V> newDefault(Function<K, V> dataSupplier, long ttlMillis, long intervalMillis) {
		AutoLoadingCache<K, V> result = newDefault(ttlMillis, intervalMillis);
		result.setLoader(dataSupplier);
		return result;
	}

	static <K, V> AutoLoadingCache<K, V> newDefault(long ttlMillis, long intervalMillis) {
		AutoLoadingCache<K, V> result = newDefault();
		result.setTimeToLive(ttlMillis);
		result.setInterval(intervalMillis);
		return result;
	}

	void setLoader(Function<K, V> dataSupplier);
}
