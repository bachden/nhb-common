package com.nhb.common.cache;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import org.cliffc.high_scale_lib.NonBlockingHashMap;

import com.nhb.common.Loggable;
import com.nhb.common.flag.SemaphoreFlag;

import lombok.Setter;

class DefaultLocalCache<K, V> implements LocalCache<K, V>, Loggable {

	private static final AtomicInteger instanceId = new AtomicInteger(0);

	private final Map<K, CachedValue<V>> source = new NonBlockingHashMap<>();

	@Setter
	private volatile long timeToLive = 300000; // 300,000 ms == 5 minutes to clear cache

	@Setter
	private volatile long interval = 60000;

	protected final AtomicBoolean stopFlag = new AtomicBoolean(true);

	private Thread cleaner = null;

	private final SemaphoreFlag cleaningFlag = SemaphoreFlag.newWithLowerBound(0);

	private void runClearCache() {
		while (!Thread.currentThread().isInterrupted() && !this.stopFlag.get()) {
			try {
				Thread.sleep(interval);
			} catch (InterruptedException e) {
				break;
			}
			try {
				this.clearCache();
			} catch (Exception ex) {
				getLogger().error("An error occurs while execute clear cache", ex);
			}
		}
	}

	private void clearCache() {
		try {
			this.cleaningFlag.lockIncrementAndWaitFor(0, 5, stopFlag);
			long curr = System.currentTimeMillis();
			Set<K> tobeRemoved = new HashSet<>();
			for (Entry<K, CachedValue<V>> entry : this.source.entrySet()) {
				if (curr - entry.getValue().getLastUpdatedTimestamp() > timeToLive) {
					tobeRemoved.add(entry.getKey());
				}
			}
			if (tobeRemoved.size() > 0) {
				for (K key : tobeRemoved) {
					this.source.remove(key);
				}
			}
			this.onClean();
		} finally {
			cleaningFlag.unlockIncrement();
		}
	}

	protected void onClean() {
		// do nothing
	}

	protected <T> T acquireExecute(Supplier<T> supplier) {
		this.cleaningFlag.incrementAndGet(stopFlag);
		return supplier.get();
	}

	protected void releaseExecute() {
		this.cleaningFlag.decrementAndGet(stopFlag);
	}

	protected <T> T acquireExecuteThenRelease(Supplier<T> supplier) {
		try {
			return this.acquireExecute(supplier);
		} finally {
			this.releaseExecute();
		}
	}

	protected V unsafeGet(K key) {
		CachedValue<V> element = this.source.get(key);
		return element == null ? null : element.get();
	}

	@Override
	public V get(K key) {
		if (this.stopFlag.get()) {
			throw new IllegalStateException("Local cache is being stopped");
		}
		return this.acquireExecuteThenRelease(() -> {
			return this.unsafeGet(key);
		});
	}

	protected V unsafePut(K key, V value) {
		CachedValue<V> existing = this.source.put(key, new CachedValue<V>(value));
		return existing == null ? null : existing.get();
	}

	@Override
	public V put(K key, V value) {
		if (this.stopFlag.get()) {
			throw new IllegalStateException("Local cache is being stopped");
		}
		return this.acquireExecuteThenRelease(() -> {
			return this.unsafePut(key, value);
		});
	}

	protected boolean unsafeContainsKey(K key) {
		return this.source.containsKey(key);
	}

	@Override
	public boolean containsKey(K key) {
		if (this.stopFlag.get()) {
			throw new IllegalStateException("Local cache is being stopped");
		}
		return this.acquireExecuteThenRelease(() -> {
			return this.unsafeContainsKey(key);
		});
	}

	@Override
	public void start() {
		if (this.stopFlag.compareAndSet(true, false)) {
			this.cleaner = new Thread(this::runClearCache, "Local cache cleaner #" + instanceId.incrementAndGet());
			this.cleaner.start();
		}
	}

	@Override
	public void stop() {
		if (this.stopFlag.compareAndSet(false, true)) {
			this.cleaner.interrupt();
			this.cleaner = null;
		}
	}
}
