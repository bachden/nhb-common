package com.nhb.messaging.zmq.consumer;

import java.util.concurrent.atomic.AtomicBoolean;

import com.nhb.common.Loggable;
import com.nhb.common.async.Callback;
import com.nhb.common.data.PuElement;
import com.nhb.messaging.zmq.DefaultZMQFuture;
import com.nhb.messaging.zmq.DisruptorZMQReceiver;
import com.nhb.messaging.zmq.ZMQEvent;
import com.nhb.messaging.zmq.ZMQPayloadExtractor;
import com.nhb.messaging.zmq.ZMQReceivedMessageHandler;
import com.nhb.messaging.zmq.ZMQReceiver;
import com.nhb.messaging.zmq.ZMQReceiverConfig;
import com.nhb.messaging.zmq.ZMQSocketRegistry;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public class ZMQTaskConsumer implements ZMQConsumer, Loggable {

	@Getter(AccessLevel.PROTECTED)
	private ZMQSocketRegistry socketRegistry;

	private final AtomicBoolean running = new AtomicBoolean(false);
	private ZMQReceiver receiver;

	@Setter(AccessLevel.PROTECTED)
	private ZMQPayloadExtractor payloadExtractor = ZMQPayloadExtractor.DEFAULT_PUARRAY_PAYLOAD_EXTRACTOR;

	private ZMQMessageProcessor messageProcessor;

	private final ZMQReceivedMessageHandler receivedMessageHandler = new ZMQReceivedMessageHandler() {

		@Override
		public void onReceive(ZMQEvent message) {
			ZMQTaskConsumer.this.onReceive(message);
		}
	};

	@Getter(AccessLevel.PROTECTED)
	private ZMQConsumerConfig config;

	private void onReceive(ZMQEvent message) {
		if (this.messageProcessor != null) {
			final DefaultZMQFuture future = new DefaultZMQFuture();
			final String responseEndpoint = message.getResponseEndpoint();
			future.setCallback(new Callback<PuElement>() {

				@Override
				public void apply(PuElement result) {
					ZMQResult.ZMQResultBuilder builder = ZMQResult.builder();
					if (result == null) {
						Throwable cause = future.getFailedCause();
						if (cause == null) {
							cause = new ZMQProcessException("Message process error");
						}
						builder.success(false).failedCause(cause);
					} else {
						builder.success(true).result(result);
					}
					onProcessComplete(builder.responseEndpoint(responseEndpoint).build());
				}
			});
			this.messageProcessor.process(message.getData(), future);
		}
	}

	protected void onProcessComplete(ZMQResult result) {
		// do nothing
	}

	public final void init(ZMQConsumerConfig config) {
		this.config = config;
		this.socketRegistry = config.getSocketRegistry();
		this.receiver = new DisruptorZMQReceiver();
		this.messageProcessor = config.getMessageProcessor();
		this.receiver.init(getSocketRegistry(), this.extractReceiverConfig(config));
		this.onInit();
	}

	protected void onInit() {

	}

	private ZMQReceiverConfig extractReceiverConfig(ZMQConsumerConfig config) {
		return ZMQReceiverConfig.builder() //
				.threadNamePattern("receiver-" + config.getThreadNamePattern()) //
				.endpoint(config.getReceiveEndpoint()) //
				.socketType(config.getReceiveSocketType()) //
				.bufferCapacity(config.getBufferCapacity()) //
				.poolSize(config.getReceiveWorkerSize()) //
				.receivedMessageHandler(this.receivedMessageHandler) //
				.payloadExtractor(this.payloadExtractor) //
				.build();
	}

	@Override
	public boolean isRunning() {
		return this.running.get();
	}

	public void start() {
		if (this.running.compareAndSet(false, true)) {
			this.receiver.start();
			this.onStart();
		}
	}

	protected void onStart() {
		// do nothing
	}

	@Override
	public void stop() {
		if (this.running.compareAndSet(true, false)) {
			this.onStop();
		}

	}

	protected void onStop() {
		// do nothing
	}
}
