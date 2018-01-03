package com.nhb.common.hash;

import net.jpountz.xxhash.XXHashFactory;

public interface BinaryHashCodeCalculator extends HashCodeCalculator<byte[]> {

	static final BinaryHashCodeCalculator DEFAULT = new BinaryHashCodeCalculator() {

		@Override
		public int calcHashCode(byte[] bytes) {
			if (bytes == null)
				return 0;

			int result = 1;
			for (byte element : bytes)
				result = (result << 5) - result + element;

			return result;
		}
	};

	static final BinaryHashCodeCalculator REVERSED = new BinaryHashCodeCalculator() {

		@Override
		public int calcHashCode(byte[] bytes) {
			if (bytes == null)
				return 0;

			int result = 1;
			for (int i = bytes.length - 1; i >= 0; i--)
				result = (result << 5) - result + bytes[i];

			return result;
		}
	};

	static final BinaryHashCodeCalculator XXHASH32_JAVA_UNSAFE = new XXHash32BinaryHashCodeCalculator(
			XXHashFactory.unsafeInstance().hash32());

	static final BinaryHashCodeCalculator XXHASH32_JAVA_SAFE = new XXHash32BinaryHashCodeCalculator(
			XXHashFactory.safeInstance().hash32());

	static final BinaryHashCodeCalculator XXHASH32_JNI = new XXHash32BinaryHashCodeCalculator(
			XXHashFactory.fastestInstance().hash32());
}
