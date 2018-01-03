package com.nhb.common.hash;

import net.jpountz.xxhash.XXHash32;

public class XXHash32BinaryHashCodeCalculator implements BinaryHashCodeCalculator {

	public static final int DEFAULT_SEED = 0x9747b28c;

	private final XXHash32 hasher;
	private final int seed;

	public XXHash32BinaryHashCodeCalculator(XXHash32 hasher) {
		this(hasher, DEFAULT_SEED);
	}

	public XXHash32BinaryHashCodeCalculator(XXHash32 hasher, int seed) {
		if (hasher == null) {
			throw new NullPointerException("Hasher cannot be null");
		}
		this.hasher = hasher;
		this.seed = seed;
	}

	@Override
	public int calcHashCode(byte[] bytes) {
		return hasher.hash(bytes, 0, bytes.length, this.seed);
	}
}