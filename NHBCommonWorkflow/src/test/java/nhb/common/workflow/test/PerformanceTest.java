package nhb.common.workflow.test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import nhb.common.Loggable;
import nhb.common.data.MapTuple;
import nhb.common.data.PuObject;
import nhb.common.utils.Initializer;
import nhb.common.workflow.JobContext;
import nhb.common.workflow.async.TaskListener;
import nhb.common.workflow.concurrent.route.RoutingBus;
import nhb.common.workflow.impl.GenericJobContext;
import nhb.common.workflow.impl.job.BaseAsyncJob;
import nhb.common.workflow.statemachine.StateMachine;
import nhb.common.workflow.statemachine.impl.IgnoreTransitionStateMachine;
import nhb.common.workflow.test.task.CalculateSumTask;
import nhb.common.workflow.test.task.PrintResultTask;

public class PerformanceTest implements Loggable {
	public static void main(String[] args) throws InterruptedException {
		Initializer.bootstrap(SimpleCalculator.class);
		new PerformanceTest().run();
	}

	private StateMachine generateStateMachine() {
		StateMachine stateMachine = new IgnoreTransitionStateMachine();
		stateMachine.addTransition(CalculatorState.ASTERISK, CalculatorState.END);
		stateMachine.addTransition(CalculatorState.CALCULATE_SUM, CalculatorState.PRINT_RESULT);
		stateMachine.setFinalState(CalculatorState.END);
		stateMachine.start(CalculatorState.CALCULATE_SUM);
		return stateMachine;
	}

	private AtomicInteger idSeed = new AtomicInteger(0);

	private JobContext generateContext() {
		GenericJobContext context = GenericJobContext.builder().id(idSeed.incrementAndGet())
				.stateMachine(generateStateMachine()).build();
		context.setInput(PuObject.fromObject(new MapTuple<>("data", new int[] { 1, 2, 3 })));
		return context;
	}

	private void run() throws InterruptedException {

		RoutingBus routingBus = new RoutingBus(3, 1024 * 1024, "PerformanceTest Routing #%d");
		BaseAsyncJob job = new BaseAsyncJob("PerformanceTest", routingBus);

		routingBus.setTaskHolder(job);
		routingBus.setWorkProcessorGroupHolder(job.getWorkProcessorGroupHolder());

		// job.addEnvironmentVariable("debug", false);
		job.addTask(new PrintResultTask(), new CalculateSumTask());
		// job.setTaskExecuteOptions("printResult",
		// TaskExecuteOptions.builder().async(true).build());

		routingBus.start();

		final int nThreads = 2;
		final int jobCount = (int) 1e5;
		final int jobPerThread = jobCount / nThreads;

		final CountDownLatch startSignal = new CountDownLatch(1);
		final CountDownLatch doneSignal = new CountDownLatch(jobCount);

		JobContext[] contexts = new JobContext[jobCount];
		for (int i = 0; i < contexts.length; i++) {
			contexts[i] = generateContext();
		}

		TaskListener jobDoneListener = new TaskListener() {

			@Override
			public void onSuccess(JobContext context) {
				doneSignal.countDown();
			}

			@Override
			public void onFailure(JobContext context, Throwable cause) {
				doneSignal.countDown();
			}
		};
		for (int i = 0; i < nThreads; i++) {
			final int threadId = i;
			new Thread(new Runnable() {
				private final int myId = threadId;

				@Override
				public void run() {
					try {
						startSignal.await();
					} catch (InterruptedException e) {
						getLogger().error("Await thread interupted", e);
						return;
					}
					int start = myId * jobPerThread;
					int end = start + jobPerThread;
					for (int j = start; j < end; j++) {
						job.execute(contexts[j]).addListener(jobDoneListener);
					}
				}
			}).start();
		}

		// DecimalFormat df = new DecimalFormat("0.##");
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					startSignal.await();
				} catch (InterruptedException e) {
					getLogger().error("Await thread interupted", e);
					return;
				}
				while (true) {
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						getLogger().error("Error while sleep monitor thread", e);
					}
					long doneCount = jobCount - doneSignal.getCount();
					if (doneCount >= jobCount) {
						getLogger().info("DONE");
						return;
					} else {
						// getLogger().info("Complete {}%",
						// df.format(Double.valueOf(doneCount) /
						// Double.valueOf(jobCount)));
					}
				}
			}
		}, "Monitor thread").start();

		long startTime = System.currentTimeMillis();
		startSignal.countDown();
		doneSignal.await();
		long latency = System.currentTimeMillis() - startTime;

		Thread.sleep(1000);
		System.out.println("Ops/sec == " + jobCount * 1e3 / latency);
		job.shutdown();
	}
}
