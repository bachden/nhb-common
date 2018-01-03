package com.nhb.common.vo;

import static com.nhb.common.hash.BinaryHashCodeCalculator.DEFAULT;
import static com.nhb.common.hash.BinaryHashCodeCalculator.XXHASH32_JAVA_SAFE;
import static com.nhb.common.hash.BinaryHashCodeCalculator.XXHASH32_JAVA_UNSAFE;
import static com.nhb.common.hash.BinaryHashCodeCalculator.XXHASH32_JNI;

import java.io.Serializable;
import java.util.Arrays;

import com.nhb.common.hash.BinaryHashCodeCalculator;

public class ByteArrayWrapper implements Serializable {
	private static final long serialVersionUID = 1L;

	public static final ByteArrayWrapper newInstance(byte[] source) {
		return new ByteArrayWrapper(source);
	}

	public static final ByteArrayWrapper newInstance(byte[] source, BinaryHashCodeCalculator hashCodeCalculator) {
		return new ByteArrayWrapper(source, hashCodeCalculator);
	}

	public static final ByteArrayWrapper newInstanceWithJavaSafeHashCodeCalculator(byte[] source) {
		return ByteArrayWrapper.newInstance(source, XXHASH32_JAVA_SAFE);
	}

	public static final ByteArrayWrapper newInstanceWithJavaUnsafeHashCodeCalculator(byte[] source) {
		return ByteArrayWrapper.newInstance(source, XXHASH32_JAVA_UNSAFE);
	}

	public static final ByteArrayWrapper newInstanceWithJNIHashCodeCalculator(byte[] source) {
		return ByteArrayWrapper.newInstance(source, XXHASH32_JNI);
	}

	private final byte[] source;

	private int hashCode = -1;

	private transient final BinaryHashCodeCalculator hashCodeCalculator;

	public ByteArrayWrapper(byte[] source) {
		this(source, DEFAULT);
	}

	public ByteArrayWrapper(byte[] source, BinaryHashCodeCalculator hashCodeCalculator) {
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
		if (other instanceof ByteArrayWrapper) {
			return Arrays.equals(source, ((ByteArrayWrapper) other).source);
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
