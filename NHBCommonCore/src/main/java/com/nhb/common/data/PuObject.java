package com.nhb.common.data;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.msgpack.MessagePack;
import org.msgpack.packer.Packer;
import org.msgpack.unpacker.Unpacker;
import org.w3c.dom.Node;

import com.nhb.common.data.msgpkg.PuObjectTemplate;
import com.nhb.common.utils.ArrayUtils;
import com.nhb.common.utils.ObjectUtils;
import com.nhb.common.utils.PrimitiveTypeUtils;
import com.nhb.eventdriven.impl.BaseEventDispatcher;

import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

public class PuObject extends BaseEventDispatcher implements PuObjectRW, Iterable<Entry<String, PuValue>> {

	private static final MessagePack msgpkg = new MessagePack();
	private static final long serialVersionUID = 1982533316259989399L;
	private Map<String, PuValue> values = new HashMap<>();

	@SuppressWarnings("unchecked")
	public static PuObject fromObject(Object obj) {
		if (obj == null) {
			return null;
		} else if (obj instanceof byte[]) {
			byte[] bytes = (byte[]) obj;
			if (bytes.length == 0) {
				return null;
			}
			Unpacker unpacker = msgpkg.createUnpacker(new ByteArrayInputStream(bytes));
			try {
				return (PuObject) PuObjectTemplate.getInstance().read(unpacker, null);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else if (obj instanceof InputStream) {
			Unpacker unpacker = msgpkg.createUnpacker((InputStream) obj);
			try {
				return (PuObject) PuObjectTemplate.getInstance().read(unpacker, null);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else if (obj instanceof PuObjectRO) {
			PuObject puo = new PuObject();
			puo.addAll((PuObjectRO) obj);
			return puo;
		} else if (!PrimitiveTypeUtils.isPrimitiveOrWrapperType(obj.getClass())
				&& !ArrayUtils.isArrayOrCollection(obj.getClass())) {
			Map<String, Object> map = null;
			if (obj instanceof Map) {
				map = (Map<String, Object>) obj;
			} else {
				map = ObjectUtils.toMapRecursive(obj);
			}
			if (map != null) {
				PuObject result = new PuObject();
				for (Entry<String, Object> entry : map.entrySet()) {
					result.set(entry.getKey(), entry.getValue());
				}
				return result;
			}
		}
		throw new IllegalArgumentException("Object of type " + obj.getClass() + " cannot be converted to PuObject");
	}

	public static PuObject fromXML(String xml) {
		if (xml != null) {
			try {
				return (PuObject) PuXmlHelper.parseXml(xml);
			} catch (Exception e) {
				throw new RuntimeException("Cannot parse xml to PuObject", e);
			}
		}
		return null;
	}

	public static PuObject fromXML(Node xml) {
		if (xml != null) {
			try {
				PuElement element = PuXmlHelper.parseXml(xml);
				if (element == PuNull.EMPTY) {
					return new PuObject();
				}
				return (PuObject) element;
			} catch (Exception e) {
				throw new RuntimeException("Cannot parse xml to PuObject", e);
			}
		}
		return null;
	}

	public static PuObject fromJSON(String json) {
		if (json != null) {
			JSONParser parser = new JSONParser(JSONParser.DEFAULT_PERMISSIVE_MODE);
			try {
				JSONObject jsonObject = (JSONObject) parser.parse(json);
				return fromObject(jsonObject);
			} catch (ParseException ex) {
				throw new RuntimeException("Cannot parse json to PuObject", ex);
			}
		}
		return null;
	}

	@Override
	public int size() {
		return this.values.size();
	}

	@Override
	public String toXML() {
		return PuXmlHelper.generateXML(this);
	}

	@Override
	public String toJSON() {
		return this.toMap().toString();
	}

	@Override
	public Map<String, Object> toMap() {
		JSONObject map = new JSONObject();
		for (Entry<String, PuValue> entry : this) {
			Object value = null;
			if (entry.getValue().getType() == PuDataType.PUOBJECT) {
				value = entry.getValue().getPuObject().toMap();
			} else if (entry.getValue().getType() == PuDataType.PUARRAY) {
				value = entry.getValue().getPuArray().toList();
			} else if (entry.getValue().getType() == PuDataType.RAW) {
				value = entry.getValue().getRawAsBase64();
			} else {
				value = entry.getValue().getData();
			}

			if (value != null) {
				map.put(entry.getKey(), value);
			}
		}
		return map;
	}

	@Override
	public void removeAll() {
		this.values = new HashMap<>();
	}

	@Override
	public Iterator<Entry<String, PuValue>> iterator() {
		return this.values.entrySet().iterator();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(String fieldName) {
		return (T) this.values.get(fieldName).getData();
	}

	@Override
	public boolean variableExists(String fieldName) {
		return this.values.containsKey(fieldName);
	}

	@Override
	public PuObject deepClone() {
		PuObject clone = new PuObject();
		for (Entry<String, PuValue> entry : this) {
			if (entry.getValue().getType() == PuDataType.PUOBJECT) {
				clone.setPuObject(entry.getKey(), entry.getValue().getPuObject().deepClone());
			} else if (entry.getValue().getType() == PuDataType.PUARRAY) {
				clone.setPuArray(entry.getKey(), entry.getValue().getPuArray().deepClone());
			} else {
				clone.set(entry.getKey(), entry.getValue().getData());
			}
		}
		return clone;
	}

	@Override
	public PuDataType typeOf(String field) {
		return this.values.get(field).getType();
	}

	@Override
	public PuValue valueOf(String field) {
		return this.values.get(field);
	}

	@Override
	public boolean getBoolean(String fieldName) {
		return this.values.get(fieldName).getBoolean();
	}

	@Override
	public boolean getBoolean(String fieldName, boolean defaultValue) {
		if (this.variableExists(fieldName)) {
			return this.getBoolean(fieldName);
		}
		return defaultValue;
	}

	@Override
	public byte getByte(String fieldName) {
		return this.values.get(fieldName).getByte();
	}

	@Override
	public byte getByte(String fieldName, byte defaultValue) {
		if (this.variableExists(fieldName)) {
			return this.getByte(fieldName);
		}
		return defaultValue;
	}

	@Override
	public short getShort(String fieldName) {
		return this.values.get(fieldName).getShort();
	}

	@Override
	public short getShort(String fieldName, short defaultValue) {
		if (this.variableExists(fieldName)) {
			return this.getShort(fieldName);
		}
		return defaultValue;
	}

	@Override
	public int getInteger(String fieldName) {
		return this.values.get(fieldName).getInteger();
	}

	@Override
	public int getInteger(String fieldName, int defaultValue) {
		if (this.variableExists(fieldName)) {
			return this.getInteger(fieldName);
		}
		return defaultValue;
	}

	@Override
	public float getFloat(String fieldName) {
		return this.values.get(fieldName).getFloat();
	}

	@Override
	public float getFloat(String fieldName, float defaultValue) {
		if (this.variableExists(fieldName)) {
			return this.getFloat(fieldName);
		}
		return defaultValue;
	}

	@Override
	public long getLong(String fieldName) {
		return this.values.get(fieldName).getLong();
	}

	@Override
	public long getLong(String fieldName, long defaultValue) {
		if (this.variableExists(fieldName)) {
			return this.getLong(fieldName);
		}
		return defaultValue;
	}

	@Override
	public double getDouble(String fieldName) {
		return this.values.get(fieldName).getDouble();
	}

	@Override
	public double getDouble(String fieldName, double defaultValue) {
		if (this.variableExists(fieldName)) {
			return this.getDouble(fieldName);
		}
		return defaultValue;
	}

	@Override
	public String getString(String fieldName) {
		return this.values.get(fieldName).getString();
	}

	@Override
	public String getString(String fieldName, String defaultValue) {
		if (this.variableExists(fieldName)) {
			return this.getString(fieldName);
		}
		return defaultValue;
	}

	@Override
	public PuObject getPuObject(String fieldName) {
		return this.values.get(fieldName).getPuObject();
	}

	@Override
	public PuObject getPuObject(String fieldName, PuObject defaultValue) {
		if (this.variableExists(fieldName)) {
			return this.getPuObject(fieldName);
		}
		return defaultValue;
	}

	@Override
	public PuArray getPuArray(String fieldName) {
		return this.values.get(fieldName).getPuArray();
	}

	@Override
	public PuArray getPuArray(String fieldName, PuArray defaultValue) {
		if (this.variableExists(fieldName)) {
			return this.getPuArray(fieldName);
		}
		return defaultValue;
	}

	@Override
	public void set(String fieldName, Object value) {
		this.values.put(fieldName, PuValue.fromObject(value));
	}

	@Override
	public void setType(String fieldName, PuDataType type) {
		if (this.variableExists(fieldName)) {
			this.values.get(fieldName).setType(type);
		}
	}

	@Override
	public void decodeBase64(String fieldName) {
		if (this.variableExists(fieldName)) {
			this.values.get(fieldName).decodeBase64();
		}
	}

	@Override
	public Object remove(String fieldName) {
		return this.values.remove(fieldName);
	}

	@Override
	public void addAll(PuObjectRO source) {
		if (source == null) {
			return;
		}
		try {
			for (Entry<String, PuValue> entry : source) {
				this.set(entry.getKey(), entry.getValue());
			}
		} catch (UnsupportedOperationException ex) {
			Map<String, ?> map = source.toMap();
			if (map != null) {
				for (Entry<String, ?> entry : map.entrySet()) {
					this.set(entry.getKey(), entry.getValue());
				}
			}
		}
	}

	@Override
	public void setBoolean(String fieldName, Boolean value) {
		this.set(fieldName, value);
	}

	@Override
	public void setByte(String fieldName, Byte value) {
		this.set(fieldName, value);
	}

	@Override
	public void setShort(String fieldName, Short value) {
		this.set(fieldName, value);
	}

	@Override
	public void setInteger(String fieldName, Integer value) {
		this.set(fieldName, value);
	}

	@Override
	public void setLong(String fieldName, Long value) {
		this.set(fieldName, value);
	}

	@Override
	public void setFloat(String fieldName, Float value) {
		this.set(fieldName, value);
	}

	@Override
	public void setDouble(String fieldName, Double value) {
		this.set(fieldName, value);
	}

	@Override
	public void setString(String fieldName, String value) {
		this.set(fieldName, value);
	}

	@Override
	public void setPuObject(String fieldName, PuObject value) {
		this.set(fieldName, value);
	}

	@Override
	public void setPuArray(String fieldName, PuArray value) {
		this.set(fieldName, value);
	}

	void append(int numTab, StringBuilder _builder) {
		StringBuilder builder = _builder == null ? new StringBuilder() : _builder;
		String tabs = "";
		if (numTab > 0) {
			for (int i = 0; i < numTab; i++) {
				tabs += "\t";
			}
		}
		builder.append("{\n");
		boolean flag = true;
		for (Entry<String, PuValue> entry : this) {
			if (flag) {
				flag = false;
			} else {
				builder.append(",\n");
			}
			builder.append(tabs + "\t");
			builder.append(entry.getKey());
			builder.append(":");
			builder.append(entry.getValue().getType().name().toLowerCase());

			builder.append(" = ");
			if (entry.getValue().getType() == PuDataType.PUOBJECT) {
				PuObject puo = (PuObject) entry.getValue().getData();
				puo.append(numTab + 1, builder);
			} else if (entry.getValue().getType() == PuDataType.PUARRAY) {
				PuArrayList arr = (PuArrayList) entry.getValue().getPuArray();
				arr.append(numTab + 1, builder);
			} else {
				builder.append(entry.getValue().toString());
			}
		}
		builder.append("\n").append(tabs).append("}");
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		this.append(0, sb);
		return sb.toString();
	}

	@Override
	public void writeTo(OutputStream out) {
		Packer packer = msgpkg.createPacker(out);
		try {
			PuObjectTemplate.getInstance().write(packer, this);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public byte[] getRaw(String fieldName) {
		Object data = this.get(fieldName);
		if (data instanceof String) {
			return ((String) data).getBytes();
		}
		return (byte[]) data;
	}

	@Override
	public byte[] getRaw(String fieldName, byte[] defaultValue) {
		return this.variableExists(fieldName) ? this.getRaw(fieldName) : defaultValue;
	}

	@Override
	public void setRaw(String fieldName, byte[] value) {
		this.set(fieldName, value);
	}

}