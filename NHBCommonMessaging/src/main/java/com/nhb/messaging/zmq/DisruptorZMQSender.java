package com.nhb.messaging.zmq;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.EventTranslator;
import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.WorkHandler;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.nhb.common.Loggable;
import com.nhb.common.data.PuElement;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

public class DisruptorZMQSender implements ZMQSender, Loggable {

	@Slf4j
	private static class SendToSocketWorker implements WorkHandler<ZMQEvent> {

		private final ZMQSocketWriter socketWriter;
		private final ZMQSocket socket;
		private final ZMQPayloadBuilder payloadBuilder;

		private SendToSocketWorker(ZMQSocket socket, ZMQSocketWriter socketWriter, ZMQPayloadBuilder payloadBuilder) {
			if (socket == null) {
				throw new NullPointerException("Socket cannot be null");
			}
			this.socket = socket;
			this.socketWriter = socketWriter;
			this.payloadBuilder = payloadBuilder;
		}

		@Override
		public void onEvent(ZMQEvent event) throws Exception {
			if (event.getData() != null) {
				try {
					this.payloadBuilder.buildPayload(event);
					if (event.isSuccess()) {
						event.setSuccess(this.socketWriter.write(event.getPayload(), this.socket));
						if (!event.isSuccess()) {
							event.setFailedCause(new ZMQSendingException("Cannot send message, unknown exception"));
						}
					} else {
						log.warn("cannot build payload: {}", event.getData());
					}
				} catch (Throwable ex) {
					event.setSuccess(false);
					event.setFailedCause(ex);
				}
			} else {
				event.setSuccess(false);
				event.setFailedCause(new NullPointerException("Data to send cannot be null"));
			}
		}
	}

	@AllArgsConstructor
	private static class SendingDoneWorker implements WorkHandler<ZMQEvent> {
		private final ZMQSendingDoneHandler handler;

		@Override
		public void onEvent(ZMQEvent event) throws Exception {
			handler.onSendingDone(event);
		}
	}

	@Getter(AccessLevel.PROTECTED)
	private ThreadFactory threadFactory;

	private Disruptor<ZMQEvent> disruptor;

	private volatile boolean running = false;
	private final AtomicBoolean runningCheckpoint = new AtomicBoolean(false);

	@Getter
	private volatile boolean initialized = false;
	private final AtomicBoolean initializedCheckpoint = new AtomicBoolean(false);

	private ZMQSocketFactory socketFactory;

	private ZMQPayloadBuilder payloadBuilder;

	public void init(ZMQSocketRegistry socketRegistry, ZMQSenderConfig config) {
		if (initializedCheckpoint.compareAndSet(false, true)) {
			if (config == null) {
				throw new NullPointerException("Config cannot be null");
			} else if (socketRegistry == null) {
				throw new NullPointerException("socketRegistry cannot be null");
			}

			config.validate();

			ZMQSendingDoneHandler sendingDoneHandler = config.getSendingDoneHandler();

			this.payloadBuilder = config.getPayloadBuilder();

			this.socketFactory = new ZMQSocketFactory(socketRegistry, config.getEndpoint(), config.getSocketType(),
					config.getSocketOptions());

			this.threadFactory = new ThreadFactoryBuilder().setNameFormat(config.getThreadNamePattern()).build();
			this.disruptor = new Disruptor<>(ZMQEvent.EVENT_FACTORY, config.getQueueSize(), threadFactory,
					ProducerType.MULTI, new BlockingWaitStrategy());

			SendToSocketWorker[] senders = new SendToSocketWorker[config.getSendWorkerSize()];
			for (int i = 0; i < senders.length; i++) {
				senders[i] = new SendToSocketWorker(socketFactory.newSocket(), config.getSocketWriter(),
						this.payloadBuilder);
			}

			SendingDoneWorker[] sendingDoneHandlers = new SendingDoneWorker[config.getSendingDoneHandlerSize()];
			for (int i = 0; i < sendingDoneHandlers.length; i++) {
				sendingDoneHandlers[i] = new SendingDoneWorker(sendingDoneHandler);
			}

			disruptor.handleEventsWithWorkerPool(senders).thenHandleEventsWithWorkerPool(sendingDoneHandlers);
			disruptor.setDefaultExceptionHandler(new ExceptionHandler<ZMQEvent>() {

				@Override
				public void handleEventException(Throwable ex, long sequence, ZMQEvent event) {
					getLogger().error("Error while handling ZMQEvent: {}", event.getPayload(), ex);
				}

				@Override
				public void handleOnStartException(Throwable ex) {
					getLogger().error("Error while starting sender disruptor", ex);
				}

				@Override
				public void handleOnShutdownException(Throwable ex) {
					getLogger().error("Error while shutting down sender disruptor", ex);
				}
			});
		}
	}

	@Override
	public boolean isRunning() {
		while (this.runningCheckpoint.get() && !this.running) {
			LockSupport.parkNanos(10);
		}
		return this.running;
	}

	@Override
	public final void start() {
		if (this.runningCheckpoint.compareAndSet(false, true)) {
			this.disruptor.start();
			this.running = true;
		} else {
			throw new IllegalStateException("Sender has been started");
		}
	}

	@Override
	public final void stop() {
		if (this.runningCheckpoint.compareAndSet(true, false)) {
			this.disruptor.halt();
			this.socketFactory.destroy();
			this.running = false;
		}
	}

	@Override
	public ZMQFuture send(final PuElement data) {
		final DefaultZMQFuture future = ZMQFuture.newDefault();
		disruptor.publishEvent(new EventTranslator<ZMQEvent>() {

			@Override
			public void translateTo(ZMQEvent event, long sequence) {
				event.clear();
				event.setData(data);
				event.setFuture(future);
			}
		});
		return future;
	}
}
