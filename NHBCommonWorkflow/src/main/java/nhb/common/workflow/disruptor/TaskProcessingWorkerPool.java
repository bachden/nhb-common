package nhb.common.workflow.disruptor;

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.Sequence;
import com.lmax.disruptor.SequenceBarrier;
import com.lmax.disruptor.WorkProcessor;

import nhb.common.Loggable;
import nhb.common.workflow.JobContext;
import nhb.common.workflow.Task;
import nhb.common.workflow.async.TaskListener;

public class TaskProcessingWorkerPool implements Loggable {

	public static String DEFAULT_TASK_PROCESSING_WORKER_NAME = "Task Processor #%d";

	private final AtomicBoolean running = new AtomicBoolean(false);

	private ExecutorService executor;
	private int numProcessors = 1;
	private int ringBufferSize;
	private ExceptionHandler<TaskProcessingEvent> exceptionHandler;
	private Collection<WorkProcessor<TaskProcessingEvent>> processors;

	private RingBuffer<TaskProcessingEvent> ringBuffer;

	private Sequence sequence;

	private String threadNamePattern = "Task Event";

	public TaskProcessingWorkerPool(int workerPoolSize, int ringBufferSize, String threadNamePattern,
			ExceptionHandler<TaskProcessingEvent> exceptionHandler) {

		this.ringBufferSize = ringBufferSize;
		this.numProcessors = workerPoolSize;
		this.exceptionHandler = exceptionHandler;
		this.threadNamePattern = threadNamePattern == null ? DEFAULT_TASK_PROCESSING_WORKER_NAME : threadNamePattern;
	}

	public boolean isRunning() {
		return this.running.get();
	}

	public void start() {
		if (!this.isRunning()) {
			synchronized (this) {
				if (!this.isRunning()) {

					ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat(threadNamePattern).build();
					this.executor = Executors.newCachedThreadPool(threadFactory);

					this.sequence = new Sequence(-1);
					this.ringBuffer = RingBuffer.createMultiProducer(TaskProcessingEvent.factory(), ringBufferSize);
					this.processors = new HashSet<>();

					SequenceBarrier barrier = this.ringBuffer.newBarrier();
					for (int i = 0; i < this.numProcessors; i++) {
						WorkProcessor<TaskProcessingEvent> processor = new WorkProcessor<>(ringBuffer, barrier,
								TaskHandler.newInstance(), this.exceptionHandler, sequence);
						this.processors.add(processor);
						this.ringBuffer.addGatingSequences(processor.getSequence());
					}
					for (WorkProcessor<TaskProcessingEvent> processor : processors) {
						this.executor.submit(processor);
					}

					this.running.set(true);
				}
			}
		}
	}

	public void shutdown() {
		if (this.isRunning()) {
			synchronized (this) {
				if (this.isRunning()) {
					for (WorkProcessor<TaskProcessingEvent> workProcessor : processors) {
						workProcessor.halt();
					}
					this.executor.shutdown();
					try {
						if (!this.executor.awaitTermination(3, TimeUnit.SECONDS)) {
							this.executor.shutdownNow();
						}
					} catch (InterruptedException e) {
						getLogger().error("Cannot shutdown executor", e);
					}
					this.ringBuffer = null;
					this.sequence = null;
					this.executor = null;
					this.processors = null;

					this.running.set(false);
				}
			}
		}
	}

	public TaskProcessingWorkerPool(int workerPoolSize, int ringBufferSize, String threadNamePattern) {
		this(workerPoolSize, ringBufferSize, threadNamePattern, null);
	}

	public void execute(Task task, JobContext jobContext, TaskListener listener) {
		long sequence = ringBuffer.next();
		try {
			TaskProcessingEvent event = ringBuffer.get(sequence);
			event.fill(task, jobContext, listener);
		} finally {
			ringBuffer.publish(sequence);
		}
	}
}
