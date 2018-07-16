package nhb.common.test;

import java.util.concurrent.atomic.AtomicLong;

import com.nhb.common.cache.AutoLoadingCache;

public class TestAutoLoadingCache {

	public static void main(String[] args) {

		final AtomicLong seed = new AtomicLong();
		final AutoLoadingCache<Integer, Long> cache = AutoLoadingCache.newDefault((key) -> {
			int latency = 200;
			try {
				System.out.println("Simulating loading latency by " + latency + "ms");
				Thread.sleep(latency);
			} catch (InterruptedException e) {
				return -1l;
			}
			return seed.incrementAndGet();
		});

		cache.setInterval(100);
		cache.setTimeToLive(300);

		final int nThreads = 10;
		final Thread[] threads = new Thread[nThreads];
		for (int i = 0; i < threads.length; i++) {
			final int threadId = i;
			threads[i] = new Thread(() -> {
				while (!Thread.currentThread().isInterrupted()) {
					try {
						Thread.sleep((threadId + 1) * 10);
					} catch (InterruptedException e) {
						return;
					}
					Long value = cache.get(0);
					if (value == null) {
						new NullPointerException("Got null value").printStackTrace();
						System.exit(1);
						return;
					}
					System.out.println("thread " + threadId + ": " + value);
				}
			}, "Thread #" + threadId);
		}

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			for (Thread thread : threads) {
				thread.interrupt();
			}
			cache.stop();
		}));

		cache.start();
		for (Thread thread : threads) {
			thread.start();
		}
	}
}
