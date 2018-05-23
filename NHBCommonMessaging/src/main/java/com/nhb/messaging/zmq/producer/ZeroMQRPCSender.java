package com.nhb.messaging.zmq.producer;

import java.io.IOException;
import java.util.Map;

import org.cliffc.high_scale_lib.NonBlockingHashMap;

import com.lmax.disruptor.EventTranslator;
import com.lmax.disruptor.WorkHandler;
import com.lmax.disruptor.dsl.Disruptor;
import com.nhb.common.data.PuArray;
import com.nhb.common.data.PuArrayList;
import com.nhb.common.data.PuDataType;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuValue;
import com.nhb.common.data.exception.InvalidDataException;
import com.nhb.common.data.msgpkg.PuElementTemplate;
import com.nhb.common.utils.UUIDUtils;
import com.nhb.common.vo.ByteArrayWrapper;
import com.nhb.messaging.zmq.ZMQSocket;
import com.nhb.messaging.zmq.ZMQSocketRegistry;
import com.nhb.messaging.zmq.ZMQSocketType;

import lombok.AllArgsConstructor;

public class ZeroMQRPCSender extends ZeroMQSender implements ZeroMQMetadataExtractor {

	@AllArgsConstructor
	private static class Unmarshaller implements WorkHandler<ZeroMQResponse> {

		private final ZeroMQMetadataExtractor metadataExtractor;

		@Override
		public void onEvent(ZeroMQResponse event) throws Exception {
			try {
				PuElement resp = PuElementTemplate.getInstance().read(event.rawData);
				if (!(resp instanceof PuArray)) {
					event.failedCause = new InvalidDataException("Response must be PuArray, got: " + resp);
					event.error = true;
				} else {
					PuArray metadata = ((PuArray) resp).getPuArray(0);
					this.metadataExtractor.extractMetadata(metadata, event);
					PuValue data = ((PuArray) resp).get(1);
					event.setData(data.getType() == PuDataType.PUARRAY ? data.getPuArray()
							: (data.getType() == PuDataType.PUOBJECT ? data.getPuObject() : data));
				}
			} catch (Exception e) {
				event.error = true;
				event.failedCause = e;
			}
		}
	}

	static class FutureRegistry {
		private final Map<ByteArrayWrapper, DefaultZeroMQRPCFuture> registry = new NonBlockingHashMap<>();

		private ByteArrayWrapper wrap(byte[] key) {
			return ByteArrayWrapper.newInstanceWithJavaSafeHashCodeCalculator(key);
		}

		public DefaultZeroMQRPCFuture get(byte[] key) {
			return this.registry.get(wrap(key));
		}

		public boolean containsKey(byte[] key) {
			return this.registry.containsKey(wrap(key));
		}

		public DefaultZeroMQRPCFuture remove(byte[] key) {
			return this.registry.remove(wrap(key));
		}

		public void put(byte[] key, DefaultZeroMQRPCFuture value) {
			this.registry.put(wrap(key), value);
		}
	}

	@AllArgsConstructor
	private static class ResponseWorker implements WorkHandler<ZeroMQResponse> {
		private final FutureRegistry futureRegistry;

		@Override
		public void onEvent(ZeroMQResponse event) throws Exception {
			DefaultZeroMQRPCFuture future = futureRegistry.remove(event.getMessageId());
			if (future != null) {
				if (event.error) {
					future.setFailedCause(event.failedCause != null ? event.failedCause
							: new UnknownError("Error while processing response, unknown!!!"));
					future.setAndDone(null);
				} else {
					future.setAndDone(event.getData());
				}
			}
		}
	}

	private Disruptor<ZeroMQResponse> disruptor;
	private String responseEndpoint;
	private final FutureRegistry futureRegistry = new FutureRegistry();
	private ZMQSocket socketReceiver;
	private Thread pollingThread;

	@Override
	public void init(ZMQSocketRegistry socketRegistry, ZeroMQProducerConfig config) {
		super.init(socketRegistry, config);

		this.disruptor = new Disruptor<>(ZeroMQResponse.EVENT_FACTORY, config.getRingBufferSize(),
				this.getThreadFactory());

		Unmarshaller[] unmarshallers = new Unmarshaller[config.getUnmarshallerSize()];
		for (int i = 0; i < unmarshallers.length; i++) {
			unmarshallers[i] = new Unmarshaller(this);
		}

		ResponseWorker[] handlers = new ResponseWorker[config.getHandlerPoolSize()];
		for (int i = 0; i < handlers.length; i++) {
			handlers[i] = new ResponseWorker(this.futureRegistry);
		}

		this.disruptor.handleEventsWithWorkerPool(unmarshallers).thenHandleEventsWithWorkerPool(handlers);

		this.responseEndpoint = config.getResponseEndpoint();
	}

	@Override
	protected void _start() {
		this.socketReceiver = this.getSocketRegistry().openSocket(this.responseEndpoint, ZMQSocketType.PULL_BIND);
		this.responseEndpoint = this.socketReceiver.getAddress();
		this.disruptor.start();
		this.pollingThread = new Thread() {
			@Override
			public void run() {
				while (isRunning()) {
					byte[] data = socketReceiver.recv();
					disruptor.publishEvent(new EventTranslator<ZeroMQResponse>() {

						@Override
						public void translateTo(ZeroMQResponse event, long sequence) {
							event.clear();
							event.rawData = data;
						}
					});
				}
			}
		};
		this.pollingThread.start();
	}

	@Override
	protected void _close() throws IOException {
		this.pollingThread.interrupt();
		this.disruptor.halt();
		this.socketReceiver.close();
	}

	@Override
	public PuArray generateMetadata(PuElement data) {
		byte[] messageId = UUIDUtils.timebasedUUIDAsBytes();

		PuArray metadata = new PuArrayList();
		metadata.addFrom(messageId);
		metadata.addFrom(this.responseEndpoint);

		return metadata;
	}

	@Override
	public void extractMetadata(PuArray metadata, ZeroMQResponse response) {
		response.setMessageId(metadata.getRaw(0));
	}

	@Override
	protected void onSendingSuccess(ZeroMQRequest request) {
		this.futureRegistry.put(request.metadata.getRaw(0), request.future);
	}
}
