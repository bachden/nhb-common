package com.nhb.messaging.zmq;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;

import org.zeromq.ZMQException;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.WorkHandler;
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
	private Disruptor<ZMQEvent> messageHandler;

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

	private void doInit() {
		this.setReceivedCountEnabled(config.isReceivedCountEnabled());

		ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat(config.getThreadNamePattern()).build();
		messageHandler = new Disruptor<>(ZMQEvent::new, config.getQueueSize(), threadFactory, ProducerType.SINGLE,
				config.getWaitStrategy());

		@SuppressWarnings("unchecked")
		WorkHandler<ZMQEvent>[] workers = new WorkHandler[config.getPoolSize()];
		for (int i = 0; i < workers.length; i++) {
			workers[i] = this::onReceive;
		}

		this.messageHandler.handleEventsWithWorkerPool(workers);
		this.messageHandler.setDefaultExceptionHandler(this.exceptionHandler);

		this.pollingThread = new Thread(this::pollData, "ZMQ " + config.getEndpoint() + " poller");
	}

	private void onReceive(ZMQEvent event) {
		this.config.getPayloadExtractor().extractPayload(event);
		this.config.getReceivedMessageHandler().onReceive(event);
	}

	private void pollData() {
		if (this.socket == null) {
			throw new NullPointerException("Receiver didn't started exception");
		}

		final ByteBuffer buffer = ByteBuffer.allocateDirect(config.getBufferCapacity());
		while (this.isRunning() && !Thread.currentThread().isInterrupted()) {
			buffer.clear();
			int recv = 0;
			try {
				recv = this.socket.recvZeroCopy(buffer, buffer.capacity(), 0);
			} catch (ZMQException e) {
				if (e.getMessage().contains("Context was terminated")) {
					return;
				}
			}
			if (recv == -1) {
				getLogger().error("Error while receive zero copy", new Exception());
				return;
			} else {
				buffer.flip();
				try {
					final PuElement payload = PuElementTemplate.getInstance().read(new ByteBufferInputStream(buffer));

					if (this.receivedCountEnabled) {
						this.receivedCounter++;
					}

					this.messageHandler.publishEvent((event, sequence) -> {
						event.clear();
						event.setPayload(payload);
					});

				} catch (IOException e) {
					getLogger().error("Cannot parse as puElement", e);
				}
			}
		}
	}

	@Override
	public void start() {
		if (this.runningCheckpoint.compareAndSet(false, true)) {
			this.socket = this.socketRegistry.openSocket(config.getEndpoint(), config.getSocketType());
			this.messageHandler.start();
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
			this.messageHandler.shutdown();
			this.socket.close();
			this.running = false;
		}
	}

}
