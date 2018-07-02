package com.nhb.messaging.zmq.producer;

import java.util.concurrent.atomic.AtomicBoolean;

import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuNull;
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

public class ZMQTaskProducer extends ZMQProducer {

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

	@Getter
	private volatile boolean running = false;
	private final AtomicBoolean runningChecker = new AtomicBoolean(false);
	@Getter
	private volatile boolean initialized = false;
	private final AtomicBoolean initializedChecker = new AtomicBoolean(false);

	@Getter(AccessLevel.PROTECTED)
	private ZMQProducerConfig config;

	@Getter(AccessLevel.PROTECTED)
	@Setter(AccessLevel.PROTECTED)
	private ZMQPayloadBuilder payloadBuilder;

	public final void init(ZMQProducerConfig config) {
		if (this.initializedChecker.compareAndSet(false, true)) {
			if (this.payloadBuilder == null) {
				this.payloadBuilder = ZMQPayloadBuilder.DEFAULT_PUARRAY_PAYLOAD_BUILDER;
			}

			this.config = config;
			this.socketRegistry = config.getSocketRegistry();

			this.sender = new DisruptorZMQSender();
			this.sender.init(this.socketRegistry, this.extractSenderConfig(config));

			this.onInit();
			this.initialized = true;
		}
	}

	protected void onInit() {

	}

	private ZMQSenderConfig extractSenderConfig(ZMQProducerConfig config) {
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
				.sentCountEnabled(config.isSentCountEnabled()) //
				.build();
	}

	@Override
	public long getSentCount() {
		return this.sender.getSentCount();
	}

	public final void start() {
		if (this.runningChecker.compareAndSet(false, true)) {
			this.sender.start();
			this.onStart();
			this.running = true;
			getLogger().debug("{} producer started, target: {}", this.getName(), this.getConfig().getSendEndpoint());
		}
	}

	protected void onStart() {
		// do nothing
	}

	public final void stop() {
		if (this.runningChecker.compareAndSet(true, false)) {
			this.sender.stop();
			this.onStop();
			this.running = false;
		}
	}

	protected void onStop() {

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
