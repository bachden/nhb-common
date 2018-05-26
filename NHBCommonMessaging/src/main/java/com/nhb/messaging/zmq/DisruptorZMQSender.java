package com.nhb.messaging.zmq;

import java.nio.ByteBuffer;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;

import org.zeromq.ZMQ;
import org.zeromq.ZMQException;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.EventTranslator;
import com.lmax.disruptor.WorkHandler;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.nhb.common.data.PuElement;
import com.nhb.common.vo.ByteBufferOutputStream;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class DisruptorZMQSender implements ZMQSender {

	private static class ZeroCopySender implements WorkHandler<ZMQEvent> {

		private final ZMQSocket socket;
		private final ByteBuffer buffer;
		private final ZMQPayloadBuilder payloadBuilder;

		private ZeroCopySender(ZMQSocket socket, int bufferSize, ZMQPayloadBuilder payloadBuilder) {
			if (socket == null) {
				throw new NullPointerException("Socket cannot be null");
			}
			this.socket = socket;
			this.buffer = ByteBuffer.allocateDirect(bufferSize);
			this.payloadBuilder = payloadBuilder;
		}

		@Override
		public void onEvent(ZMQEvent event) throws Exception {
			if (event.getData() != null) {
				this.buffer.clear();
				this.payloadBuilder.buildPayload(event);
				event.getPayload().writeTo(new ByteBufferOutputStream(buffer));
				this.buffer.flip();
				try {
					event.setSuccess(this.socket.sendZeroCopy(buffer, buffer.remaining(), ZMQ.NOBLOCK));
					if (!event.isSuccess()) {
						event.setFailedCause(new ZMQException("Cannot send message", -1));
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

	private final AtomicBoolean running = new AtomicBoolean(false);

	private ZMQSocketFactory socketFactory;

	private ZMQPayloadBuilder payloadBuilder;

	@Override
	public boolean isRunning() {
		return this.running.get();
	}

	public void init(ZMQSocketRegistry socketRegistry, ZMQSenderConfig config) {
		if (config == null) {
			throw new NullPointerException("Config cannot be null");
		} else if (socketRegistry == null) {
			throw new NullPointerException("socketFactory cannot be null");
		}

		config.validate();

		ZMQSendingDoneHandler sendingDoneHandler = config.getSendingDoneHandler();

		this.payloadBuilder = config.getPayloadBuilder();

		this.socketFactory = new ZMQSocketFactory(socketRegistry, config.getEndpoint(), config.getSocketType(),
				config.getSocketOptions());

		this.threadFactory = new ThreadFactoryBuilder().setNameFormat(config.getThreadNamePattern()).build();
		this.disruptor = new Disruptor<>(ZMQEvent.EVENT_FACTORY, config.getQueueSize(), threadFactory,
				ProducerType.MULTI, new BlockingWaitStrategy());

		ZeroCopySender[] senders = new ZeroCopySender[config.getSendWorkerSize()];
		for (int i = 0; i < senders.length; i++) {
			senders[i] = new ZeroCopySender(socketFactory.newSocket(), config.getBufferCapacity(), this.payloadBuilder);
		}

		SendingDoneWorker[] sendingDoneHandlers = new SendingDoneWorker[config.getSendingDoneHandlerSize()];
		for (int i = 0; i < sendingDoneHandlers.length; i++) {
			sendingDoneHandlers[i] = new SendingDoneWorker(sendingDoneHandler);
		}

		disruptor.handleEventsWithWorkerPool(senders).thenHandleEventsWithWorkerPool(sendingDoneHandlers);
	}

	@Override
	public final void start() {
		if (this.running.compareAndSet(false, true)) {
			this.disruptor.start();
		} else {
			throw new IllegalStateException("Sender has been started");
		}
	}

	@Override
	public final void stop() {
		if (this.running.compareAndSet(true, false)) {
			this.disruptor.halt();
			this.socketFactory.destroy();
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
