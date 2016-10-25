package nhb.common.data;

public interface PuObjectRW extends PuObjectRO {

	Object remove(String fieldName);

	void removeAll();

	void addAll(PuObjectRO source);

	void decodeBase64(String fieldName);

	void setType(String fieldName, PuDataType type);

	void set(String fieldName, Object value);

	void setRaw(String fieldName, byte[] value);

	void setBoolean(String fieldName, Boolean value);

	void setByte(String fieldName, Byte value);

	void setShort(String fieldName, Short value);

	void setInteger(String fieldName, Integer value);

	void setLong(String fieldName, Long value);

	void setFloat(String fieldName, Float value);

	void setDouble(String fieldName, Double value);

	void setString(String fieldName, String value);

	void setPuObject(String fieldName, PuObject value);

	void setPuArray(String fieldName, PuArray value);

}