package com.nhb.common.utils;

import com.nhb.common.annotations.NotThreadSafe;

@NotThreadSafe
public class TimeWatcher {

	private long startTime;
	private long lastResult = 0;

	public TimeWatcher reset() {
		this.startTime = System.nanoTime();
		return this;
	}

	public long endLapNano() {
		lastResult = this.getElapsedNano();
		this.reset();
		return lastResult;
	}

	public double endLapMicro() {
		return Double.valueOf(this.endLapNano()) / 1e3;
	}

	public double endLapMillis() {
		return Double.valueOf(this.endLapNano()) / 1e6;
	}

	public double endLapSeconds() {
		return Double.valueOf(this.endLapNano()) / 1e9;
	}

	public long getLastResultNano() {
		return lastResult;
	}

	public double getLastResultMicro() {
		return Double.valueOf(lastResult) / 1e3;
	}

	public double getLastResultMillis() {
		return Double.valueOf(lastResult) / 1e6;
	}

	public double getLastResultSeconds() {
		return Double.valueOf(lastResult) / 1e6;
	}

	public long getElapsedNano() {
		return System.nanoTime() - startTime;
	}

	public double getElapsedMicro() {
		return Double.valueOf(this.getElapsedNano()) / 1e3;
	}

	public double getElapsedMillis() {
		return this.getElapsedNano() / 1e6;
	}

	public double getElapsedSeconds() {
		return this.getElapsedNano() / 1e9;
	}
}
