package nhb.common.messaging.test;

import java.text.DecimalFormat;
import java.util.concurrent.ExecutionException;

import com.nhb.common.data.PuObject;
import com.nhb.common.utils.TimeWatcher;
import com.nhb.messaging.zmq.DisruptorZMQReceiver;
import com.nhb.messaging.zmq.DisruptorZMQSender;
import com.nhb.messaging.zmq.ZMQPayloadBuilder;
import com.nhb.messaging.zmq.ZMQPayloadExtractor;
import com.nhb.messaging.zmq.ZMQReceivedMessageHandler;
import com.nhb.messaging.zmq.ZMQReceiver;
import com.nhb.messaging.zmq.ZMQReceiverConfig;
import com.nhb.messaging.zmq.ZMQSender;
import com.nhb.messaging.zmq.ZMQSenderConfig;
import com.nhb.messaging.zmq.ZMQSendingDoneHandler;
import com.nhb.messaging.zmq.ZMQSocketRegistry;
import com.nhb.messaging.zmq.ZMQSocketType;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestZMQSenderAndReceiver {

	private static final String ENDPOINT = "tcp://127.0.0.1:5678";

	public static void main(String[] args) throws InterruptedException, ExecutionException {

		ZMQSocketRegistry socketRegistry = new ZMQSocketRegistry();

		final int numSenders = 4;

		final ZMQSender sender = initSender(socketRegistry, numSenders);
		final ZMQReceiver receiver = initReceiver(socketRegistry);

		receiver.start();
		sender.start();

		int numMessages = (int) 1e6;
		PuObject[] messages = new PuObject[numMessages];
		for (int i = 0; i < messages.length; i++) {
			PuObject data = new PuObject();
			data.set("name", "Nguyễn Hoàng Bách");
			data.set("age", "unknown");
			data.set("id", i);
			messages[i] = data;
		}

		TimeWatcher timeWatcher = new TimeWatcher();
		timeWatcher.reset();
		for (PuObject message : messages) {
			sender.send(message);
		}
		double totalTimeSeconds = timeWatcher.endLapSeconds();

		double avgMessageSize = Double
				.valueOf(messages[0].toBytes().length + messages[numMessages - 1].toBytes().length) / 2;
		double totalMessageBytes = avgMessageSize * numMessages;

		DecimalFormat df = new DecimalFormat("###,###.##");

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
	}

	private static ZMQReceiver initReceiver(ZMQSocketRegistry socketRegistry) {
		ZMQReceiver receiver = new DisruptorZMQReceiver();
		ZMQReceiverConfig config = ZMQReceiverConfig.builder() //
				.endpoint(ENDPOINT) //
				.socketType(ZMQSocketType.PULL_BIND) //
				.payloadExtractor(ZMQPayloadExtractor.DEFAULT_PUARRAY_PAYLOAD_EXTRACTOR) //
				.receivedMessageHandler(ZMQReceivedMessageHandler.IGNORE_RECEIVED_DATA_HANDLER) //
				.build();

		receiver.init(socketRegistry, config);
		return receiver;
	}

	private static ZMQSender initSender(ZMQSocketRegistry socketRegistry, int numSenders) {
		ZMQSender sender = new DisruptorZMQSender();
		ZMQSenderConfig config = ZMQSenderConfig.builder()//
				.endpoint(ENDPOINT) //
				.sendWorkerSize(numSenders) //
				.socketType(ZMQSocketType.PUSH_CONNECT) //
				.sendingDoneHandler(ZMQSendingDoneHandler.DEFAULT) //
				.payloadBuilder(ZMQPayloadBuilder.DEFAULT_PUARRAY_PAYLOAD_BUILDER) //
				.build();

		sender.init(socketRegistry, config);
		return sender;
	}

}
