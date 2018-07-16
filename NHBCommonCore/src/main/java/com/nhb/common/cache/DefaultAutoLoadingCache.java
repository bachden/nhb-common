package com.nhb.common.cache;

import java.util.Map;
import java.util.function.Function;

import org.cliffc.high_scale_lib.NonBlockingHashMap;

import com.nhb.common.flag.MutexFlag;

import lombok.Setter;

class DefaultAutoLoadingCache<K, V> extends DefaultLocalCache<K, V> implements AutoLoadingCache<K, V> {

	@Setter
	private Function<K, V> loader;

	private final Map<K, MutexFlag> loadingBarriers = new NonBlockingHashMap<>();

	@Override
	protected void onClean() {
		this.loadingBarriers.clear();
	}

	private MutexFlag ensureLoadingBarrier(K key) {
		MutexFlag result = loadingBarriers.get(key);
		if (result == null) {
			result = new MutexFlag();
			MutexFlag old = this.loadingBarriers.putIfAbsent(key, result);
			if (old != null) {
				result = old;
			}
		}
		return result;
	}

	@Override
	public V get(K key) {
		if (this.stopFlag.get()) {
			throw new IllegalStateException("Local cache is being shutted down");
		}
		try {
			return this.acquireExecute(() -> {
				if (!this.unsafeContainsKey(key)) {
					ensureLoadingBarrier(key).waitToApplyThenReset(() -> {
						if (!this.unsafeContainsKey(key)) {
							this.unsafePut(key, this.loader.apply(key));
						}
					}, this.stopFlag);
				}
				return this.unsafeGet(key);
			});
		} finally {
			this.releaseExecute();
		}
	}
}
