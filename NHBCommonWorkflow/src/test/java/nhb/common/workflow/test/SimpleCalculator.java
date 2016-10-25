package nhb.common.workflow.test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import nhb.common.Loggable;
import nhb.common.data.MapTuple;
import nhb.common.data.PuObject;
import nhb.common.utils.Initializer;
import nhb.common.workflow.JobContext;
import nhb.common.workflow.async.TaskExecuteOptions;
import nhb.common.workflow.async.TaskFuture;
import nhb.common.workflow.async.TaskListener;
import nhb.common.workflow.concurrent.impl.DisruptorWorkProcessorGroup;
import nhb.common.workflow.impl.GenericJobContext;
import nhb.common.workflow.impl.job.BaseAsyncJob;
import nhb.common.workflow.statemachine.StateMachine;
import nhb.common.workflow.statemachine.impl.IgnoreTransitionStateMachine;
import nhb.common.workflow.test.task.CalculateSumTask;
import nhb.common.workflow.test.task.PrintResultTask;

public class SimpleCalculator implements Loggable {

	public static void main(String[] args) throws InterruptedException {
		Initializer.bootstrap(SimpleCalculator.class);
		new SimpleCalculator().run();
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
		DisruptorWorkProcessorGroup wpg = new DisruptorWorkProcessorGroup("abc", 2, 1024);
		wpg.start();

		BaseAsyncJob job = new BaseAsyncJob("SimpleCalculator");

		job.addEnvironmentVariable("debug", true);

		job.getWorkProcessorGroupHolder().addWorkProcessorGroup(wpg);
		job.addTask(new PrintResultTask(), new CalculateSumTask());
		job.setTaskExecuteOptions("printResult",
				TaskExecuteOptions.builder().async(true).workProcessorGroupName("abc").build());

		int count = 1;
		CountDownLatch doneSignal = new CountDownLatch(count);

		TaskListener jobDoneListener = new TaskListener() {

			@Override
			public void onSuccess(JobContext context) {
				System.out.println("Done successfully, job: " + context.getId() + " --> thread: ["
						+ Thread.currentThread().getName() + "]");
				doneSignal.countDown();
			}

			@Override
			public void onFailure(JobContext context, Throwable cause) {
				System.err.println("Done with failure, current task: " + context.getStateMachine().getCurrentState()
						+ " --> thread [" + Thread.currentThread().getName() + "]");
				cause.printStackTrace();
				doneSignal.countDown();
			}
		};

		CountDownLatch startSignal = new CountDownLatch(1);
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					startSignal.await();
				} catch (InterruptedException e) {
					getLogger().error("Error", e);
					System.exit(1);
					return;
				}
				for (int i = 0; i < count; i++) {
					TaskFuture future = job.execute(generateContext());
					future.cancel();
					future.addListener(jobDoneListener);
				}
			}
		}).start();

		startSignal.countDown();
		doneSignal.await();

		Thread.sleep(1000);

		job.shutdown();
		wpg.shutdown();
	}
}
