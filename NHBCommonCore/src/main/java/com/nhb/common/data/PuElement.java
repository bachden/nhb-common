package com.nhb.common.data;

import java.io.OutputStream;
import java.io.Serializable;

public interface PuElement extends Serializable {

	byte[] toBytes();

	String toJSON();

	String toXML();

	/**
	 * Write msgpack binary to output stream. For input side, use fromObject to
	 * read from an InputStream
	 * 
	 * @param out
	 */
	void writeTo(OutputStream out);
}
