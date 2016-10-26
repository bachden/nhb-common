package nhb.common.messaging.test;

import com.nhb.common.BaseLoggable;
import com.nhb.common.utils.Initializer;

public class TestKafka extends BaseLoggable {

	public static void main(String[] args) {

		Initializer.bootstrap(TestKafka.class);

		final TestKafka test = new TestKafka();

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				test.stop();
			}
		});

		test.start();
	}

	private TestKafka() {
	}

	private void start() {
	}

	private void stop() {
	}
}
