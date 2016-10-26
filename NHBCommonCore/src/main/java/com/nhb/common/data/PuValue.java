package com.nhb.common.data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Base64;

import org.msgpack.MessagePack;
import org.msgpack.MessageTypeException;
import org.msgpack.packer.Packer;
import org.msgpack.type.Value;

import com.nhb.common.data.msgpkg.PuElementTemplate;
import com.nhb.common.exception.UnsupportedTypeException;
import com.nhb.common.utils.ArrayUtils;
import com.nhb.common.utils.PrimitiveTypeUtils;
import com.nhb.common.utils.StringUtils;

import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

public class PuValue implements PuElement, Serializable {

	private static final long serialVersionUID = -1038540720446971124L;
	private Object data = null;
	private PuDataType type = PuDataType.NULL;

	public PuValue() {
	}

	public PuValue(Object data) {
		this.setData(data);
	}

	public PuValue(Object data, PuDataType type) {
		if (type == PuDataType.RAW) {
			this.setData(data);
		} else {
			this.type = type;
			this.data = data;
		}
	}

	public boolean getBoolean() {
		return PrimitiveTypeUtils.getBooleanValueFrom(
				this.getData() instanceof byte[] ? new String((byte[]) this.getData()) : this.getData());
	}

	public boolean getBoolean(boolean defaultValue) {
		if (this.getData() == null) {
			return defaultValue;
		}
		return this.getBoolean();
	}

	public byte getByte() {
		return PrimitiveTypeUtils.getByteValueFrom(this.getData());
	}

	public byte getByte(byte defaultValue) {
		if (this.getData() == null) {
			return defaultValue;
		}
		return this.getByte();
	}

	public short getShort() {
		if (this.type == PuDataType.BYTE) {
			this.type = PuDataType.SHORT;
		}
		return PrimitiveTypeUtils.getShortValueFrom(this.getData());
	}

	public short getShort(short defaultValue) {
		if (this.getData() == null) {
			return defaultValue;
		}
		return this.getShort();
	}

	public int getInteger() {
		if (this.type == PuDataType.BYTE || this.type == PuDataType.SHORT) {
			this.type = PuDataType.INTEGER;
		}
		return PrimitiveTypeUtils.getIntegerValueFrom(this.getData());
	}

	public int getInteger(int defaultValue) {
		if (this.getData() == null) {
			return defaultValue;
		}
		return this.getInteger();
	}

	public long getLong() {
		if (this.type == PuDataType.BYTE || this.type == PuDataType.SHORT || this.type == PuDataType.INTEGER) {
			this.type = PuDataType.LONG;
		}
		return PrimitiveTypeUtils.getLongValueFrom(this.getData());
	}

	public long getLong(long defaultValue) {
		if (this.getData() == null) {
			return defaultValue;
		}
		return this.getLong();
	}

	public float getFloat() {
		return PrimitiveTypeUtils.getFloatValueFrom(this.getData());
	}

	public float getFloat(float defaultValue) {
		if (this.getData() == null) {
			return defaultValue;
		}
		return this.getFloat();
	}

	public double getDouble() {
		if (this.type == PuDataType.FLOAT) {
			this.type = PuDataType.DOUBLE;
		}
		return PrimitiveTypeUtils.getDoubleValueFrom(this.getData());
	}

	public double getDouble(double defaultValue) {
		if (this.getData() == null) {
			return defaultValue;
		}
		return this.getDouble();
	}

	public char getCharacter() {
		return PrimitiveTypeUtils.getCharValueFrom(this.getData());
	}

	public char getCharacter(char defaultValue) {
		if (this.getData() == null) {
			return defaultValue;
		}
		return this.getCharacter();
	}

	public String getString() {
		if (this.type == PuDataType.RAW) {
			this.type = PuDataType.STRING;
		}
		return PrimitiveTypeUtils.getStringValueFrom(this.getData());
	}

	public String getString(String defaultValue) {
		if (this.getData() == null) {
			return defaultValue;
		}
		return this.getString();
	}

	public byte[] getRaw() {
		if (this.type == PuDataType.STRING) {
			this.type = PuDataType.RAW;
		}
		if (this.data instanceof String) {
			this.data = ((String) this.data).getBytes();
		}
		return (byte[]) this.data;
	}

	public byte[] getRaw(byte[] defaultValue) {
		if (this.getData() == null) {
			return defaultValue;
		}
		return this.getRaw();
	}

	public PuObject getPuObject() {
		if (this.getData() instanceof PuObject) {
			return (PuObject) this.getData();
		}
		return PuObject.fromObject(this.getData());
	}

	public PuObject getPuObject(PuObject defaultValue) {
		if (this.getData() == null) {
			return defaultValue;
		}
		return this.getPuObject();
	}

	public PuArray getPuArray() {
		if (this.getData() instanceof PuArray) {
			return (PuArray) this.getData();
		}
		return PuArrayList.fromObject(this.getData());
	}

	public PuArray getPuArray(PuArray defaultValue) {
		if (this.getData() == null) {
			return defaultValue;
		}
		return this.getPuArray();
	}

	public Object getData() {
		return this.data;
	}

	/**
	 * use getRawAsBase64() instead
	 */
	@Deprecated
	public String getBase64() {
		return this.getRawAsBase64();
	}

	public String getRawAsBase64() {
		if (this.getData() instanceof byte[]) {
			return new String(Base64.getEncoder().encode(this.getRaw()));
		}
		throw new IllegalStateException("getBase64 support only for data type " + PuDataType.RAW);
	}

	public void setData(Object data) {
		this.data = data;
		this.type = PuDataType.fromObject(data);
		// if (data instanceof byte[]) {
		// String str = new String((byte[]) data);
		// if (StringUtils.isPrinable(str)) {
		// System.out.println("string is prinable: " + str);
		// this.data = str;
		// this.type = PuDataType.STRING;
		// }
		// }
	}

	public PuDataType getType() {
		return this.type;
	}

	public void decodeBase64() {
		if (this.data instanceof String) {
			this.data = Base64.getDecoder().decode((String) data);
			this.type = PuDataType.RAW;
		}
	}

	public void setType(PuDataType type) {
		this.type = type;
		switch (this.type) {
		case BOOLEAN:
			this.data = PrimitiveTypeUtils.getBooleanValueFrom(this.data);
			break;
		case BYTE:
			this.data = PrimitiveTypeUtils.getByteValueFrom(this.data);
			break;
		case CHARACTER:
			this.data = PrimitiveTypeUtils.getCharValueFrom(this.data);
			break;
		case DOUBLE:
			this.data = PrimitiveTypeUtils.getDoubleValueFrom(this.data);
			break;
		case FLOAT:
			this.data = PrimitiveTypeUtils.getFloatValueFrom(this.data);
			break;
		case INTEGER:
			this.data = PrimitiveTypeUtils.getIntegerValueFrom(this.data);
			break;
		case LONG:
			this.data = PrimitiveTypeUtils.getLongValueFrom(this.data);
			break;
		case NULL:
			this.data = null;
			break;
		case PUARRAY:
			this.data = PuArrayList.fromObject(this.data);
			break;
		case PUOBJECT:
			this.data = PuObject.fromObject(this.data);
			break;
		case RAW:
			if (this.data instanceof String) {
				this.data = ((String) this.data).getBytes();
			} else if (!(this.data instanceof byte[])) {
				throw new IllegalStateException("cannot set type to RAW when data is not String base64 or byte[]");
			}
			break;
		case SHORT:
			this.data = PrimitiveTypeUtils.getShortValueFrom(this.data);
			break;
		case STRING:
			this.data = PrimitiveTypeUtils.getStringValueFrom(this.data);
			break;

		}
	}

	public static PuValue fromObject(Object value) {
		if (value == null) {
			return new PuValue(null, PuDataType.NULL);
		} else if (value instanceof byte[]) {
			return new PuValue((byte[]) value);
		} else if (value instanceof PuValue) {
			return (new PuValue(((PuValue) value).getData(), ((PuValue) value).getType()));
		} else if (value instanceof PuObject || value instanceof PuArray
				|| PrimitiveTypeUtils.isPrimitiveOrWrapperType(value.getClass())) {
			return (new PuValue(value));
		} else if (ArrayUtils.isArrayOrCollection(value.getClass())) {
			return new PuValue(PuArrayList.fromObject(value));
		} else if (value instanceof Value) {
			switch (((Value) value).getType()) {
			case NIL:
				return new PuValue(null, PuDataType.NULL);
			case BOOLEAN:
				return new PuValue(((Value) value).asBooleanValue(), PuDataType.BOOLEAN);
			case FLOAT:
				try {
					return new PuValue(((Value) value).asFloatValue().floatValue(), PuDataType.FLOAT);
				} catch (MessageTypeException ex) {
					return new PuValue(((Value) value).asFloatValue().doubleValue(), PuDataType.LONG);
				}
			case INTEGER:
				try {
					return new PuValue(((Value) value).asIntegerValue().intValue(), PuDataType.INTEGER);
				} catch (MessageTypeException ex) {
					return new PuValue(((Value) value).asIntegerValue().longValue(), PuDataType.LONG);
				}
			case RAW:
				return new PuValue(((Value) value).asRawValue().getByteArray(), PuDataType.RAW);
			default:
				throw new UnsupportedTypeException("Map and Array cannot be read inside PuValue");
			}
		} else {
			return (new PuValue(PuObject.fromObject(value)));
		}
	}

	@Override
	public String toString() {
		if (this.getData() instanceof byte[]) {
			String str = new String((byte[]) this.getData());
			if (!StringUtils.isPrinable(str)) {
				StringBuilder sb = new StringBuilder();
				sb.append("[");
				byte[] bytes = (byte[]) this.getData();
				for (int i = 0; i < bytes.length; i++) {
					Byte b = bytes[i];
					if (sb.length() > 1) {
						sb.append(",");
					}
					sb.append(b.toString());
				}
				sb.append("]");
				return sb.toString();
			}
			return str;
		}
		return this.getData() == null ? null : this.getData().toString();
	}

	@Override
	public byte[] toBytes() {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		this.writeTo(out);
		return out.toByteArray();
	}

	@Override
	public String toJSON() {
		return this.toString();
	}

	@Override
	public String toXML() {
		return PuXmlHelper.generateXML(this);
	}

	private static final MessagePack msgpkg = new MessagePack();

	@Override
	public void writeTo(OutputStream out) {
		Packer packer = msgpkg.createPacker(out);
		try {
			PuElementTemplate.getInstance().write(packer, this);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static PuValue fromXML(String xml) {
		try {
			return (PuValue) PuXmlHelper.parseXml(xml);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static PuValue fromJSON(String json) throws ParseException {
		JSONParser parser = new JSONParser(JSONParser.DEFAULT_PERMISSIVE_MODE);
		return new PuValue(parser.parse(json));
	}
}