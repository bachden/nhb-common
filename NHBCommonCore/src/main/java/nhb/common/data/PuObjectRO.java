package nhb.common.data;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public interface PuObjectRO extends Iterable<Entry<String, PuValue>>, PuElement {
	// generic
	int size();

	Map<String, ?> toMap();

	Iterator<Entry<String, PuValue>> iterator();

	<T> T get(String fieldName);

	boolean variableExists(String fieldName);

	PuObject deepClone();

	PuDataType typeOf(String field);

	boolean getBoolean(String fieldName);

	boolean getBoolean(String fieldName, boolean defaultValue);

	byte[] getRaw(String fieldName);

	byte[] getRaw(String fieldName, byte[] defaultValue);

	byte getByte(String fieldName);

	byte getByte(String fieldName, byte defaultValue);

	short getShort(String fieldName);

	short getShort(String fieldName, short defaultValue);

	int getInteger(String fieldName);

	int getInteger(String fieldName, int defaultValue);

	float getFloat(String fieldName);

	float getFloat(String fieldName, float defaultValue);

	long getLong(String fieldName);

	long getLong(String fieldName, long defaultValue);

	double getDouble(String fieldName);

	double getDouble(String fieldName, double defaultValue);

	String getString(String fieldName);

	String getString(String fieldName, String defaultValue);

	PuObject getPuObject(String fieldName);

	PuObject getPuObject(String fieldName, PuObject defaultValue);

	PuArray getPuArray(String fieldName);

	PuArray getPuArray(String fieldName, PuArray defaultValue);
	
	PuValue valueOf(String fieldName);
}