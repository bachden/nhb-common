package com.nhb.common.utils;

import com.nhb.common.annotations.NotThreadSafe;

@NotThreadSafe
public class TimeWatcher {

	private long startTime;

	public TimeWatcher reset() {
		this.startTime = System.nanoTime();
		return this;
	}

	public long endLapNano() {
		long result = this.getNano();
		this.reset();
		return result;
	}

	public long endLapMicro() {
		return this.endLapNano() / 1000l;
	}

	public long endLapMillis() {
		return this.endLapMicro() / 1000l;
	}

	public long endLapSeconds() {
		return this.endLapMillis() / 1000l;
	}

	public long getNano() {
		return System.nanoTime() - startTime;
	}

	public long getMicro() {
		return this.getNano() / 1000l;
	}

	public long getMillis() {
		return this.getMicro() / 1000l;
	}

	public long getSeconds() {
		return this.getMillis() / 1000l;
	}
}
