package com.nhb.common.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.msgpack.MessagePack;
import org.msgpack.packer.Packer;

import com.nhb.common.data.msgpkg.PuArrayTemplate;
import com.nhb.common.utils.ArrayUtils;
import com.nhb.common.utils.PrimitiveTypeUtils;
import com.nhb.common.utils.ArrayUtils.ForeachCallback;

import net.minidev.json.JSONArray;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

public class PuArrayList extends ArrayList<PuValue> implements PuArray {

	private static final MessagePack msgpkg = new MessagePack();
	private static final long serialVersionUID = 1L;

	public static PuArray fromObject(Object data) {
		if (data == null) {
			return null;
		} else if (data instanceof byte[]) {
			byte[] bytes = (byte[]) data;
			try {
				return (PuArray) PuArrayTemplate.getInstance()
						.read(msgpkg.createUnpacker(new ByteArrayInputStream(bytes)), null);
			} catch (IOException e) {
				throw new RuntimeException(
						"Error occurs while trying to deserialize byte array as message pack to PuArray", e);
			}
		} else if (data instanceof PuArray) {
			PuArray arr = (PuArray) data;
			for (PuValue value : arr) {
				arr.add(new PuValue(((PuValue) value).getData(), ((PuValue) value).getType()));
			}
			return arr;
		} else if (ArrayUtils.isArrayOrCollection(data.getClass())) {
			final PuArray result = new PuArrayList();
			ArrayUtils.foreach(data, new ForeachCallback<Object>() {

				@Override
				public void apply(Object element) {
					result.add(PuValue.fromObject(element));
				}
			});
			return result;
		}
		throw new IllegalArgumentException("cannot convert " + data.getClass() + " to PuArray");
	}

	public static PuArray fromJSON(String json) {
		if (json != null) {
			JSONParser parser = new JSONParser(JSONParser.DEFAULT_PERMISSIVE_MODE);
			try {
				JSONArray jsonArray = (JSONArray) parser.parse(json);
				return fromObject(jsonArray);
			} catch (ParseException ex) {
				throw new RuntimeException("Cannot parse json to PuObject", ex);
			}
		}
		return null;
	}

	public static PuArray fromXML(String xml) {
		PuElement element;
		try {
			element = PuXmlHelper.parseXml(xml);
			if (element == PuNull.EMPTY) {
				return new PuArrayList();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return (PuArray) element;
	}

	@Override
	public void addFrom(Object... data) {
		for (Object obj : data) {
			this.add(PuValue.fromObject(obj));
		}
	}

	@Override
	public PuDataType getTypeAt(int index) {
		return this.get(index).getType();
	}

	@Override
	public boolean getBoolean(int index) {
		return PrimitiveTypeUtils.getBooleanValueFrom(this.get(index).getData());
	}

	@Override
	public byte getByte(int index) {
		return PrimitiveTypeUtils.getByteValueFrom(this.get(index).getData());
	}

	@Override
	public short getShort(int index) {
		return PrimitiveTypeUtils.getShortValueFrom(this.get(index).getData());
	}

	@Override
	public int getInteger(int index) {
		return PrimitiveTypeUtils.getIntegerValueFrom(this.get(index).getData());
	}

	@Override
	public float getFloat(int index) {
		return PrimitiveTypeUtils.getFloatValueFrom(this.get(index).getData());
	}

	@Override
	public long getLong(int index) {
		return PrimitiveTypeUtils.getLongValueFrom(this.get(index).getData());
	}

	@Override
	public double getDouble(int index) {
		return PrimitiveTypeUtils.getDoubleValueFrom(this.get(index).getData());
	}

	@Override
	public String getString(int index) {
		return PrimitiveTypeUtils.getStringValueFrom(this.get(index).getData());
	}

	@Override
	public PuObject getPuObject(int index) {
		Object obj = this.get(index).getData();
		if (obj == null) {
			return null;
		}
		if (obj instanceof PuObject) {
			return (PuObject) obj;
		}
		if (PrimitiveTypeUtils.isPrimitiveOrWrapperType(obj.getClass())
				|| ArrayUtils.isArrayOrCollection(obj.getClass())) {
			throw new RuntimeException("Cannot convert primitive type or array/collection to PuObject");
		}
		return PuObject.fromObject(obj);
	}

	@Override
	public PuArray getPuArray(int index) {
		Object obj = this.get(index).getData();
		if (obj == null) {
			return null;
		}
		if (obj instanceof PuArray) {
			return (PuArray) obj;
		} else if (!ArrayUtils.isArrayOrCollection(obj.getClass())) {
			throw new RuntimeException("Cannot convert non-array/non-collection to PuArray");
		}
		return PuArrayList.fromObject(obj);
	}

	@Override
	public List<Object> toList() {
		JSONArray jsonArray = new JSONArray();
		for (PuValue value : this) {
			if (value.getType() == PuDataType.PUOBJECT) {
				jsonArray.add(value.getPuObject().toMap());
			} else if (value.getType() == PuDataType.PUARRAY) {
				jsonArray.add(value.getPuArray().toList());
			} else if (value.getType() == PuDataType.RAW) {
				jsonArray.add(value.getRawAsBase64());
			} else {
				jsonArray.add(value.getData());
			}
		}
		return jsonArray;
	}

	@Override
	public String toJSON() {
		return this.toList().toString();
	}

	@Override
	public String toXML() {
		return null;
	}

	void append(int numTab, StringBuilder sb) {
		sb.append("[");
		boolean flag = false;
		for (PuValue value : this) {
			if (flag) {
				sb.append(", ");
			} else {
				flag = true;
			}
			if (value.getType() == PuDataType.PUOBJECT) {
				PuObject puo = (PuObject) value.getData();
				puo.append(numTab + 1, sb);
			} else if (value.getType() == PuDataType.PUARRAY) {
				PuArrayList puArray = (PuArrayList) value.getData();
				puArray.append(numTab + 1, sb);
			} else {
				sb.append((Object) value.getData());
			}
		}
		sb.append("]");
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		append(0, sb);
		return sb.toString();
	}

	@Override
	public byte[] toBytes() {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		this.writeTo(out);
		return out.toByteArray();
	}

	@Override
	public void writeTo(OutputStream os) {
		Packer packer = msgpkg.createPacker(os);
		try {
			PuArrayTemplate.getInstance().write(packer, this);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public byte[] getRaw(int index) {
		PuValue value = this.get(index);
		if (value == null) {
			return null;
		}
		return (byte[]) value.getData();
	}

	@Override
	public PuArray deepClone() {
		PuArrayList clone = new PuArrayList();
		for (PuValue entry : this) {
			if (entry.getType() == PuDataType.PUOBJECT) {
				clone.addFrom(entry.getPuObject().deepClone());
			} else if (entry.getType() == PuDataType.PUARRAY) {
				clone.addFrom(entry.getPuArray().deepClone());
			} else {
				clone.addFrom(entry.getData());
			}
		}
		return clone;
	}
}
