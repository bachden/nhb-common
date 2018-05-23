package com.nhb.messaging.zmq.producer;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;

import org.zeromq.ZMQ;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.EventTranslator;
import com.lmax.disruptor.WorkHandler;
import com.lmax.disruptor.dsl.Disruptor;
import com.nhb.common.data.PuArray;
import com.nhb.common.data.PuArrayList;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuNull;
import com.nhb.messaging.zmq.ZMQSocket;
import com.nhb.messaging.zmq.ZMQSocketRegistry;
import com.nhb.messaging.zmq.ZMQSocketType;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class ZeroMQSender implements Closeable, ZeroMQMetadataGenerator, ZeroMQSendingDoneHandler {

	@AllArgsConstructor
	private static class Marshaller implements WorkHandler<ZeroMQRequest> {

		private final ZeroMQMetadataGenerator metadataGenerator;

		@Override
		public void onEvent(ZeroMQRequest event) throws Exception {
			event.metadata = this.metadataGenerator.generateMetadata(event.data);

			final PuArray payload = new PuArrayList();
			payload.addFrom(event.metadata);
			payload.addFrom(event.data);

			event.rawData = payload.toBytes();
		}
	}

	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	private static class Sender implements EventHandler<ZeroMQRequest> {

		private final ZeroMQSender producer;

		@Override
		public void onEvent(ZeroMQRequest event, long sequence, boolean endOfBatch) throws Exception {
			this.producer.writeToSocket(event);
		}
	}

	@AllArgsConstructor
	private static class SendingDoneHandler implements WorkHandler<ZeroMQRequest> {
		private final ZeroMQSendingDoneHandler handler;

		@Override
		public void onEvent(ZeroMQRequest event) throws Exception {
			handler.onSendingDone(event);
		}
	}

	@Getter
	private ZMQSocketRegistry socketRegistry;
	@Getter
	private ZMQSocketType socketType;
	@Getter
	private String endpoint;

	private final AtomicBoolean running = new AtomicBoolean(false);
	private ZMQSocket socket;
	private Disruptor<ZeroMQRequest> disruptor;

	@Getter(AccessLevel.PROTECTED)
	private ThreadFactory threadFactory;

	@SuppressWarnings("unchecked")
	public void init(ZMQSocketRegistry socketRegistry, ZeroMQProducerConfig config) {
		if (config == null) {
			throw new NullPointerException("Config cannot be null");
		} else if (config.getSocketType() == null) {
			throw new NullPointerException("config's socketType cannot be null");
		} else if (config.getEndpoint() == null) {
			throw new NullPointerException("config's endpoint cannot be null");
		} else if (socketRegistry == null) {
			throw new NullPointerException("socketRegistry cannot be null");
		}

		this.socketRegistry = socketRegistry;
		this.socketType = config.getSocketType();
		this.endpoint = config.getEndpoint();

		this.threadFactory = new ThreadFactoryBuilder().setNameFormat(config.getThreadNamePattern()).build();
		this.disruptor = new Disruptor<>(ZeroMQRequest.EVENT_FACTORY, config.getRingBufferSize(), threadFactory);

		Marshaller[] marshallers = new Marshaller[config.getMarshallerSize()];
		for (int i = 0; i < marshallers.length; i++) {
			marshallers[i] = new Marshaller(this);
		}

		Sender sender = new Sender(this);

		SendingDoneHandler[] sendingDoneHandlers = new SendingDoneHandler[config.getSendingDoneHandlerSize()];
		for (int i = 0; i < sendingDoneHandlers.length; i++) {
			sendingDoneHandlers[i] = new SendingDoneHandler(this);
		}

		disruptor.handleEventsWithWorkerPool(marshallers).then(sender)
				.thenHandleEventsWithWorkerPool(sendingDoneHandlers);

	}

	public final boolean isRunning() {
		return running.get();
	}

	public final void start() {
		if (this.running.compareAndSet(false, true)) {
			this.socket = this.socketRegistry.openSocket(this.getEndpoint(), this.getSocketType());
			this.disruptor.start();
			this._start();
		} else {
			throw new IllegalStateException("Producer is running");
		}
	}

	protected void _start() {

	}

	@Override
	public final void close() throws IOException {
		if (this.running.compareAndSet(true, false)) {
			if (this.socket != null) {
				this.socket.close();
				this._close();
			}
		}
	}

	protected void _close() throws IOException {

	}

	/**
	 * Single thread access only
	 * 
	 * @param data
	 */
	private void writeToSocket(ZeroMQRequest request) {
		if (this.socket.send(request.rawData, ZMQ.NOBLOCK)) {
			request.success = true;
		} else {
			request.success = false;
		}
	}

	public ZeroMQRPCFuture send(final PuElement data) {

		final DefaultZeroMQRPCFuture future = new DefaultZeroMQRPCFuture();

		this.disruptor.publishEvent(new EventTranslator<ZeroMQRequest>() {
			@Override
			public void translateTo(ZeroMQRequest event, long sequence) {
				event.clear();
				event.data = data;
				event.future = future;
			}
		});

		return future;
	}

	@Override
	public PuArray generateMetadata(PuElement data) {
		return null;
	}

	/**
	 * should be overrided by sub-class
	 * 
	 * @param request
	 */
	protected void onSendingSuccess(ZeroMQRequest request) {
		request.future.setAndDone(PuNull.IGNORE_ME);
	}

	@Override
	public final void onSendingDone(ZeroMQRequest request) {
		if (request.success) {
			this.onSendingSuccess(request);
		} else {
			request.future.setFailedCause(
					request.failedCause != null ? request.failedCause : new ZeroMQSendingFailException());
			request.future.setAndDone(null);
		}
	}
}
