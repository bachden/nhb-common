package nhb.common.data;

import org.msgpack.template.BooleanTemplate;
import org.msgpack.template.ByteArrayTemplate;
import org.msgpack.template.ByteTemplate;
import org.msgpack.template.CharacterTemplate;
import org.msgpack.template.DoubleTemplate;
import org.msgpack.template.FloatTemplate;
import org.msgpack.template.IntegerTemplate;
import org.msgpack.template.LongTemplate;
import org.msgpack.template.ShortTemplate;
import org.msgpack.template.StringTemplate;
import org.msgpack.template.Template;

import nhb.common.data.msgpkg.PuArrayTemplate;
import nhb.common.data.msgpkg.PuObjectTemplate;
import nhb.common.utils.ArrayUtils;
import nhb.common.utils.PrimitiveTypeUtils;

public enum PuDataType {

	NULL(0),
	RAW(1, ByteArrayTemplate.getInstance()),
	BOOLEAN(2, BooleanTemplate.getInstance(), Boolean.class),
	BYTE(3, ByteTemplate.getInstance(), Byte.class),
	SHORT(4, ShortTemplate.getInstance(), Short.class),
	INTEGER(5, IntegerTemplate.getInstance(), Integer.class),
	LONG(6, LongTemplate.getInstance(), Long.class),
	FLOAT(7, FloatTemplate.getInstance(), Float.class),
	DOUBLE(8, DoubleTemplate.getInstance(), Double.class),
	CHARACTER(9, CharacterTemplate.getInstance(), Character.class),
	STRING(10, StringTemplate.getInstance(), String.class),
	PUOBJECT(11, PuObjectTemplate.getInstance()),
	PUARRAY(12, PuArrayTemplate.getInstance());

	private byte typeId;
	private Template<?> template;
	private Class<?> dataClass;

	private PuDataType(int typeId) {
		this.typeId = (byte) typeId;
	}

	private PuDataType(int typeId, Template<?> messagePackTemplate) {
		this.typeId = (byte) typeId;
		this.template = messagePackTemplate;
	}

	private PuDataType(int typeId, Template<?> messagePackTemplate, Class<?> dataClass) {
		this(typeId, messagePackTemplate);
		this.dataClass = dataClass;
	}

	public byte getTypeId() {
		return this.typeId;
	}

	public String getName() {
		return this.name().toLowerCase();
	}

	public static PuDataType fromId(byte id) {
		if (id >= 0) {
			for (PuDataType dt : values()) {
				if (dt.getTypeId() == id) {
					return dt;
				}
			}
		}
		return null;
	}

	public static PuDataType fromObject(Object obj) {
		if (obj != null) {
			if (obj instanceof byte[]) {
				return RAW;
			} else if (PrimitiveTypeUtils.isPrimitiveOrWrapperType(obj.getClass())) {
				if (obj.getClass() == Byte.class || obj.getClass() == Byte.TYPE) {
					return PuDataType.BYTE;
				} else if (obj.getClass() == Short.class || obj.getClass() == Short.TYPE) {
					return PuDataType.SHORT;
				} else if (obj.getClass() == Integer.class || obj.getClass() == Integer.TYPE) {
					return PuDataType.INTEGER;
				} else if (obj.getClass() == Long.class || obj.getClass() == Long.TYPE) {
					return PuDataType.LONG;
				} else if (obj.getClass() == Float.class || obj.getClass() == Float.TYPE) {
					return PuDataType.FLOAT;
				} else if (obj.getClass() == Double.class || obj.getClass() == Double.TYPE) {
					return PuDataType.DOUBLE;
				} else if (obj.getClass() == String.class) {
					return PuDataType.STRING;
				} else if (obj.getClass() == Character.class || obj.getClass() == Character.TYPE) {
					return PuDataType.CHARACTER;
				} else if (obj.getClass() == Boolean.class || obj.getClass() == Boolean.TYPE) {
					return PuDataType.BOOLEAN;
				}
			} else if (ArrayUtils.isArrayOrCollection(obj.getClass())) {
				return PUARRAY;
			} else if (obj instanceof PuObject) {
				return PUOBJECT;
			}
			throw new RuntimeException("Object type not supported: " + obj.getClass());
		}
		return PuDataType.NULL;
	}

	public static PuDataType fromName(String name) {
		if (name != null) {
			for (PuDataType dt : values()) {
				if (dt.name().equalsIgnoreCase(name)) {
					return dt;
				}
			}
		}
		return null;
	}

	@SuppressWarnings("rawtypes")
	public Template getTemplate() {
		return template;
	}

	public Class<?> getDataClass() {
		return dataClass;
	}

}