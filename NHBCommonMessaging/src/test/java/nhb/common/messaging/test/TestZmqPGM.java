package nhb.common.messaging.test;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.nhb.messaging.zmq.ZMQSocketOptions;
import com.nhb.messaging.zmq.ZMQSocketRegistry;
import com.nhb.messaging.zmq.ZMQSocketType;
import com.nhb.messaging.zmq.ZMQSocketWriter;
import com.nhb.messaging.zmq.consumer.ZMQConsumer;
import com.nhb.messaging.zmq.consumer.ZMQConsumerConfig;
import com.nhb.messaging.zmq.consumer.ZMQMessageProcessor;
import com.nhb.messaging.zmq.consumer.ZMQRPCConsumer;

public class TestZmqPGM {

	private static final String ENDPOINT = "epgm://239.192.1.1:5555";

	public static void main(String[] args) throws InterruptedException {
		ZMQSocketRegistry socketRegistry = new ZMQSocketRegistry();
		ZMQConsumer zmqConsumer = initConsumer(socketRegistry, 1, 1024);
		try {
			zmqConsumer.start();
			Thread.sleep(1000);
		} finally {
			zmqConsumer.stop();
		}
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
		config.setMessageProcessor(ZMQMessageProcessor.ECHO_MESSAGE_PROCESSOR);
		config.setRespondedCountEnabled(true);
		config.setReceivedCountEnabled(true);
		config.setReceiveWaitStrategy(new BlockingWaitStrategy());
		config.setResponderMaxIdleMinutes(1);
		config.setReceiveSocketType(ZMQSocketType.SUB_CONNECT);

		ZMQRPCConsumer consumer = new ZMQRPCConsumer();
		consumer.init(config);

		return consumer;
	}
}
