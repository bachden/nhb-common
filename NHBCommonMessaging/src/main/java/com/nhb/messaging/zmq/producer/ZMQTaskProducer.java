package com.nhb.messaging.zmq.producer;

import java.util.concurrent.atomic.AtomicBoolean;

import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuNull;
import com.nhb.messaging.MessageProducer;
import com.nhb.messaging.zmq.DefaultZMQFuture;
import com.nhb.messaging.zmq.DisruptorZMQSender;
import com.nhb.messaging.zmq.ZMQEvent;
import com.nhb.messaging.zmq.ZMQFuture;
import com.nhb.messaging.zmq.ZMQPayloadBuilder;
import com.nhb.messaging.zmq.ZMQSender;
import com.nhb.messaging.zmq.ZMQSenderConfig;
import com.nhb.messaging.zmq.ZMQSendingDoneHandler;
import com.nhb.messaging.zmq.ZMQSocketRegistry;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public class ZMQTaskProducer implements MessageProducer<ZMQFuture> {

	private final ZMQSendingDoneHandler sendingDoneHandler = new ZMQSendingDoneHandler() {

		@Override
		public void onSendingDone(ZMQEvent message) {
			if (message.isSuccess()) {
				ZMQTaskProducer.this.onSendingSuccess(message);
			} else {
				DefaultZMQFuture future = message.getFuture();
				if (future != null) {
					future.setFailedCause(message.getFailedCause());
					future.setAndDone(null);
				}
			}
		}
	};

	private ZMQSender sender;

	@Getter(AccessLevel.PROTECTED)
	private ZMQSocketRegistry socketRegistry;

	private final AtomicBoolean running = new AtomicBoolean(false);
	private final AtomicBoolean initialized = new AtomicBoolean(false);

	@Getter(AccessLevel.PROTECTED)
	private ZMQProducerConfig config;

	@Getter(AccessLevel.PROTECTED)
	@Setter(AccessLevel.PROTECTED)
	private ZMQPayloadBuilder payloadBuilder;

	public final void init(ZMQProducerConfig config) {
		if (this.initialized.compareAndSet(false, true)) {
			if (this.payloadBuilder == null) {
				this.payloadBuilder = ZMQPayloadBuilder.DEFAULT_PUARRAY_PAYLOAD_BUILDER;
			}

			this.config = config;
			this.socketRegistry = config.getSocketRegistry();

			this.sender = new DisruptorZMQSender();
			this.sender.init(this.socketRegistry, this.extractSenderConfig(config));

			this.onInit();
		} else {
			throw new IllegalStateException("Instance has been started");
		}
	}

	protected void onInit() {

	}

	protected ZMQSenderConfig extractSenderConfig(ZMQProducerConfig config) {
		return ZMQSenderConfig.builder()//
				.threadNamePattern("sender-" + config.getThreadNamePattern()) //
				.sendWorkerSize(config.getSendWorkerSize()) //
				.sendingDoneHandlerSize(config.getSendingDoneHandlerSize()) //
				.queueSize(config.getQueueSize()) //
				.endpoint(config.getSendEndpoint()) //
				.socketType(config.getSendSocketType()) //
				.socketOptions(config.getSendSocketOptions()) //
				.payloadBuilder(this.payloadBuilder) //
				.sendingDoneHandler(this.sendingDoneHandler) //
				.socketWriter(config.getSocketWriter()) //
				.build();
	}

	public final void start() {
		if (this.running.compareAndSet(false, true)) {
			this.sender.start();
			this.onStart();
		}
	}

	protected void onStart() {
		// do nothing
	}

	public final void stop() {
		if (this.running.compareAndSet(true, false)) {
			this.sender.stop();
			this.onStop();
		}
	}

	protected void onStop() {

	}

	public boolean isRunning() {
		return this.running.get();
	}

	public boolean isInitialized() {
		return this.initialized.get();
	}

	@Override
	public ZMQFuture publish(PuElement data) {
		return this.sender.send(data);
	}

	@Override
	public ZMQFuture publish(PuElement data, String routingKey) {
		throw new UnsupportedOperationException();
	}

	protected void onSendingSuccess(ZMQEvent message) {
		DefaultZMQFuture future = message.getFuture();
		if (future != null) {
			future.setAndDone(PuNull.IGNORE_ME);
		}
	}
}
