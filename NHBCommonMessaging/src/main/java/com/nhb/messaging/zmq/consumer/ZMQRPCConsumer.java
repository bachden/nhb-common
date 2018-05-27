package com.nhb.messaging.zmq.consumer;

import java.util.Arrays;
import java.util.Map;

import org.cliffc.high_scale_lib.NonBlockingHashMap;

import com.nhb.common.annotations.NotThreadSafe;
import com.nhb.common.async.Callback;
import com.nhb.common.data.PuArray;
import com.nhb.common.data.PuArrayList;
import com.nhb.common.data.PuDataType;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuValue;
import com.nhb.common.data.exception.InvalidDataException;
import com.nhb.messaging.zmq.DisruptorZMQSender;
import com.nhb.messaging.zmq.ZMQEvent;
import com.nhb.messaging.zmq.ZMQFuture;
import com.nhb.messaging.zmq.ZMQPayloadBuilder;
import com.nhb.messaging.zmq.ZMQPayloadExtractor;
import com.nhb.messaging.zmq.ZMQSender;
import com.nhb.messaging.zmq.ZMQSenderConfig;
import com.nhb.messaging.zmq.ZMQSendingDoneHandler;

public class ZMQRPCConsumer extends ZMQTaskConsumer {

	private final Map<String, ZMQSender> responderRegistry = new NonBlockingHashMap<>();
	private ZMQPayloadExtractor payloadExtractor = new ZMQPayloadExtractor() {

		/**
		 * Single thread access only
		 */
		@Override
		@NotThreadSafe
		public void extractPayload(ZMQEvent event) {
			if (event.getPayload() instanceof PuArray) {
				PuArray puArray = (PuArray) event.getPayload();
				if (puArray.size() > 2) {
					event.setMessageId(puArray.getRaw(0));
					String responseEndpoint = puArray.getString(1);
					if (!responderRegistry.containsKey(responseEndpoint)) {
						ZMQSender sender = new DisruptorZMQSender();
						responderRegistry.put(responseEndpoint, sender);
					}

					event.setResponseEndpoint(responseEndpoint);
					PuValue value = puArray.get(2);
					if (value.getType() == PuDataType.PUARRAY) {
						event.setData(value.getPuArray());
					} else if (value.getType() == PuDataType.PUOBJECT) {
						event.setData(value.getPuObject());
					} else {
						event.setData(value);
					}
				} else {
					event.setSuccess(false);
					event.setFailedCause(new InvalidDataException(
							"PuArray payload must have atleast 3 elements, got " + puArray.size()));
				}
			} else {
				event.setSuccess(false);
				event.setFailedCause(new InvalidDataException("Expected for PuArray, got: " + event.getPayload()));
			}
		}
	};
	private ZMQPayloadBuilder payloadBuilder = new ZMQPayloadBuilder() {

		@Override
		public void buildPayload(ZMQEvent event) {
			if (event.getData() instanceof PuResult) {
				PuResult dummyResult = event.getData().cast();

				PuArray payload = new PuArrayList();
				payload.addFrom(dummyResult.getMessageId());
				payload.addFrom(dummyResult.isSuccess());
				payload.addFrom(dummyResult.getResult());
				event.setPayload(payload);
			}
		}
	};

	public ZMQRPCConsumer() {
		this.setPayloadExtractor(payloadExtractor);
	}

	private ZMQSenderConfig extractSenderConfig(String endpoint, ZMQConsumerConfig config) {
		return ZMQSenderConfig.builder()//
				.threadNamePattern("responder-" + config.getThreadNamePattern()) //
				.sendWorkerSize(config.getSendWorkerSize()) //
				.sendingDoneHandlerSize(config.getSendingDoneHandlerSize()) //
				.queueSize(config.getQueueSize()) //
				.endpoint(endpoint) //
				.socketType(config.getSendSocketType()) //
				.socketOptions(config.getSendSocketOptions()) //
				.payloadBuilder(this.payloadBuilder) //
				.sendingDoneHandler(ZMQSendingDoneHandler.DEFAULT) //
				.socketWriter(config.getSocketWriter()) //
				.build();
	}

	@Override
	protected void onProcessComplete(ZMQResult result) {
		String responseEndpoint = result.getResponseEndpoint();
		if (responseEndpoint != null) {
			ZMQSender responder = this.responderRegistry.get(responseEndpoint);
			if (responder != null) {
				if (!responder.isRunning()) {
					synchronized (responder) {
						if (!responder.isInitialized()) {
							responder.init(getSocketRegistry(), extractSenderConfig(responseEndpoint, getConfig()));
						}
						responder.start();
						getLogger().debug("Responder for {} started...", responseEndpoint);
					}
				}

				final PuResult dummyResult;
				if (result.isSuccess()) {
					dummyResult = new PuResult(result.getMessageId(), result.getResult());
				} else {
					Throwable failedCause = result.getFailedCause();
					dummyResult = new PuResult(result.getMessageId(),
							"Internal server error: " + (failedCause == null ? "unknown" : failedCause.getMessage()));
				}

				final ZMQFuture future = responder.send(dummyResult);
				future.setCallback(new Callback<PuElement>() {

					@Override
					public void apply(PuElement res) {
						if (res == null) {
							getLogger().error("Error while sending response: messageId="
									+ Arrays.toString(result.getMessageId()) + ", responseEndpoint="
									+ result.getResponseEndpoint() + ", data=" + result.getResult(),
									future.getFailedCause());
						}
					}
				});
			}
		}
	}
}
