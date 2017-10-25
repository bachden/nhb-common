package com.nhb.common.vo;

import java.io.Serializable;
import java.util.Arrays;

public class ByteArrayWrapper implements Serializable {

	private static final long serialVersionUID = 1L;

	private final byte[] source;

	private int hashCode = -1;

	public ByteArrayWrapper(byte[] source) {
		if (source == null) {
			throw new NullPointerException("Source byte[] cannot be null");
		}
		this.source = source;
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
			hashCode = Arrays.hashCode(source);
		}
		return hashCode;
	}

}
