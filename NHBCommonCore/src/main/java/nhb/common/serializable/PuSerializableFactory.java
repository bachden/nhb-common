package nhb.common.serializable;

import java.util.Map.Entry;

import nhb.common.Loggable;
import nhb.common.data.PuArray;
import nhb.common.data.PuArrayList;
import nhb.common.data.exception.InvalidTypeException;
import nhb.common.exception.CreateInstanceErrorException;
import nhb.common.serializable.exception.ClassDoesNotRegisteredException;
import nhb.common.serializable.exception.ClassForTypeNotFoundException;
import nhb.common.utils.PrimitiveTypeUtils;

import java.util.Properties;

public interface PuSerializableFactory extends Loggable {

	void register(int type, Class<? extends PuSerializable> clazz);

	void deregister(int type);

	<T extends PuSerializable> Integer getTypeForClass(Class<T> clazz);

	default int getTypeForClass(String className) {
		return this.getTypeForClass(className, this.getClass().getClassLoader());
	}

	int getTypeForClass(String className, ClassLoader classLoader);

	<T extends PuSerializable> Class<T> getClassForType(int type);

	default <T extends PuSerializable> T deserialize(PuArray puArray) {
		if (puArray != null) {
			int type = puArray.remove(0).getInteger();
			T instance = this.newInstanceForType(type);
			instance.read(puArray);
			return instance;
		}
		return null;
	}

	default <T extends PuSerializable> PuArray serialize(T instance) {
		if (instance != null) {
			PuArray arr = new PuArrayList();
			Integer type = this.getTypeForClass(instance.getClass());
			if (type == null) {
				throw new ClassDoesNotRegisteredException(
						"Class named " + instance.getClass().getName() + " did not registered");
			}
			arr.addFrom(type.intValue());
			instance.write(arr);
			return arr;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	default void register(int type, String className, ClassLoader classLoader) {
		try {
			Class<?> clazz = classLoader.loadClass(className);
			if (PuSerializable.class.isAssignableFrom(clazz)) {
				this.register(type, (Class<? extends PuSerializable>) clazz);
			} else {
				throw new InvalidTypeException("Class for name " + className + " is not instanceof PuSerializable");
			}
		} catch (ClassNotFoundException e) {
			throw new InvalidTypeException(e);
		}
	}

	default void register(int type, String className) {
		this.register(type, className, this.getClass().getClassLoader());
	}

	default void registerIfAbsent(int type, Class<? extends PuSerializable> clazz) {
		if (this.getClassForType(type) == null) {
			this.register(type, clazz);
		}
	}

	@SuppressWarnings("unchecked")
	default void registerIfAbsent(int type, String className, ClassLoader classLoader) {
		try {
			this.registerIfAbsent(type, (Class<? extends PuSerializable>) classLoader.loadClass(className));
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Cannot load class for name: " + className);
		}
	}

	default void register(Properties properties, ClassLoader classLoader) {
		if (properties == null) {
			this.getLogger().warn("Properties is null, ignore registering");
		}

		for (Entry<Object, Object> entry : properties.entrySet()) {
			int type = PrimitiveTypeUtils.getIntegerValueFrom(entry.getKey());
			String className = PrimitiveTypeUtils.getStringValueFrom(entry.getValue());
			if (className == null) {
				getLogger().warn("Class name is null for type " + type + ", ignore this entry");
			} else {
				this.register(type, className, classLoader);
			}
		}
	}

	default void register(Properties properties) {
		this.register(properties, this.getClass().getClassLoader());
	}

	default <T extends PuSerializable> T newInstanceForType(int type) {
		Class<T> clazz = this.getClassForType(type);
		if (clazz != null) {
			try {
				return clazz.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				throw new CreateInstanceErrorException("Unable to create instance for class " + clazz.getName(), e);
			}
		} else {
			throw new ClassForTypeNotFoundException("Type " + type + " was not registered for any class");
		}
	}

}
