package com.nhb.messaging.zmq.consumer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.cliffc.high_scale_lib.NonBlockingHashMap;

import com.nhb.common.async.Callback;
import com.nhb.common.data.PuArray;
import com.nhb.common.data.PuArrayList;
import com.nhb.common.data.PuDataType;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuValue;
import com.nhb.common.data.exception.InvalidDataException;
import com.nhb.common.flag.SemaphoreFlag;
import com.nhb.messaging.zmq.DisruptorZMQSender;
import com.nhb.messaging.zmq.ZMQEvent;
import com.nhb.messaging.zmq.ZMQFuture;
import com.nhb.messaging.zmq.ZMQSender;
import com.nhb.messaging.zmq.ZMQSenderConfig;
import com.nhb.messaging.zmq.ZMQSendingDoneHandler;

public class ZMQRPCConsumer extends ZMQTaskConsumer {

	private final Map<String, ZMQSender> responderRegistry = new NonBlockingHashMap<>();

	private Thread idleResponderCleaner;
	private final AtomicBoolean shuttingDownFlag = new AtomicBoolean(false);
	private final SemaphoreFlag responderCleanBarrier = SemaphoreFlag.newWithLowerBound(0);

	public ZMQRPCConsumer() {
		this.setPayloadExtractor(this::extractReceivedPayload);
	}

	@Override
	protected void onInit() {
		if (this.getConfig().getResponderMaxIdleMinutes() > 0) {
			idleResponderCleaner = new Thread(this::cleanIdleResponders);
		}
	}

	@Override
	protected void onStart() {
		if (this.idleResponderCleaner != null) {
			this.idleResponderCleaner.start();
		}
		this.shuttingDownFlag.set(false);
	}

	private void cleanIdleResponders() {
		getLogger().debug("Start idle responder cleaner thread");
		while (!Thread.currentThread().isInterrupted()) {
			try {
				Thread.sleep((long) 6e4); // 1 minute
			} catch (InterruptedException e) {
				break;
			}

			responderCleanBarrier.lockIncrementAndWaitFor(0, 10, this.shuttingDownFlag);
			try {
				Set<String> tobeRemoved = new HashSet<>();
				for (Entry<String, ZMQSender> entry : this.responderRegistry.entrySet()) {
					if (entry.getValue().getIdleTime(TimeUnit.MINUTES) > this.getConfig()
							.getResponderMaxIdleMinutes()) {
						tobeRemoved.add(entry.getKey());
					}
				}

				for (String endpoint : tobeRemoved) {
					ZMQSender responder = this.responderRegistry.remove(endpoint);
					if (responder != null) {
						getLogger().debug("Stoping responder for endpoint {} because of idle more than {} minutes",
								endpoint, this.getConfig().getResponderMaxIdleMinutes());
						try {
							responder.stop();
						} catch (Exception ex) {
							getLogger().error("Cannot stop responder for endpoint {}", endpoint);
						}
					}
				}
			} finally {
				responderCleanBarrier.unlockIncrement();
			}
		}
	}

	private ZMQSender getResponder(String endpoint) {
		if (endpoint != null) {
			this.responderCleanBarrier.incrementAndGet(shuttingDownFlag);
			try {
				ZMQSender responder = this.responderRegistry.get(endpoint);

				if (responder == null) {
					responder = new DisruptorZMQSender();
					ZMQSender old = this.responderRegistry.putIfAbsent(endpoint, responder);
					if (old != null) {
						responder = old;
					}
				}

				if (!responder.isRunning()) {
					if (!responder.isInitialized()) {
						responder.init(getSocketRegistry(), extractSenderConfig(endpoint, getConfig()));
					}
					responder.start();
					getLogger().debug("Responder for {} started...", endpoint);
				}

				return responder;
			} finally {
				this.responderCleanBarrier.decrementAndGet(shuttingDownFlag);
			}
		}
		return null;
	}

	protected void extractReceivedPayload(ZMQEvent event) {
		if (event.getPayload() instanceof PuArray) {
			PuArray puArray = (PuArray) event.getPayload();
			if (puArray.size() > 2) {
				event.setMessageId(puArray.getRaw(0));
				String responseEndpoint = puArray.getString(1);

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

	protected void buildResponsePayload(ZMQEvent event) {
		if (event.getData() instanceof PuResult) {
			PuResult dummyResult = event.getData().cast();
			PuArray payload = new PuArrayList();
			payload.addFrom(dummyResult.getMessageId());
			payload.addFrom(dummyResult.isSuccess());
			payload.addFrom(dummyResult.getResult());
			event.setPayload(payload);
			event.setSuccess(true);
		} else {
			event.setFailedCause(new InvalidDataException("Expected PuResult instance"));
			event.setSuccess(false);
		}
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
				.payloadBuilder(this::buildResponsePayload) //
				.sendingDoneHandler(ZMQSendingDoneHandler.DEFAULT) //
				.socketWriter(config.getSocketWriter()) //
				.sentCountEnabled(config.isRespondedCountEnabled()) //
				.build();
	}

	public long getRespondedCount() {
		long sum = 0;
		for (ZMQSender sender : this.responderRegistry.values()) {
			sum += sender.getSentCount();
		}
		return sum;
	}

	@Override
	protected void onProcessComplete(ZMQResult result) {
		String responseEndpoint = result.getResponseEndpoint();
		if (responseEndpoint != null) {
			ZMQSender responder = this.getResponder(responseEndpoint);
			if (responder != null) {
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
							String msg = "Error while sending response: messageId="
									+ Arrays.toString(result.getMessageId()) + ", responseEndpoint="
									+ result.getResponseEndpoint() + ", data=" + result.getResult();

							getLogger().error(msg, future.getFailedCause() != null ? future.getFailedCause()
									: new Exception("Unknown error"));
						}
					}
				});
			} else {
				getLogger().error("Responder cannot be create for endpoint: " + responseEndpoint,
						new NullPointerException("Null responder, may endpoint is unmalformed"));
			}
		}
	}

	@Override
	protected void onStop() {
		this.shuttingDownFlag.set(true);
		if (this.idleResponderCleaner != null) {
			this.idleResponderCleaner.interrupt();
		}

		for (ZMQSender responder : this.responderRegistry.values()) {
			responder.stop();
		}
	}
}
