package nhb.common.data;

import java.util.List;

public interface PuArray extends List<PuValue>, PuElement {

	List<Object> toList();

	void addFrom(Object... data);

	PuDataType getTypeAt(int index);

	byte[] getRaw(int index);

	boolean getBoolean(int index);

	byte getByte(int index);

	short getShort(int index);

	int getInteger(int index);

	float getFloat(int index);

	long getLong(int index);

	double getDouble(int index);

	String getString(int index);

	PuObject getPuObject(int index);

	PuArray getPuArray(int index);

	PuArray deepClone();
}
