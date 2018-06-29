package nhb.common.test;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import com.nhb.common.async.BaseRPCFuture;
import com.nhb.common.async.Callback;

public class TestRPCFuture {

	public static void main(String[] args) throws InterruptedException {

		for (int i = 1; i <= 30; i++) {
			System.out.println("------ Test " + i + " ------");

			final long time = ThreadLocalRandom.current().nextLong(1800l) + 100;
			final BaseRPCFuture<String> future = new BaseRPCFuture<>();

			Thread setResultThread = new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						Thread.sleep(time);
						if (!future.isDone()) {
							future.setAndDone("Successful 1");
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}, "Set result thread 1");

			Thread setResultThread2 = new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						Thread.sleep(time);
						if (!future.isDone()) {
							future.setAndDone("Successful 2");
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}, "Set result thread 2");

			Thread waitingForDataThread1 = new Thread(new Runnable() {

				@Override
				public void run() {
					String data;
					try {
						data = future.get();
						if (data != null) {
							System.out.println(Thread.currentThread().getName() + " --> Data: " + data);
						} else {
							future.getFailedCause().printStackTrace();
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

			}, "Waiting for data thread 1");

			Thread waitingForDataThread2 = new Thread(new Runnable() {

				@Override
				public void run() {
					String data;
					try {
						data = future.get();
						if (data != null) {
							System.out.println(Thread.currentThread().getName() + " --> Data: " + data);
						} else {
							future.getFailedCause().printStackTrace();
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

			}, "Waiting for data thread 2");

			future.setCallback(new Callback<String>() {

				@Override
				public void apply(String result) {
					if (result != null) {
						System.out.println(Thread.currentThread().getName() + " --> Callback data: " + result);
					} else {
						future.getFailedCause().printStackTrace();
					}
				}
			});

			long timeout = time + (ThreadLocalRandom.current().nextInt(10) - 5);
			Thread autoCancelThread = new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						Thread.sleep(timeout);
						if (future.cancel(true)) {
							System.out.println("---> Because of auto cancel...");
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}, "Automatic cancel future");

			System.out.println("Data will be appeared after " + time + "ms, timeout after " + timeout + "ms");

			future.setTimeout(timeout + (ThreadLocalRandom.current().nextInt(6) - 4), TimeUnit.MILLISECONDS);
			autoCancelThread.start();

			setResultThread.start();
			setResultThread2.start();

			waitingForDataThread1.start();
			waitingForDataThread2.start();

			Thread.sleep(time + 100);
			System.out.println("Future is done: " + future.isDone() + ", is cancelled: " + future.isCancelled());
		}

		System.out.println("------ DONE ------");
		System.exit(0);
	}

}
