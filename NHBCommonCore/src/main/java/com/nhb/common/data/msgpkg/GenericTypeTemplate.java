package com.nhb.common.data.msgpkg;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.msgpack.MessageTypeException;
import org.msgpack.packer.Packer;
import org.msgpack.template.AbstractTemplate;
import org.msgpack.template.ByteArrayTemplate;
import org.msgpack.template.StringTemplate;
import org.msgpack.type.RawValue;
import org.msgpack.type.ValueType;
import org.msgpack.unpacker.Unpacker;

import com.nhb.common.utils.ArrayUtils;
import com.nhb.common.utils.PrimitiveTypeUtils;
import com.nhb.common.utils.ArrayUtils.ForeachCallback;

public class GenericTypeTemplate extends AbstractTemplate<Object> {

	private static final GenericTypeTemplate instance = new GenericTypeTemplate();

	public static GenericTypeTemplate getInstance() {
		return instance;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void write(final Packer pk, final Object obj, boolean required) throws IOException {
		if (obj == null) {
			pk.writeNil();
		} else if (obj instanceof byte[]) {
			ByteArrayTemplate.getInstance().write(pk, (byte[]) obj, required);
		} else if (PrimitiveTypeUtils.isPrimitiveOrWrapperType(obj.getClass())) {
			pk.write(obj);
		} else if (ArrayUtils.isArrayOrCollection(obj.getClass())) {
			this.writeList(pk, obj, required);
		} else if (obj instanceof Map) {
			this.writeMap(pk, (Map) obj, required);
		}
	}

	protected void writeMap(Packer pk, Map<String, Object> map, boolean required) throws IOException {
		pk.writeMapBegin(map.size());
		for (Entry<String, Object> entry : map.entrySet()) {
			StringTemplate.getInstance().write(pk, entry.getKey(), required);
			this.write(pk, entry.getValue(), required);
		}
		pk.writeMapEnd();
	}

	protected void writeList(final Packer pk, Object arrayOrCollection, final boolean required) throws IOException {
		pk.writeArrayBegin(ArrayUtils.length(arrayOrCollection));
		ArrayUtils.foreach(arrayOrCollection, new ForeachCallback<Object>() {

			@Override
			public void apply(Object element) {
				try {
					GenericTypeTemplate.this.write(pk, element, required);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		});
		pk.writeArrayEnd();
	}

	@Override
	public Object read(Unpacker unpacker, Object result, boolean required) throws IOException {
		Object _result = null;
		ValueType nextValueType = unpacker.getNextType();
		switch (nextValueType) {
		case NIL:
			unpacker.readNil();
			_result = null;
			break;
		case ARRAY:
			_result = this.readList(unpacker, required);
			break;
		case MAP:
			_result = this.readMap(unpacker, required);
			break;
		case BOOLEAN:
			_result = unpacker.readBoolean();
			break;
		case FLOAT:
			try {
				_result = unpacker.readFloat();
			} catch (MessageTypeException e) {
				_result = unpacker.readDouble();
			}
			break;
		case INTEGER:
			try {
				_result = unpacker.readInt();
			} catch (MessageTypeException e) {
				_result = unpacker.readLong();
			}
			break;
		case RAW:
			RawValue value = (RawValue) unpacker.readValue();
			_result = value.getByteArray();
			break;
		case STRING:
			_result = unpacker.readString();
			break;
		default:
			throw new RuntimeException("Value type is not supported or invalid: " + nextValueType);
		}
		return _result;
	}

	protected Object readList(Unpacker unpacker, boolean required) throws IOException {
		List<Object> list = new ArrayList<>();
		int length = unpacker.readArrayBegin();
		for (int i = 0; i < length; i++) {
			list.add(read(unpacker, null, required));
		}
		unpacker.readArrayEnd();
		return list;
	}

	protected Object readMap(Unpacker unpacker, boolean required) throws IOException {
		Map<String, Object> map = new HashMap<>();
		int size = unpacker.readMapBegin();
		for (int i = 0; i < size; i++) {
			map.put(unpacker.readString(), this.read(unpacker, null, required));
		}
		unpacker.readMapEnd();
		return map;
	}

}