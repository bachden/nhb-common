package com.nhb.common.serializable.impl;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import com.nhb.common.serializable.PuNamedTypeSerializableFactory;
import com.nhb.common.serializable.PuSerializable;
import com.nhb.common.serializable.exception.ClassForTypeNotFoundException;

public class DefaultNamedTypeSerializableFactory extends AutoIncrementTypeSerializableFactory
		implements PuNamedTypeSerializableFactory {

	private final Map<String, Integer> namedTypeMap = new ConcurrentHashMap<>();

	@Override
	@Deprecated
	public <T extends PuSerializable> int register(Class<T> clazz) {
		throw new RuntimeException(new IllegalAccessException(
				"Direct register class in NamedTypeSerializableFactory is Deprecated, use register with name instead"));
	}

	@Override
	@Deprecated
	public <T extends PuSerializable> int register(String className, ClassLoader classLoader) {
		throw new RuntimeException(new IllegalAccessException(
				"Direct register class in NamedTypeSerializableFactory is Deprecated, use register with name instead"));
	}

	@Override
	public <T extends PuSerializable> void register(String typeName, Class<T> clazz) {
		this.namedTypeMap.put(typeName, super.register(clazz));
	}

	@Override
	public <T extends PuSerializable> void register(String typeName, String className, ClassLoader classLoader) {
		this.namedTypeMap.put(typeName, super.register(className, classLoader));
	}

	@Override
	public <T extends PuSerializable> Class<T> getClassForType(String name) {
		if (!this.namedTypeMap.containsKey(name)) {
			throw new ClassForTypeNotFoundException("Type named " + name + " did not registered");
		}
		return this.getClassForType(this.namedTypeMap.get(name));
	}

	/**
	 * 
	 * @param name
	 *            name of the type
	 * @return null if type for name didn't registered
	 */
	public Integer getTypeForName(String name) {
		if (name != null) {
			return this.namedTypeMap.get(name);
		}
		return null;
	}

	@Override
	public void deregister(String name) {
		final Integer type = this.namedTypeMap.remove(name);
		if (type != null) {
			super.deregister(type.intValue());
		}
	}

	@Override
	@Deprecated
	public void deregister(int type) {
		throw new RuntimeException(new IllegalAccessException(
				"Direct diregister by type is being deprecated in NamedTypeSerializableFactory, use deregister by name"));
	}

	@Override
	public void register(Properties properties, ClassLoader classLoader) {
		PuNamedTypeSerializableFactory.super.register(properties, classLoader);
	}

	@Override
	public void register(Properties properties) {
		PuNamedTypeSerializableFactory.super.register(properties);
	}
}
