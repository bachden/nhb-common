package com.nhb.common.vo;

import static com.nhb.common.hash.BinaryHashCodeCalculator.DEFAULT;
import static com.nhb.common.hash.BinaryHashCodeCalculator.XXHASH32_JAVA_SAFE;
import static com.nhb.common.hash.BinaryHashCodeCalculator.XXHASH32_JAVA_UNSAFE;
import static com.nhb.common.hash.BinaryHashCodeCalculator.XXHASH32_JNI;

import java.io.Serializable;
import java.util.Arrays;

import com.nhb.common.hash.BinaryHashCodeCalculator;

public class ByteArray implements Serializable {
	private static final long serialVersionUID = 1L;

	public static final ByteArray newInstance(byte[] source) {
		return new ByteArray(source);
	}

	public static final ByteArray newInstance(byte[] source, BinaryHashCodeCalculator hashCodeCalculator) {
		return new ByteArray(source, hashCodeCalculator);
	}

	public static final ByteArray newInstanceWithJavaSafeHashCodeCalculator(byte[] source) {
		return ByteArray.newInstance(source, XXHASH32_JAVA_SAFE);
	}

	public static final ByteArray newInstanceWithJavaUnsafeHashCodeCalculator(byte[] source) {
		return ByteArray.newInstance(source, XXHASH32_JAVA_UNSAFE);
	}

	public static final ByteArray newInstanceWithJNIHashCodeCalculator(byte[] source) {
		return ByteArray.newInstance(source, XXHASH32_JNI);
	}

	private final byte[] source;

	private int hashCode = -1;

	private transient final BinaryHashCodeCalculator hashCodeCalculator;

	public ByteArray(byte[] source) {
		this(source, DEFAULT);
	}

	public ByteArray(byte[] source, BinaryHashCodeCalculator hashCodeCalculator) {
		if (source == null) {
			throw new NullPointerException("Source byte[] cannot be null");
		}
		if (hashCodeCalculator == null) {
			throw new NullPointerException("Hash code caculator cannot be null");
		}
		this.source = source;
		this.hashCodeCalculator = hashCodeCalculator;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof ByteArray) {
			return Arrays.equals(source, ((ByteArray) other).source);
		} else if (other instanceof byte[]) {
			return Arrays.equals(source, (byte[]) other);
		}
		return false;
	}

	public byte[] getSource() {
		return this.source;
	}

	@Override
	public int hashCode() {
		if (hashCode == -1) {
			hashCode = this.hashCodeCalculator.calcHashCode(this.source);
		}
		return hashCode;
	}

	@Override
	public String toString() {
		return Arrays.toString(this.source);
	}

}
