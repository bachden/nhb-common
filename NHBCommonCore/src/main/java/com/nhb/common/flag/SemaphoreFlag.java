package com.nhb.common.flag;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

public class SemaphoreFlag {

	private final int lowerBound;
	private final int upperBound;

	private final AtomicInteger counter = new AtomicInteger(0);
	private final AtomicReference<AtomicInteger> incrementLock = new AtomicReference<>(counter);
	private final AtomicReference<AtomicInteger> decrementLock = new AtomicReference<>(counter);

	public static SemaphoreFlag newDefault() {
		return new SemaphoreFlag(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	public static SemaphoreFlag newDefault(int lowerBound, int upperBound) {
		return new SemaphoreFlag(lowerBound, upperBound);
	}

	public static SemaphoreFlag newWithLowerBound(int lowerBound) {
		return new SemaphoreFlag(lowerBound, Integer.MAX_VALUE);
	}

	public static SemaphoreFlag newWithUpperBound(int upperBound) {
		return new SemaphoreFlag(Integer.MIN_VALUE, upperBound);
	}

	private SemaphoreFlag(int lowerBound, int upperBound) {
		if (upperBound < lowerBound) {
			throw new IllegalArgumentException("upperBound cannot be less than lowerBound");
		}

		this.lowerBound = lowerBound;
		this.upperBound = upperBound;

		if (lowerBound != 0) {
			this.counter.set(lowerBound);
		}
	}

	private void waitForUnlocked(long nanos, AtomicBoolean breakSpinLoop, AtomicReference<AtomicInteger> lock) {
		while (lock.get() == null && !Thread.currentThread().isInterrupted()) {
			if (breakSpinLoop == null || !breakSpinLoop.get()) {
				LockSupport.parkNanos(nanos);
			}
		}
	}

	public void waitForIncrementUnlocked(long nanos, AtomicBoolean breakSpinLoop) {
		this.waitForUnlocked(nanos, breakSpinLoop, incrementLock);
	}

	public void waitForDecrementUnlocked(long nanos, AtomicBoolean breakSpinLoop) {
		this.waitForUnlocked(nanos, breakSpinLoop, decrementLock);
	}

	public boolean lockIncrement() {
		return this.incrementLock.compareAndSet(this.counter, null);
	}

	public int incrementAndGet(AtomicBoolean breakSpinLoop) {
		this.waitForIncrementUnlocked(10, breakSpinLoop);
		return this.counter.accumulateAndGet(1, (currentValue, incrementBy) -> {
			int newValue = currentValue + incrementBy;
			if (newValue > upperBound) {
				return upperBound;
			}
			return newValue;
		});
	}

	public boolean lockDecrement() {
		return this.decrementLock.compareAndSet(this.counter, null);
	}

	public int decrementAndGet(AtomicBoolean breakSpinLoop) {
		this.waitForDecrementUnlocked(10, breakSpinLoop);
		return this.counter.accumulateAndGet(-1, (currentValue, incrementBy) -> {
			int newValue = currentValue + incrementBy;
			if (newValue < lowerBound) {
				return lowerBound;
			}
			return newValue;
		});
	}

	public int get() {
		return this.counter.get();
	}

	private final void lockAndWaitFor(int value, long nanos, AtomicBoolean breakSpinLoop,
			AtomicReference<AtomicInteger> lock) {
		if (lock.compareAndSet(this.counter, null)) {
			this.waitForCounter(value, nanos, breakSpinLoop);
		} else {
			throw new IllegalStateException("Increase lock has been locked");
		}
	}

	public final void lockIncrementAndWaitFor(int value, long nanos, AtomicBoolean breakSpinLoop) {
		this.lockAndWaitFor(value, nanos, breakSpinLoop, incrementLock);
	}

	public final void lockDecrementAndWaitFor(int value, long nanos, AtomicBoolean breakSpinLoop) {
		this.lockAndWaitFor(value, nanos, breakSpinLoop, decrementLock);
	}

	private final void waitForCounter(int value, long nanos, AtomicBoolean breakSpinLoop) {
		while (this.counter.get() != value) {
			if (breakSpinLoop == null || !breakSpinLoop.get()) {
				LockSupport.parkNanos(nanos);
			}
		}
	}

	public boolean unlockIncrement() {
		return this.incrementLock.compareAndSet(null, this.counter);
	}

	public boolean unlockDecrement() {
		return this.decrementLock.compareAndSet(null, this.counter);
	}
}
