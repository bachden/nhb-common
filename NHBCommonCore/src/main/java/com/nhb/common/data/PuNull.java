package com.nhb.common.data;

import java.io.OutputStream;

/**
 * Define an object will be ignore by all handling process...
 * 
 * @author bachden
 *
 */
public class PuNull implements PuElement {

	public static final PuNull IGNORE_ME = new PuNull();
	public static final PuElement EMPTY = new PuNull();

	private static final long serialVersionUID = 1807884846483316157L;

	protected PuNull() {
	}

	@Override
	public byte[] toBytes() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toJSON() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toXML() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void writeTo(OutputStream out) {
		throw new UnsupportedOperationException();
	}

}
