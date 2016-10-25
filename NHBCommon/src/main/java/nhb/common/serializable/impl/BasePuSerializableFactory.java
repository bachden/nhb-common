package nhb.common.serializable.impl;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;

import nhb.common.annotations.Transparent;
import nhb.common.data.exception.InvalidTypeException;
import nhb.common.serializable.PuSerializable;
import nhb.common.serializable.PuSerializableFactory;

@Transparent
public class BasePuSerializableFactory implements PuSerializableFactory {

	private final BiMap<Integer, Class<? extends PuSerializable>> typeToClassMap = Maps
			.synchronizedBiMap(HashBiMap.create());

	@Override
	public final void register(int type, Class<? extends PuSerializable> clazz) {
		if (this.typeToClassMap.containsKey(type)) {
			throw new IllegalArgumentException(
					"Type " + type + " did registered to class " + this.getClassForType(type).getName());
		} else if (this.typeToClassMap.inverse().containsKey(clazz)) {
			throw new IllegalArgumentException(
					"Class " + clazz + " did registered to type " + this.getTypeForClass(clazz));
		}
		this.typeToClassMap.put(type, clazz);
	}

	@Override
	public void deregister(int type) {
		if (this.typeToClassMap.containsKey(type)) {
			this.typeToClassMap.remove(type);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public final <T extends PuSerializable> Class<T> getClassForType(int type) {
		return (Class<T>) this.typeToClassMap.get(type);
	}

	@Override
	public <T extends PuSerializable> Integer getTypeForClass(Class<T> clazz) {
		if (this.typeToClassMap.inverse().containsKey(clazz)) {
			return this.typeToClassMap.inverse().get(clazz);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public int getTypeForClass(String className, ClassLoader classLoader) {
		Class<?> tmpClass = null;
		try {
			tmpClass = classLoader.loadClass(className);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Cannot load class named " + className + " from specific classLoader");
		}
		if (PuSerializable.class.isAssignableFrom(tmpClass)) {
			Class<? extends PuSerializable> clazz = (Class<? extends PuSerializable>) tmpClass;
			return this.getTypeForClass(clazz);
		}
		throw new InvalidTypeException("Class for name " + className
				+ " loaded from specific classLoader is not assignable from PuSerializable");
	}

}
