package nhb.common.messaging.test;

import java.text.DecimalFormat;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.nhb.common.async.Callback;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.PuObject;
import com.nhb.common.data.PuValue;
import com.nhb.common.utils.TimeWatcher;
import com.nhb.messaging.zmq.ZMQSocketOptions;
import com.nhb.messaging.zmq.ZMQSocketRegistry;
import com.nhb.messaging.zmq.consumer.ZMQConsumerConfig;
import com.nhb.messaging.zmq.consumer.ZMQMessageProcessor;
import com.nhb.messaging.zmq.consumer.ZMQRPCConsumer;
import com.nhb.messaging.zmq.producer.ZMQProducerConfig;
import com.nhb.messaging.zmq.producer.ZMQRPCProducer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestZMQRPC {

	private static final String ENDPOINT = "tcp://127.0.0.1:6789";
	private static final String PRODUCER_RECEIVE_ENDPOINT = "tcp://127.0.0.1";

	public static void main(String[] args) throws InterruptedException, ExecutionException {
		ZMQSocketRegistry socketRegistry = new ZMQSocketRegistry();

		final int numSenders = 4;
		ZMQRPCProducer producer = initProducer(socketRegistry, numSenders);
		ZMQRPCConsumer consumer = initConsumer(socketRegistry, numSenders);

		consumer.start();
		producer.start();

		int count = 10;
		while (count-- > 0) {
			try {
				producer.publish(PuValue.fromObject("ping")).get(10, TimeUnit.MILLISECONDS);
				break;
			} catch (TimeoutException e) {
			}
		}

		int numMessages = (int) 1e6;
		PuObject[] messages = new PuObject[numMessages];
		for (int i = 0; i < messages.length; i++) {
			PuObject data = new PuObject();
			data.set("name", "Nguyễn Hoàng Bách");
			data.set("age", "unknown");
			data.set("id", i);
			messages[i] = data;
		}

		log.debug("Start sending....");
		CountDownLatch doneSignal = new CountDownLatch(numMessages);
		Thread monitor = new Thread(() -> {
			while (!Thread.currentThread().isInterrupted()) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					return;
				}
				log.debug("Remaining: {}", doneSignal.getCount());
			}
		}, "monitor");
		monitor.start();

		TimeWatcher timeWatcher = new TimeWatcher();
		timeWatcher.reset();
		for (PuObject message : messages) {
			producer.publish(message).setCallback(new Callback<PuElement>() {

				@Override
				public void apply(PuElement result) {
					doneSignal.countDown();
					// log.debug("Got response: " + result + " --> remaining: " +
					// doneSignal.getCount());
				}
			});
		}
		doneSignal.await();
		double totalTimeSeconds = timeWatcher.endLapSeconds();

		monitor.interrupt();

		double avgMessageSize = Double
				.valueOf(messages[0].toBytes().length + messages[numMessages - 1].toBytes().length) / 2;
		double totalMessageBytes = avgMessageSize * numMessages;

		DecimalFormat df = new DecimalFormat("###,###.##");

		log.info("************** STATISIC **************");
		log.info("Num senders: {}", numSenders);
		log.info("Num msgs: {}", df.format(numMessages));
		log.info("Elapsed: {} seconds", df.format(totalTimeSeconds));
		log.info("Avg msg size: {} bytes", df.format(avgMessageSize));
		log.info("Msg Rate: {} msg/s", df.format(Double.valueOf(numMessages) / totalTimeSeconds));
		log.info("Total sent bytes: {} bytes == {} KB == {} MB", df.format(totalMessageBytes),
				df.format(totalMessageBytes / 1024), df.format(totalMessageBytes / 1024 / 1024));
		log.info("Throughput: {} bytes/s == {} KB/s == {} MB/s", df.format(totalMessageBytes / totalTimeSeconds),
				df.format(totalMessageBytes / 1024 / totalTimeSeconds),
				df.format(totalMessageBytes / 1024 / 1024 / totalTimeSeconds));

		log.info("*************** DONE ***************");
		System.exit(0);
	}

	private static ZMQRPCConsumer initConsumer(ZMQSocketRegistry socketRegistry, int numSenders) {
		ZMQRPCConsumer consumer = new ZMQRPCConsumer();
		ZMQConsumerConfig config = new ZMQConsumerConfig();
		config.setSendSocketOptions(
				ZMQSocketOptions.builder().hwm((long) 1e5).sndHWM((long) 1e5).rcvHWM((long) 1e5).build());
		config.setSendWorkerSize(numSenders);
		config.setReceiveEndpoint(ENDPOINT);
		config.setSocketRegistry(socketRegistry);
		config.setMessageProcessor(ZMQMessageProcessor.SIMPLE_RESPONSE_MESSAGE_PROCESSOR);

		consumer.init(config);
		return consumer;
	}

	private static ZMQRPCProducer initProducer(ZMQSocketRegistry socketRegistry, int numSenders) {
		ZMQRPCProducer producer = new ZMQRPCProducer();
		ZMQProducerConfig config = new ZMQProducerConfig();
		config.setSendSocketOptions(
				ZMQSocketOptions.builder().hwm((long) 1e5).sndHWM((long) 1e5).rcvHWM((long) 1e5).build());
		config.setSocketRegistry(socketRegistry);
		config.setSendEndpoint(ENDPOINT);
		config.setReceiveEndpoint(PRODUCER_RECEIVE_ENDPOINT);
		config.setSendWorkerSize(numSenders);

		producer.init(config);
		return producer;
	}

}
