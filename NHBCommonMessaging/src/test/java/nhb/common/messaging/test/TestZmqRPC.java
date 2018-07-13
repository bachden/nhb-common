package nhb.common.messaging.test;

import java.text.DecimalFormat;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import com.nhb.common.async.Callback;
import com.nhb.common.data.PuDataType;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuValue;
import com.nhb.common.utils.TimeWatcher;
import com.nhb.messaging.zmq.ZMQSocketOptions;
import com.nhb.messaging.zmq.ZMQSocketRegistry;
import com.nhb.messaging.zmq.ZMQSocketWriter;
import com.nhb.messaging.zmq.consumer.ZMQConsumerConfig;
import com.nhb.messaging.zmq.consumer.ZMQMessageProcessor;
import com.nhb.messaging.zmq.consumer.ZMQRPCConsumer;
import com.nhb.messaging.zmq.producer.ZMQProducerConfig;
import com.nhb.messaging.zmq.producer.ZMQRPCProducer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestZmqRPC {

	private static final String PRODUCER_RECEIVE_ENDPOINT = "tcp://127.0.0.1";
	private static final String ENDPOINT = PRODUCER_RECEIVE_ENDPOINT + ":6789";

	public static void main(String[] args) throws InterruptedException, ExecutionException {
		ZMQSocketRegistry socketRegistry = new ZMQSocketRegistry();

		final int messageSize = 1024;
		final int numSenders = 3;
		ZMQRPCProducer producer = initProducer(socketRegistry, numSenders, messageSize);
		ZMQRPCConsumer consumer = initConsumer(socketRegistry, numSenders, messageSize);

		consumer.start();
		producer.start();

		int numMessages = (int) 1024 * 1024;
		PuValue data = new PuValue(new byte[messageSize - 3 /* for msgpack meta */], PuDataType.RAW);

		log.debug("Start sending....");
		// reset receiveCouter
		Thread monitor = new Thread(() -> {
			DecimalFormat dfP = new DecimalFormat("0.##%");
			DecimalFormat df = new DecimalFormat("###,###.##");
			while (!Thread.currentThread().isInterrupted()) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					return;
				}
				log.debug(
						"[DONE: {}, remaining={}] => [PRODUCER: sent={}, responsed={}] => [CONSUMER: received={}, responsed={}]" //
				, dfP.format(Double.valueOf(producer.getReceivedCount()) / numMessages) // done percentage
				, df.format(numMessages - producer.getReceivedCount()) // remaining
				, df.format(producer.getSentCount()) // sent count
				, df.format(producer.getReceivedCount()) // received count
				, df.format(consumer.getReceivedCount()) // consumer received count
				, df.format(consumer.getRespondedCount()) // consumer responsed count
				);
			}
		}, "monitor");

		TimeWatcher timeWatcher = new TimeWatcher();
		CountDownLatch doneSignal = new CountDownLatch(1);

		Callback<PuElement> callback = new Callback<PuElement>() {
			private int doneCounter = 0;

			/**
			 * Single thread only
			 */
			@Override
			public void apply(PuElement result) {
				if (result == null) {
					log.error("ERROR...");
				}
				if (++doneCounter == numMessages) {
					doneSignal.countDown();
				}
			}
		};

		monitor.start();
		timeWatcher.reset();
		for (int i = 0; i < numMessages; i++) {
			producer.publish(data).setCallback(callback);
		}
		doneSignal.await();
		double totalTimeSeconds = timeWatcher.endLapSeconds();

		Thread.sleep(500);
		monitor.interrupt();

		double avgMessageSize = data.toBytes().length;
		double totalSentBytes = avgMessageSize * numMessages;
		double totalIOThroughput = totalSentBytes * 2;

		DecimalFormat df = new DecimalFormat("###,###.##");

		log.info("************** STATISTIC **************");
		log.info("Num senders: {}", numSenders);
		log.info("Num msgs: {}", df.format(numMessages));
		log.info("Elapsed: {} seconds", df.format(totalTimeSeconds));
		log.info("Avg msg size: {} bytes", df.format(avgMessageSize));
		log.info("Msg rate: {} msg/s", df.format(Double.valueOf(numMessages) / totalTimeSeconds));
		log.info("Total sent bytes: {} bytes == {} KB == {} MB", df.format(totalSentBytes),
				df.format(totalSentBytes / 1024), df.format(totalSentBytes / 1024 / 1024));

		log.info("Sending throughput: {} bytes/s == {} KB/s == {} MB/s", df.format(totalSentBytes / totalTimeSeconds),
				df.format(totalSentBytes / 1024 / totalTimeSeconds),
				df.format(totalSentBytes / 1024 / 1024 / totalTimeSeconds));

		log.info("Total I/O throughput: {} bytes/s == {} KB/s == {} MB/s",
				df.format(totalIOThroughput / totalTimeSeconds), df.format(totalIOThroughput / 1024 / totalTimeSeconds),
				df.format(totalIOThroughput / 1024 / 1024 / totalTimeSeconds));

		log.info("**************** DONE ****************");
		System.exit(0);
	}

	private static ZMQRPCConsumer initConsumer(ZMQSocketRegistry socketRegistry, int numSenders, int messageSize) {

		ZMQConsumerConfig config = new ZMQConsumerConfig();
		config.setSendSocketOptions(
				ZMQSocketOptions.builder().hwm((long) 1e6).sndHWM((long) 1e6).rcvHWM((long) 1e6).build());
		config.setSendWorkerSize(numSenders);
		config.setReceiveEndpoint(ENDPOINT);
		config.setBufferCapacity(messageSize * 2);
		config.setSocketWriter(ZMQSocketWriter.newNonBlockingWriter(messageSize + 32));
		config.setSocketRegistry(socketRegistry);
		config.setMessageProcessor(ZMQMessageProcessor.SIMPLE_RESPONSE_MESSAGE_PROCESSOR);
		config.setRespondedCountEnabled(true);
		config.setReceivedCountEnabled(true);

		ZMQRPCConsumer consumer = new ZMQRPCConsumer();
		consumer.init(config);

		return consumer;
	}

	private static ZMQRPCProducer initProducer(ZMQSocketRegistry socketRegistry, int numSenders, int messageSize) {
		ZMQProducerConfig config = new ZMQProducerConfig();
		config.setSendSocketOptions(
				ZMQSocketOptions.builder().hwm((long) 1e6).sndHWM((long) 1e6).rcvHWM((long) 1e6).build());
		config.setSocketRegistry(socketRegistry);
		config.setSendEndpoint(ENDPOINT);
		config.setBufferCapacity(messageSize * 2);
		config.setSocketWriter(ZMQSocketWriter.newNonBlockingWriter(messageSize + 32));
		config.setReceiveEndpoint(PRODUCER_RECEIVE_ENDPOINT);
		config.setSendWorkerSize(numSenders);
		config.setSentCountEnabled(true);
		config.setReceivedCountEnable(true);

		ZMQRPCProducer producer = new ZMQRPCProducer();
		producer.init(config);

		return producer;
	}

}
