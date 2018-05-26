package com.nhb.common.data;

import java.io.OutputStream;

public class PuDummy implements PuElement {

	private static final long serialVersionUID = 1L;

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
