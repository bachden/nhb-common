package com.nhb.common.cache;

import java.util.concurrent.atomic.AtomicReference;

import lombok.Getter;

public class CachedValue<V> {

	@Getter
	private long lastUpdatedTimestamp = 0;

	private final AtomicReference<V> ref = new AtomicReference<V>(null);

	public CachedValue(V value) {
		this.ref.set(value);
		this.lastUpdatedTimestamp = System.currentTimeMillis();
	}

	public V get() {
		return this.ref.get();
	}
}
