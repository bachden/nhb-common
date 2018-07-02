package com.nhb.messaging.zmq;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;

import org.zeromq.ZMQException;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.EventTranslator;
import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.WorkHandler;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.nhb.common.Loggable;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.msgpkg.PuElementTemplate;
import com.nhb.common.vo.ByteBufferInputStream;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

public class DisruptorZMQReceiver implements ZMQReceiver, Loggable {

	@AllArgsConstructor
	private static class MessageReceivingWorker implements WorkHandler<ZMQEvent> {

		private final ZMQReceivedMessageHandler handler;

		@Override
		public void onEvent(ZMQEvent event) throws Exception {
			this.handler.onReceive(event);
		}
	}

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
	private Disruptor<ZMQEvent> disruptor;

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
			this.payloadExtractor = config.getPayloadExtractor();

			this.setReceivedCountEnabled(config.isReceivedCountEnabled());

			ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat(config.getThreadNamePattern())
					.build();
			disruptor = new Disruptor<>(ZMQEvent.EVENT_FACTORY, config.getQueueSize(), threadFactory,
					ProducerType.SINGLE, new BlockingWaitStrategy());

			final ZMQReceivedMessageHandler receivedMessageHandler = config.getReceivedMessageHandler();

			MessageReceivingWorker[] workers = new MessageReceivingWorker[config.getPoolSize()];
			for (int i = 0; i < workers.length; i++) {
				workers[i] = new MessageReceivingWorker(receivedMessageHandler);
			}

			this.disruptor.handleEventsWithWorkerPool(workers);
			disruptor.setDefaultExceptionHandler(this.exceptionHandler);

			this.initialized = true;
		}
	}

	@Override
	public void start() {
		if (this.runningCheckpoint.compareAndSet(false, true)) {
			this.socket = this.socketRegistry.openSocket(config.getEndpoint(), config.getSocketType());
			this.pollingThread = new Thread(() -> {
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
							final PuElement payload = PuElementTemplate.getInstance()
									.read(new ByteBufferInputStream(buffer));
							if (this.receivedCountEnabled) {
								this.receivedCounter++;
							}
							this.disruptor.publishEvent(new EventTranslator<ZMQEvent>() {

								@Override
								public void translateTo(ZMQEvent event, long sequence) {
									event.clear();
									event.setPayload(payload);
									DisruptorZMQReceiver.this.payloadExtractor.extractPayload(event);
								}
							});
						} catch (IOException e) {
							getLogger().error("Cannot parse as puElement", e);
						}
					}
				}
			});
			this.disruptor.start();
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
			this.disruptor.halt();
			this.socket.close();
			this.running = false;
		}
	}

}
