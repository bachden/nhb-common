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

	void setLoader(Function<K, V> dataSupplier);
}
