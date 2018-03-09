package com.nhb.common.serializable;

import java.util.Map.Entry;
import java.util.Properties;

import com.nhb.common.exception.CreateInstanceErrorException;
import com.nhb.common.serializable.exception.ClassForTypeNotFoundException;
import com.nhb.common.utils.PrimitiveTypeUtils;

public interface PuNamedTypeSerializableFactory extends PuSerializableFactory {

	<T extends PuSerializable> void register(String typeName, Class<T> clazz);

	void deregister(String name);

	<T extends PuSerializable> Class<T> getClassForType(String name);

	@SuppressWarnings("unchecked")
	default <T extends PuSerializable> void register(String typeName, String className, ClassLoader classLoader) {
		try {
			this.register(typeName, (Class<T>) classLoader.loadClass(className));
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Error while getting class for name " + className);
		}
	}

	default <T extends PuSerializable> void registerIfAbsent(String typeName, Class<T> clazz) {
		if (this.getClassForType(typeName) == null) {
			this.register(typeName, clazz);
		}
	}

	@SuppressWarnings("unchecked")
	default <T extends PuSerializable> void registerIfAbsent(String typeName, String className,
			ClassLoader classLoader) {
		try {
			this.registerIfAbsent(typeName, (Class<T>) classLoader.loadClass(className));
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Error while getting class for name " + className);
		}
	}

	default void register(Properties properties, ClassLoader classLoader) {
		if (properties == null) {
			this.getLogger().warn("Properties is null, ignore registering");
		}

		for (Entry<Object, Object> entry : properties.entrySet()) {
			String typeName = PrimitiveTypeUtils.getStringValueFrom(entry.getKey());
			String className = PrimitiveTypeUtils.getStringValueFrom(entry.getValue());
			if (className == null) {
				getLogger().warn("Class name is null for type " + typeName + ", ignore this entry");
			} else {
				this.register(typeName, className, classLoader);
			}
		}
	}

	default void register(Properties properties) {
		this.register(properties, this.getClass().getClassLoader());
	}

	default <T extends PuSerializable> T newInstanceForType(String typeName) {
		if (typeName != null) {
			Class<T> clazz = this.getClassForType(typeName);
			if (clazz == null) {
				throw new ClassForTypeNotFoundException("Class for type " + typeName + " not found");
			}
			try {
				return clazz.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				throw new CreateInstanceErrorException(
						"Cannot create instance for registered type " + typeName + ", class: " + clazz.getName());
			}
		}
		throw new NullPointerException("Type name cannot be null");
	}
}
