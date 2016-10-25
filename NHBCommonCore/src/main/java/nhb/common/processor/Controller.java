package nhb.common.processor;

import nhb.common.serializable.PuSerializable;

public interface Controller {

	<T extends PuSerializable, R extends PuSerializable> void registerProcessor(Class<T> messageType,
			Processor<T, R> processor);

	<T extends PuSerializable, R extends PuSerializable> void deregisterProcessor(Class<T> messageType);

	<T extends PuSerializable, R extends PuSerializable> Processor<T, R> getProcessorByType(Class<T> clazz);

	@SuppressWarnings("unchecked")
	default <T extends PuSerializable, R extends PuSerializable> R process(T message) {
		if (message == null) {
			throw new NullPointerException("Message to be processed is null");
		}
		Processor<T, R> processor = (Processor<T, R>) this.getProcessorByType((Class<T>) message.getClass());
		if (processor == null) {
			throw new NullPointerException("Processor didn't found for message type " + message.getClass());
		}
		return processor.process(message);
	}
}
