package com.nhb.messaging.zmq;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;

import org.zeromq.ZMQException;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.WorkHandler;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.nhb.common.Loggable;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.msgpkg.PuElementTemplate;
import com.nhb.common.vo.ByteBufferInputStream;

import lombok.Getter;
import lombok.Setter;

public class DisruptorZMQReceiver implements ZMQReceiver, Loggable {

	@Getter
	private volatile boolean initialized = false;
	private final AtomicBoolean initializedCheckpoint = new AtomicBoolean(false);

	@Getter
	private volatile boolean running = false;
	private final AtomicBoolean runningCheckpoint = new AtomicBoolean(false);

	@Setter
	private volatile boolean receivedCountEnabled = false;
	private volatile long receivedCounter = 0;

	private ZMQSocket socket;
	private Thread pollingThread;
	private ZMQSocketRegistry socketRegistry;
	private ZMQReceiverConfig config;
	private Disruptor<ZMQEvent> handlerPool;
	private Disruptor<ByteBuffer> unmashallerPool;

	private ZMQPayloadExtractor payloadExtractor;
	private ExceptionHandler<ZMQEvent> exceptionHandler = new ExceptionHandler<ZMQEvent>() {

		@Override
		public void handleEventException(Throwable ex, long sequence, ZMQEvent event) {
			getLogger().error("Error while handling ZMQEvent: {}", event.getPayload(), ex);
			if (event.getFuture() != null && !event.getFuture().isDone()) {
				event.getFuture().setFailedCause(ex);
				event.getFuture().setAndDone(null);
			}
		}

		@Override
		public void handleOnStartException(Throwable ex) {
			getLogger().error("Error while starting sender disruptor", ex);
		}

		@Override
		public void handleOnShutdownException(Throwable ex) {
			getLogger().error("Error while shutting down sender disruptor", ex);
		}
	};;

	@Override
	public String getEndpoint() {
		if (this.socket != null) {
			return this.socket.getAddress();
		}
		return null;
	}

	@Override
	public long getReceivedCount() {
		return this.receivedCounter;
	}

	@Override
	public void init(ZMQSocketRegistry registry, ZMQReceiverConfig config) {
		if (this.initializedCheckpoint.compareAndSet(false, true)) {
			if (registry == null) {
				throw new NullPointerException("Socket registry cannot be null");
			} else if (config == null) {
				throw new NullPointerException("Config cannot be null");
			}

			config.validate();

			this.config = config;
			this.socketRegistry = registry;

			doInit();

			this.initialized = true;
		}
	}

	private ByteBuffer createByteBuffer() {
		return ByteBuffer.allocateDirect(config.getBufferCapacity());
	}

	private void doInit() {
		this.payloadExtractor = config.getPayloadExtractor();

		this.setReceivedCountEnabled(config.isReceivedCountEnabled());

		ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat(config.getThreadNamePattern()).build();

		initUnmashallerPool(threadFactory);
		initHandlerPool(threadFactory);

		this.pollingThread = new Thread(this::pollData, "ZMQ " + config.getEndpoint() + " poller");

	}

	private void initUnmashallerPool(ThreadFactory threadFactory) {
		@SuppressWarnings("unchecked")
		WorkHandler<ByteBuffer>[] unmashallers = new WorkHandler[config.getUnmashallerSize()];
		for (int i = 0; i < unmashallers.length; i++) {
			unmashallers[i] = this::unmashall;
		}
		getLogger().debug("Unmashaller size: {}", unmashallers.length);
		this.unmashallerPool = new Disruptor<>(this::createByteBuffer, unmashallers.length * 2, threadFactory,
				ProducerType.SINGLE, new YieldingWaitStrategy());

		this.unmashallerPool.handleEventsWithWorkerPool(unmashallers);

	}

	private void initHandlerPool(ThreadFactory threadFactory) {
		final ZMQReceivedMessageHandler receivedMessageHandler = config.getReceivedMessageHandler();

		@SuppressWarnings("unchecked")
		WorkHandler<ZMQEvent>[] workers = new WorkHandler[config.getPoolSize()];
		for (int i = 0; i < workers.length; i++) {
			workers[i] = receivedMessageHandler::onReceive;
		}

		this.handlerPool = new Disruptor<>(ZMQEvent::new, config.getQueueSize(), threadFactory,
				config.getUnmashallerSize() == 1 ? ProducerType.SINGLE : ProducerType.MULTI,
				new YieldingWaitStrategy());
		this.handlerPool.handleEventsWithWorkerPool(workers);
		this.handlerPool.setDefaultExceptionHandler(this.exceptionHandler);
	}

	private void unmashall(ByteBuffer buffer) {
		if (buffer.position() > 0) {
			buffer.flip();
			try {
				final PuElement payload = PuElementTemplate.getInstance().read(new ByteBufferInputStream(buffer));

				this.handlerPool.publishEvent((event, sequence) -> {
					event.clear();
					event.setPayload(payload);
					this.payloadExtractor.extractPayload(event);
				});

			} catch (IOException e) {
				getLogger().error("Cannot parse as puElement", e);
			}
		}
	}

	private void pollData() {
		if (this.socket == null) {
			throw new NullPointerException("Receiver didn't started exception");
		}

		AtomicBoolean hasError = new AtomicBoolean(false);
		while (this.isRunning() && !hasError.get() && !Thread.currentThread().isInterrupted()) {
			this.unmashallerPool.publishEvent((buffer, sequence) -> {
				buffer.clear();
				int recv = 0;

				try {
					recv = this.socket.recvZeroCopy(buffer, buffer.capacity(), 0);
				} catch (ZMQException e) {
					if (e.getMessage().contains("Context was terminated")) {
						hasError.set(true);
					}
				}

				if (recv == -1) {
					getLogger().error("Error while receive zero copy", new Exception());
					buffer.clear();
					hasError.set(true);
				} else if (this.receivedCountEnabled) {
					this.receivedCounter++;
				}
			});
		}

	}

	@Override
	public void start() {
		if (this.runningCheckpoint.compareAndSet(false, true)) {
			this.socket = this.socketRegistry.openSocket(config.getEndpoint(), config.getSocketType());
			this.unmashallerPool.start();
			this.handlerPool.start();
			this.pollingThread.start();
			this.running = true;
		}
	}

	@Override
	public void stop() {
		if (this.runningCheckpoint.compareAndSet(true, false)) {
			if (!this.pollingThread.isInterrupted()) {
				this.pollingThread.interrupt();
			}
			this.unmashallerPool.shutdown();
			this.handlerPool.shutdown();
			this.socket.close();
			this.running = false;
		}
	}

}
