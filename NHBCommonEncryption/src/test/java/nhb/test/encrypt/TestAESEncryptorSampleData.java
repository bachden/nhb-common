package nhb.test.encrypt;

import java.text.DecimalFormat;
import java.util.concurrent.CountDownLatch;

import nhb.common.encrypt.aes.AESEncryptor;

public class TestAESEncryptorSampleData {

	public static void main(String[] args) throws InterruptedException {

		final String[] samples = ("jM7Xwv0Vw9zqU/EqyUv3NaeGncViwbUb6e2ofoJujMuRb6e7VtXCuuSrP9ig1qt3/YyT3ZyKfh7yV6F9Ood7cTCS8yKrW3dVXzUckf7wVk9HE25pXx5nDQxrD1icpX26"
				+ "\n"
				+ "VzHiiQ5q4tqZFRBymG9SQkbXT1YA3lqS9X0oumorQtrRbbt3zdfxOiOqoPavpX0yyeaep/O39Nge8NKghG7TPwwMmxZ0uod8Izx8s2DPshC8K42yO5ewIpJBk2tqsX1/"
				+ "\n"
				+ "w9GpzV6aLutB2veb1nGIzf+mbEXJSsQDIxizrhBFEzetEyvCHXKv1S0hH932emzRWBUvrtbe9jrsOd9o+eUs/cHAPAjNjHhW/ZzDYYMGrw67A0adZLDbPeaPc5cBy49f"
				+ "\n"
				+ "UNNuzR/0qqBG0ND2i4L5ElgayIKTNHkB6GOzdDfVnkituG5dsIq4bm+BEmUSFERt5/nI9aoCGTofmkpU4y7veI0z+TO6Qem3CGmqY0DgG1DG0JSsNJQLEYdn6p3YNoYy"
				+ "\n"
				+ "upcKZzRzgOer7aGuIc+Av12XnBlJYnhp8yJ3r1aeRyBFUMCfNFPSEs0/A1G2/Tb3VHj1bqPbIMi4W528s3KzWPvkw3FFA8E39MxS3WC44m4="
				+ "\n"
				+ "tRbreOFdxvKvzJhAOYhlR0Pi/PZzjYDhsDlD5OYa/irQ39kzCDTbfY/Ng8t3BCb/lRiUR/FkEyMWKOLycH35l8s9xksU+1Bjx+2NZ7R4mc8="
				+ "\n"
				+ "CFbfsK4B4wHlZZEFVimeURwKEUY63kDkUVKx2CrzCpDcN4q712Ew6pZ6gUR9ctUQ09PhQCZCyVIXxU0goY8mxhc91O8+gk/KLJv9fBqxUKQ=")
						.split("\n");

		final String passwordBase64 = "DMQQMMmNQVLZDRlF9my7PA==";

		final int numThreads = args.length > 0 ? Integer.valueOf(args[0]) : 100;
		final int numOptPerThread = args.length > 1 ? Integer.valueOf(args[1]) : 100;

		final int numOperators = numOptPerThread * numThreads;

		final long[] latencies = new long[numOperators];

		final CountDownLatch startSignal = new CountDownLatch(1);
		final CountDownLatch doneSignal1 = new CountDownLatch(numThreads);
		final CountDownLatch doneSignal2 = new CountDownLatch(numThreads);

		for (int i = 0; i < numThreads; i++) {
			final int _threadId = i;
			(new Thread() {

				private final int threadId = _threadId;

				{
					this.setName("Encryption thread #" + this.threadId);
				}

				@Override
				public void run() {

					AESEncryptor aesEncryptor = AESEncryptor.newInstance();
					aesEncryptor.setPasswordBase64(passwordBase64);

					String arr[] = new String[numOptPerThread];
					for (int i = 0; i < arr.length; i++) {
						arr[i] = samples[i % samples.length];
					}
					doneSignal1.countDown();

					try {
						startSignal.await();
					} catch (InterruptedException e) {
						throw new RuntimeException("Interupted while waiting", e);
					}

					long start = System.currentTimeMillis();
					for (int i = 0; i < arr.length; i++) {
						start = System.nanoTime();
						aesEncryptor.decryptFromBase64(arr[i]);
						latencies[numOptPerThread * threadId + i] = System.nanoTime() - start;
					}
					doneSignal2.countDown();
				}
			}).start();
		}

		final DecimalFormat df = new DecimalFormat("###,###.##");
		Thread monitorThread = new Thread() {

			@Override
			public void run() {
				while (true) {
					int count = 0;
					for (int i = 0; i < latencies.length; i++) {
						if (latencies[i] > 0) {
							count++;
						}
					}
					float percent = Float.valueOf(count * 100f) / latencies.length;
					System.out.println("Completed " + df.format(count) + "/" + df.format(latencies.length) + " -> "
							+ df.format(percent) + "%");
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
					}

					if (count == latencies.length) {
						break;
					}
				}
			};
		};

		System.out.println("Initializing data...");
		doneSignal1.await();

		System.out.println("Start decryption test...");
		monitorThread.start();
		long startTimeNano = System.nanoTime();
		startSignal.countDown();
		doneSignal2.await();

		long totalTimeNano = System.nanoTime() - startTimeNano;

		monitorThread.interrupt();
		Thread.sleep(200);

		long maxLatency = Long.MIN_VALUE;
		long minLatency = Long.MAX_VALUE;
		long sumLatency = 0;

		for (long lat : latencies) {
			if (lat > maxLatency) {
				maxLatency = lat;
			}
			if (lat < minLatency) {
				minLatency = lat;
			}
			sumLatency += lat;
		}
		double avgLatency = Double.valueOf(sumLatency) / latencies.length;

		System.out.println("--------------- REPORT ---------------");
		System.out.println("Num threads: " + df.format(numThreads));
		System.out.println("Num operators: " + df.format(numThreads * numOptPerThread));
		System.out.println("Num operators per thread: " + df.format(numOptPerThread));
		System.out
				.println("Total execution time: " + df.format(totalTimeNano) + "ns = " + df.format(totalTimeNano / 1e3)
						+ "µs = " + df.format(totalTimeNano / 1e6) + "ms = " + df.format(totalTimeNano / 1e9) + "s");
		System.out.println("Max latency: " + df.format(maxLatency) + "ns = " + df.format(maxLatency / 1e3) + "µs = "
				+ df.format(maxLatency / 1e6) + "ms = " + df.format(maxLatency / 1e9) + "s");
		System.out.println("Min latency: " + df.format(minLatency) + "ns = " + df.format(minLatency / 1e3) + "µs = "
				+ df.format(minLatency / 1e6) + "ms = " + df.format(minLatency / 1e9) + "s");
		System.out.println("Avg latency: " + df.format(avgLatency) + "ns = " + df.format(avgLatency / 1e3) + "µs = "
				+ df.format(avgLatency / 1e6) + "ms = " + df.format(avgLatency / 1e9) + "s");
		System.out.println("--------------------------------------");

		System.exit(0);
	}
}
