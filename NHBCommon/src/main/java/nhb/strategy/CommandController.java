package nhb.strategy;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unchecked")
public class CommandController {

	private Map<String, Object> commandProcessorMap;
	private ClassLoader classLoader = this.getClass().getClassLoader();

	private Map<String, Object> environments;

	public CommandController() {
		this.commandProcessorMap = new ConcurrentHashMap<String, Object>();
		this.environments = new ConcurrentHashMap<String, Object>();
	}

	public CommandController(Map<String, Object> commands) {
		this();
		try {
			this.initProcessors(commands);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public Set<String> getRegisteredCommands() {
		return this.commandProcessorMap.keySet();
	}

	public boolean isCommandRegistered(String command) {
		return this.commandProcessorMap.containsKey(command);
	}

	public void initProcessors(Map<String, Object> commands) throws InvalidProcessorType, ClassNotFoundException {
		for (String command : commands.keySet()) {
			Object value = commands.get(command);
			if (value instanceof String) {
				this.registerCommand(command, (String) value);
			} else if (value instanceof Class<?>) {
				this.registerCommand(command, (Class<? extends CommandProcessor>) value);
			} else if (value instanceof CommandProcessor) {
				this.registerCommand(command, (CommandProcessor) value);
			} else {
				throw new InvalidProcessorType("processor type for command `" + command + "` is invalid");
			}
		}
	}

	public void registerCommand(String command, CommandProcessor processor) throws InvalidProcessorType {
		if (processor != null) {
			if (this.commandProcessorMap == null) {
				this.commandProcessorMap = new ConcurrentHashMap<String, Object>();
			}
			this.commandProcessorMap.put(command, processor);
		} else {
			throw new InvalidProcessorType("processor type must be not-null");
		}
	}

	public void registerCommand(String command, String className) throws InvalidProcessorType, ClassNotFoundException {
		this.registerCommand(command, (Class<? extends CommandProcessor>) classLoader.loadClass(className));
	}

	public void registerCommand(String command, Class<? extends CommandProcessor> clazz) throws InvalidProcessorType {
		if (clazz != null) {
			this.commandProcessorMap.put(command, clazz);
		} else {
			throw new InvalidProcessorType("processor type must be not-null");
		}
	}

	public void deregisterCommand(String command) {
		this.commandProcessorMap.remove(command);
	}

	public CommandResponseData processCommand(String command, CommandRequestParameters request)
			throws NoProcessorRegisteredException, InstantiationException, IllegalAccessException,
			InvalidProcessorType {
		if (this.commandProcessorMap != null && this.commandProcessorMap.containsKey(command)) {
			Object processor = this.commandProcessorMap.get(command);
			CommandProcessor concreteProcessor = null;
			if (processor instanceof Class<?>) {
				Class<? extends CommandProcessor> processorType = (Class<? extends CommandProcessor>) processor;
				concreteProcessor = (CommandProcessor) processorType.newInstance();
			} else if (processor instanceof CommandProcessor) {
				concreteProcessor = (CommandProcessor) processor;
			}
			if (concreteProcessor != null) {
				return concreteProcessor.execute(this, request);
			} else {
				throw new InvalidProcessorType("Processor type for command `" + command + "` is invalid");
			}
		}
		throw new NoProcessorRegisteredException("Processor for command " + command + " is not registered");
	}

	public Set<String> getEnvironmentKeys() {
		return environments.keySet();
	}

	public <T> T getEnvironment(String key) {
		return (T) this.environments.get(key);
	}

	public void setEnviroiment(String key, Object value) {
		if (key != null && value != null) {
			this.environments.put(key, value);
		} else {
			throw new IllegalArgumentException("Key and value for environment must be not-null");
		}
	}
}
