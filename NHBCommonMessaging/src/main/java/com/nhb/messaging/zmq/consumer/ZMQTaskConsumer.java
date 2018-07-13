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

	@Getter
	private volatile boolean running = false;
	private final AtomicBoolean runningChecker = new AtomicBoolean(false);

	@Getter
	private volatile boolean initialized = false;
	private final AtomicBoolean initializedChecker = new AtomicBoolean(false);

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
			final byte[] messageId = message.getMessageId();
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
					onProcessComplete(builder.responseEndpoint(responseEndpoint).messageId(messageId).build());
				}
			});
			if (message.getData() == null) {
				getLogger().warn("data null...", new NullPointerException());
			}
			this.messageProcessor.process(message.getData(), future);
		}
	}

	protected void onProcessComplete(ZMQResult result) {
		// do nothing
	}

	public final void init(ZMQConsumerConfig config) {
		if (this.initializedChecker.compareAndSet(false, true)) {
			this.config = config;
			this.socketRegistry = config.getSocketRegistry();
			this.receiver = new DisruptorZMQReceiver();
			this.messageProcessor = config.getMessageProcessor();
			this.receiver.init(getSocketRegistry(), this.extractReceiverConfig(config));
			this.onInit();
			this.initialized = true;
		}
	}

	protected void onInit() {
		// should be overrided in sub class
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
				.receivedCountEnabled(config.isReceivedCountEnabled()) //
				.unmashallerSize(config.getUnmashallerSize()) //
				.build();
	}

	public void start() {
		if (this.runningChecker.compareAndSet(false, true)) {
			this.receiver.start();
			this.onStart();
			this.running = true;
		}
	}

	protected void onStart() {
		// do nothing
	}

	@Override
	public void stop() {
		if (this.runningChecker.compareAndSet(true, false)) {
			this.receiver.stop();
			this.onStop();
			this.running = false;
		}

	}

	protected void onStop() {
		// do nothing
	}

	@Override
	public long getReceivedCount() {
		return this.receiver.getReceivedCount();
	}
}
