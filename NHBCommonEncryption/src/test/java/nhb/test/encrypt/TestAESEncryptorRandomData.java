package nhb.test.encrypt;

import java.text.DecimalFormat;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import com.nhb.common.encrypt.aes.AESEncryptor;

public class TestAESEncryptorRandomData {

	public static void main(String[] args) throws InterruptedException {

		String passwordBase64 = "DMQQMMmNQVLZDRlF9my7PA==";

		final AESEncryptor aesEncryptor = AESEncryptor.newInstance();
		aesEncryptor.setPasswordBase64(passwordBase64);

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

					String arr[] = new String[numOptPerThread];
					for (int i = 0; i < arr.length; i++) {
						arr[i] = aesEncryptor.encryptToBase64(
								(UUID.randomUUID().toString() + "|bachden|quybu|" + System.currentTimeMillis())
										.getBytes());
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
