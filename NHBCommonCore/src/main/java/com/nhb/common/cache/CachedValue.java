package com.nhb.common.cache;

import lombok.Getter;

public class CachedValue<V> {

	@Getter
	private long lastUpdatedTimestamp = 0;

	private final V value;

	public CachedValue(V value) {
		this.value = value;
		this.lastUpdatedTimestamp = System.currentTimeMillis();
	}

	public V get() {
		return this.value;
	}
}
