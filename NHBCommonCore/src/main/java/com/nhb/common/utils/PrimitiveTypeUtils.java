package com.nhb.common.utils;

import java.lang.reflect.Method;

public final class PrimitiveTypeUtils {

	private PrimitiveTypeUtils() {
		// make constructor private to prevent other where create new instance
	}

	public static final boolean isPrimitiveOrWrapperType(Class<?> resultType) {
		return (resultType == String.class || resultType == Integer.TYPE || resultType == Integer.class
				|| resultType == Float.TYPE || resultType == Float.class || resultType == Long.TYPE
				|| resultType == Long.class || resultType == Double.TYPE || resultType == Double.class
				|| resultType == Short.TYPE || resultType == Short.class || resultType == Byte.TYPE
				|| resultType == Byte.class || resultType == Character.TYPE || resultType == Character.class
				|| resultType == Boolean.TYPE || resultType == Boolean.class) && !resultType.isArray();
	}

	@SuppressWarnings("unchecked")
	public static final <T> T getValueFrom(Class<T> resultType, Object obj) {
		if (resultType != null) {
			if (resultType == String.class) {
				return (T) getStringValueFrom(obj);
			} else if (resultType == Integer.TYPE || resultType == Integer.class) {
				return (T) new Integer(getIntegerValueFrom(obj));
			} else if (resultType == Float.TYPE || resultType == Float.class) {
				return (T) new Float(getFloatValueFrom(obj));
			} else if (resultType == Long.TYPE || resultType == Long.class) {
				return (T) new Long(getLongValueFrom(obj));
			} else if (resultType == Double.TYPE || resultType == Double.class) {
				return (T) new Double(getDoubleValueFrom(obj));
			} else if (resultType == Short.TYPE || resultType == Short.class) {
				return (T) new Short(getShortValueFrom(obj));
			} else if (resultType == Byte.TYPE || resultType == Byte.class) {
				return (T) new Byte(getByteValueFrom(obj));
			} else if (resultType == Character.TYPE || resultType == Character.class) {
				return (T) new Character(getCharValueFrom(obj));
			} else if (resultType == Boolean.TYPE || resultType == Boolean.class) {
				return (T) new Boolean(getBooleanValueFrom(obj));
			} else {
				throw new RuntimeException("type doesn't supported");
			}
		}
		throw new RuntimeException("result type must be specificed");
	}

	public static final String getStringValueFrom(Object obj) {
		if (obj == null) {
			return null;
		} else if (obj instanceof String) {
			return (String) obj;
		} else if (obj instanceof byte[]) {
			return new String((byte[]) obj);
		} else if (isPrimitiveOrWrapperType(obj.getClass())) {
			return String.valueOf(obj);
		}
		return obj.toString();
	}

	public static final int getIntegerValueFrom(Object obj) {
		if (obj != null) {
			if (obj instanceof Integer || obj instanceof Long || obj instanceof Float || obj instanceof Double
					|| obj instanceof Short || obj instanceof Byte) {
				try {
					Method intValueMethod = obj.getClass().getDeclaredMethod("intValue");
					return ((Integer) intValueMethod.invoke(obj)).intValue();
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (obj instanceof Character) {
				return ((Character) obj).charValue();
			} else if (obj instanceof String) {
				return Integer.valueOf((String) obj);
			} else if (obj instanceof Boolean) {
				return (Boolean) obj ? 1 : 0;
			} else if (obj instanceof byte[]) {
				return ByteArrayUtils.primitiveFromByteArray(Integer.class, (byte[]) obj);
			}
		}
		throw new RuntimeException("cannot convert null object");
	}

	public static final long getLongValueFrom(Object obj) {
		if (obj != null) {
			if (obj instanceof Integer || obj instanceof Long || obj instanceof Float || obj instanceof Double
					|| obj instanceof Short || obj instanceof Byte) {
				try {
					Method method = obj.getClass().getDeclaredMethod("longValue");
					return (long) method.invoke(obj);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (obj instanceof Character) {
				return (long) ((Character) obj).charValue();
			} else if (obj instanceof String) {
				return Long.valueOf((String) obj);
			} else if (obj instanceof Boolean) {
				return (Boolean) obj ? 1l : 0l;
			} else if (obj instanceof byte[]) {
				return ByteArrayUtils.primitiveFromByteArray(Long.class, (byte[]) obj);
			}
		}
		throw new RuntimeException("cannot convert null object");
	}

	public static final float getFloatValueFrom(Object obj) {
		if (obj != null) {
			if (obj instanceof Integer || obj instanceof Long || obj instanceof Float || obj instanceof Double
					|| obj instanceof Short || obj instanceof Byte) {
				try {
					Method method = obj.getClass().getDeclaredMethod("floatValue");
					return (float) method.invoke(obj);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (obj instanceof Character) {
				return (float) ((Character) obj).charValue();
			} else if (obj instanceof String) {
				return Float.valueOf((String) obj);
			} else if (obj instanceof Boolean) {
				return (Boolean) obj ? 1f : 0f;
			} else if (obj instanceof byte[]) {
				return ByteArrayUtils.primitiveFromByteArray(Float.class, (byte[]) obj);
			}
		}
		throw new RuntimeException("cannot convert null object");
	}

	public static final double getDoubleValueFrom(Object obj) {
		if (obj != null) {
			if (obj instanceof Integer || obj instanceof Long || obj instanceof Float || obj instanceof Double
					|| obj instanceof Short || obj instanceof Byte) {
				try {
					Method method = obj.getClass().getDeclaredMethod("doubleValue");
					return (double) method.invoke(obj);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (obj instanceof Character) {
				return (double) ((Character) obj).charValue();
			} else if (obj instanceof String) {
				return Double.valueOf((String) obj);
			} else if (obj instanceof Boolean) {
				return (Boolean) obj ? 1d : 0d;
			} else if (obj instanceof byte[]) {
				return ByteArrayUtils.primitiveFromByteArray(Double.class, (byte[]) obj);
			}
		}
		throw new RuntimeException("cannot convert null object");
	}

	public static final short getShortValueFrom(Object obj) {
		if (obj != null) {
			if (obj instanceof Integer || obj instanceof Long || obj instanceof Float || obj instanceof Double
					|| obj instanceof Short || obj instanceof Byte) {
				try {
					Method method = obj.getClass().getDeclaredMethod("shortValue");
					return (short) method.invoke(obj);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (obj instanceof Character) {
				return (short) ((Character) obj).charValue();
			} else if (obj instanceof String) {
				return Short.valueOf((String) obj);
			} else if (obj instanceof Boolean) {
				return (short) ((Boolean) obj ? 1 : 0);
			} else if (obj instanceof byte[]) {
				return ByteArrayUtils.primitiveFromByteArray(Short.class, (byte[]) obj);
			}
		}
		throw new RuntimeException("cannot convert null object");
	}

	public static final byte getByteValueFrom(Object obj) {
		if (obj != null) {
			if (obj instanceof Integer || obj instanceof Long || obj instanceof Float || obj instanceof Double
					|| obj instanceof Short || obj instanceof Byte) {
				try {
					Method method = obj.getClass().getDeclaredMethod("byteValue");
					return (byte) method.invoke(obj);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (obj instanceof Character) {
				return (byte) ((Character) obj).charValue();
			} else if (obj instanceof String) {
				return Byte.valueOf((String) obj);
			} else if (obj instanceof Boolean) {
				return (byte) ((Boolean) obj ? 1 : 0);
			} else if (obj instanceof byte[]) {
				return ByteArrayUtils.primitiveFromByteArray(Byte.class, (byte[]) obj);
			}
		}
		throw new RuntimeException("cannot convert null object");
	}

	/**
	 * return char value for specific obj </br>
	 * if obj is number, return char represent by obj as UTF-16 code</br>
	 * else if obj is boolean, return '0' for false, '1' for true
	 * 
	 * @param obj
	 * @return char represented by input obj
	 */
	public static final char getCharValueFrom(Object obj) {
		if (obj != null) {
			if (obj instanceof Integer || obj instanceof Long || obj instanceof Float || obj instanceof Double
					|| obj instanceof Short || obj instanceof Byte) {
				try {
					Method method = obj.getClass().getDeclaredMethod("intValue");
					return Character.toChars((int) method.invoke(obj))[0];
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (obj instanceof Character) {
				return ((Character) obj).charValue();
			} else if (obj instanceof String) {
				if (((String) obj).length() > 0) {
					return ((String) obj).charAt(0);
				} else {
					return '\0';
				}
			} else if (obj instanceof Boolean) {
				return ((Boolean) obj ? '1' : '0');
			} else if (obj instanceof byte[]) {
				return ByteArrayUtils.primitiveFromByteArray(Character.class, (byte[]) obj);
			}
		}
		throw new RuntimeException("cannot convert null object");
	}

	/**
	 * return boolean value for specific obj </br>
	 * if obj is number, return false if obj == 0, true for otherwise </br>
	 * else if obj is character, return false if obj == '\0' char (null value),
	 * true for otherwise </br>
	 * else return object != null
	 * 
	 * @param obj
	 * @return
	 */
	public static final boolean getBooleanValueFrom(Object obj) {
		if (obj instanceof Integer || obj instanceof Long || obj instanceof Float || obj instanceof Double
				|| obj instanceof Short || obj instanceof Byte) {
			try {
				Method method = obj.getClass().getDeclaredMethod("longValue");
				return (long) method.invoke(obj) != 0;
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (obj instanceof String) {
			return Boolean.valueOf((String) obj);
		} else if (obj instanceof Character) {
			return ((Character) obj).charValue() != '\0';
		} else if (obj instanceof Boolean) {
			return ((Boolean) obj).booleanValue();
		} else if (obj instanceof byte[]) {
			return ByteArrayUtils.primitiveFromByteArray(Boolean.class, (byte[]) obj);
		}
		return obj != null;
	}
}
