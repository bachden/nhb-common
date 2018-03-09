package com.nhb.common.data;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

public interface PuElement extends Serializable {

	public static final int DEFAULT_BUFFER_SIZE = Integer
			.valueOf(System.getProperty("PuElement.defaultBufferSize", "1024"));

	default byte[] toBytes() {
		return toBytes(DEFAULT_BUFFER_SIZE);
	}

	default byte[] toBytes(int bufferSize) {
		ByteArrayOutputStream out = new ByteArrayOutputStream(bufferSize);
		this.writeTo(out);
		return out.toByteArray();
	}

	String toJSON();

	String toXML();

	/**
	 * Write msgpack binary to output stream. For input side, use fromObject to read
	 * from an InputStream
	 * 
	 * @param out
	 */
	void writeTo(OutputStream out);
}
