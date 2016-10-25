package nhb.common.serializable.impl;

import java.util.concurrent.atomic.AtomicInteger;

import nhb.common.serializable.PuSerializable;

public class AutoIncrementTypeSerializableFactory extends BasePuSerializableFactory {

	private final AtomicInteger idSeed = new AtomicInteger(0);

	/**
	 * 
	 * @param clazz
	 *            class to be registered
	 * @return the type value assigned to class
	 */
	public <T extends PuSerializable> int register(Class<T> clazz) {
		int type = idSeed.incrementAndGet();
		this.register(type, clazz);
		return type;
	}

	public <T extends PuSerializable> int register(String className, ClassLoader classLoader) {
		int type = idSeed.incrementAndGet();
		this.register(type, className, classLoader);
		return type;
	}
}
