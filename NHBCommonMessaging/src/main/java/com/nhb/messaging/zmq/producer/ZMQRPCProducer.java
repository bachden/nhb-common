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
import com.nhb.messaging.zmq.ZMQReceiver;
import com.nhb.messaging.zmq.ZMQReceiverConfig;

public class ZMQRPCProducer extends ZMQTaskProducer {

	private ZMQReceiver receiver;
	private final ZMQIdGenerator idGenerator;
	private final ZMQFutureRegistry futureRegistry = new ZMQFutureRegistry();

	public ZMQRPCProducer() {
		this(ZMQIdGenerator.newTimebasedUUIDGenerator());
	}

	public ZMQRPCProducer(ZMQIdGenerator idGenerator) {
		this.idGenerator = idGenerator;
		this.setPayloadBuilder(this::buildPayload);
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
				.receivedMessageHandler(this::onReceive) //
				.payloadExtractor(this::extractReceivedPayload) //
				.receivedCountEnabled(config.isReceivedCountEnable()) //
				.build();
	}

	@Override
	protected void onStart() {
		this.receiver.start();
	}

	@Override
	protected void onStop() {
		this.futureRegistry.cancelAll();
		this.receiver.stop();
	}

	@Override
	protected DefaultZMQFuture createNewFuture() {
		DefaultZMQFuture future = new DefaultZMQFuture();
		future.setRefId(idGenerator.generateId());
		this.futureRegistry.put(future.getRefId(), future);
		future.setCancelCallback(this::onFutureCancelled);
		return future;
	}

	@Override
	protected void onSendingDone(ZMQEvent event) {
		if (!event.isSuccess()) {
			DefaultZMQFuture future = this.futureRegistry.remove(event.getMessageId());
			future.setFailedCause(event.getFailedCause());
			future.setAndDone(null);
		}
	}

	protected void onFutureCancelled(byte[] messageId) {
		if (messageId != null) {
			this.futureRegistry.remove(messageId);
		}
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
		} else {
			getLogger().error("Error while handling received socket data",
					new NullPointerException("Got response but future cannot be found"));
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
		byte[] messageId = event.getFuture().getRefId();

		PuArray payload = new PuArrayList();
		payload.addFrom(messageId);
		payload.addFrom(this.receiver.getEndpoint());
		payload.addFrom(event.getData());

		event.setPayload(payload);
		event.setMessageId(messageId);
	}

	public int remaining() {
		return this.futureRegistry.remaining();
	}

	public long getReceivedCount() {
		return this.receiver.getReceivedCount();
	}
}
