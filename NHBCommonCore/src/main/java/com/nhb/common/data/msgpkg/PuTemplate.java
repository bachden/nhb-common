package com.nhb.common.data.msgpkg;

import java.io.IOException;

import org.msgpack.template.AbstractTemplate;
import org.msgpack.unpacker.Unpacker;

import com.nhb.common.data.PuArrayList;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuValue;

abstract class PuTemplate<T> extends AbstractTemplate<T> {

	private GenericTypeTemplate genericTypeTemplate = new GenericTypeTemplate() {

		protected Object readMap(Unpacker unpacker, boolean required) throws IOException {
			PuObject map = new PuObject();
			int size = unpacker.readMapBegin();
			for (int i = 0; i < size; i++) {
				map.set(unpacker.readString(), this.read(unpacker, null, required));
			}
			unpacker.readMapEnd();
			return map;
		}

		protected Object readList(Unpacker unpacker, boolean required) throws IOException {
			PuArrayList list = new PuArrayList();
			int length = unpacker.readArrayBegin();
			for (int i = 0; i < length; i++) {
				list.add(new PuValue(read(unpacker, null, required)));
			}
			unpacker.readArrayEnd();
			return list;
		}
	};

	public GenericTypeTemplate getGenericTypeTemplate() {
		return genericTypeTemplate;
	}

	public void setGenericTypeTemplate(GenericTypeTemplate genericTypeTemplate) {
		this.genericTypeTemplate = genericTypeTemplate;
	}
}
