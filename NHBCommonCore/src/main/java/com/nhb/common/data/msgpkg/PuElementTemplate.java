package com.nhb.common.data.msgpkg;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.msgpack.MessagePack;
import org.msgpack.packer.Packer;
import org.msgpack.type.ValueType;
import org.msgpack.unpacker.Unpacker;

import com.nhb.common.data.PuArray;
import com.nhb.common.data.PuArrayList;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuValue;

public class PuElementTemplate extends PuTemplate<PuElement> {

	private static final MessagePack msgpack = new MessagePack();
	private static final PuElementTemplate instance = new PuElementTemplate();

	public static final PuElementTemplate getInstance() {
		return instance;
	}

	@Override
	public void write(Packer pk, PuElement puElement, boolean required) throws IOException {
		if (puElement instanceof PuArray) {
			PuArrayTemplate.getInstance().write(pk, (PuArrayList) puElement, required);
		} else if (puElement instanceof PuObject) {
			PuObjectTemplate.getInstance().write(pk, (PuObject) puElement, required);
		} else if (puElement instanceof PuValue) {
			this.getGenericTypeTemplate().write(pk, ((PuValue) puElement).getData());
		} else {
			throw new IllegalArgumentException(puElement.getClass() + " is not supported");
		}
	}

	@Override
	public PuElement read(Unpacker unpacker, PuElement obj, boolean required) throws IOException {
		ValueType valueType = unpacker.getNextType();
		switch (valueType) {
		case ARRAY:
			return PuArrayTemplate.getInstance().read(unpacker, obj != null ? (PuArrayList) obj : null, required);
		case MAP:
			return PuObjectTemplate.getInstance().read(unpacker, obj != null ? (PuObject) obj : null, required);
		default:
			return PuValue.fromObject(unpacker.readValue());
		}
	}

	public PuElement read(byte[] bytes) throws IOException {
		try (ByteArrayInputStream in = new ByteArrayInputStream(bytes)) {
			return this.read(in);
		}
	}

	public PuElement read(InputStream is) throws IOException {
		Unpacker unpacker = msgpack.createUnpacker(is);
		return this.read(unpacker, null);
	}
}
