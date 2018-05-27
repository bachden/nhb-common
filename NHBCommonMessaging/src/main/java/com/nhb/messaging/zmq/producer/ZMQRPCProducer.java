package com.nhb.messaging.zmq.producer;

import com.nhb.common.data.PuArray;
import com.nhb.common.data.PuArrayList;
import com.nhb.common.data.PuDataType;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuValue;
import com.nhb.messaging.zmq.DefaultZMQFuture;
import com.nhb.messaging.zmq.DisruptorZMQReceiver;
import com.nhb.messaging.zmq.ZMQEvent;
import com.nhb.messaging.zmq.ZMQFutureRegistry;
import com.nhb.messaging.zmq.ZMQIdGenerator;
import com.nhb.messaging.zmq.ZMQPayloadBuilder;
import com.nhb.messaging.zmq.ZMQPayloadExtractor;
import com.nhb.messaging.zmq.ZMQReceivedMessageHandler;
import com.nhb.messaging.zmq.ZMQReceiver;
import com.nhb.messaging.zmq.ZMQReceiverConfig;

public class ZMQRPCProducer extends ZMQTaskProducer {

	private ZMQReceiver receiver;
	private final ZMQIdGenerator idGenerator;
	private final ZMQFutureRegistry futureRegistry = new ZMQFutureRegistry();

	private final ZMQReceivedMessageHandler receivedMessageHandler = new ZMQReceivedMessageHandler() {

		@Override
		public void onReceive(ZMQEvent message) {
			ZMQRPCProducer.this.onReceive(message);
		}
	};

	private ZMQPayloadBuilder payloadBuilder = new ZMQPayloadBuilder() {

		@Override
		public void buildPayload(ZMQEvent event) {
			ZMQRPCProducer.this.buildPayload(event);
		}
	};

	private ZMQPayloadExtractor payloadExtractor = new ZMQPayloadExtractor() {

		@Override
		public void extractPayload(ZMQEvent event) {
			ZMQRPCProducer.this.extractReceivedPayload(event);
		}
	};

	public ZMQRPCProducer() {
		this(ZMQIdGenerator.newTimebasedUUIDGenerator());
	}

	public ZMQRPCProducer(ZMQIdGenerator idGenerator) {
		this.idGenerator = idGenerator;
		this.setPayloadBuilder(this.payloadBuilder);
	}

	@Override
	protected void onInit() {
		this.receiver = new DisruptorZMQReceiver();
		this.receiver.init(getSocketRegistry(), extractReceiverConfig(this.getConfig()));
	}

	private ZMQReceiverConfig extractReceiverConfig(ZMQProducerConfig config) {
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
	protected void onStart() {
		this.receiver.start();
	}

	@Override
	protected void onStop() {
		this.receiver.stop();
	}

	@Override
	protected void onSendingSuccess(ZMQEvent event) {
		byte[] messageId = event.getMessageId();
		this.futureRegistry.put(messageId, event.getFuture());
	}

	private void onReceive(ZMQEvent event) {
		DefaultZMQFuture future = event.getFuture();
		if (future != null) {
			if (event.isSuccess()) {
				future.setAndDone(event.getData());
			} else {
				future.setFailedCause(event.getFailedCause());
				future.setAndDone(null);
			}
		}
	}

	private void extractReceivedPayload(ZMQEvent event) {
		PuElement payload = event.getPayload();
		if (payload instanceof PuArray && ((PuArray) payload).size() >= 2) {
			PuArray puArray = (PuArray) payload;
			byte[] messageId = puArray.getRaw(0);
			DefaultZMQFuture future = this.futureRegistry.remove(messageId);

			event.setSuccess(puArray.getBoolean(1));
			event.setFuture(future);

			if (puArray.size() > 2) {
				PuValue value = puArray.get(2);
				if (value.getType() == PuDataType.PUARRAY) {
					event.setData(value.getPuArray());
				} else if (value.getType() == PuDataType.PUOBJECT) {
					event.setData(value.getPuObject());
				} else {
					event.setData(value);
				}
			}

			if (!event.isSuccess()) {
				event.setFailedCause(new Exception("Internal server error, message: " + event.getData()));
			}
		} else {
			event.setSuccess(false);
			event.setFailedCause(new IllegalArgumentException("Cannot extract payload: " + payload));
		}
	}

	private void buildPayload(ZMQEvent event) {
		byte[] messageId = this.idGenerator.generateId();

		PuArray payload = new PuArrayList();
		payload.addFrom(messageId);
		payload.addFrom(this.receiver.getEndpoint());
		payload.addFrom(event.getData());

		event.setPayload(payload);
		event.setMessageId(messageId);
	}
}
