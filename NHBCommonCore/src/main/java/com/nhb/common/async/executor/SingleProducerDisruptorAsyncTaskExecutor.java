package com.nhb.common.async.executor;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.WorkHandler;
import com.lmax.disruptor.WorkerPool;
import com.nhb.common.Loggable;

import lombok.Getter;
import lombok.Setter;

public class SingleProducerDisruptorAsyncTaskExecutor implements AsyncTaskExecutor, Loggable {

	private RingBuffer<RunnableEvent> ringBuffer;
	private WorkerPool<RunnableEvent> workerPool;

	@Setter
	@Getter
	private static class RunnableEvent {
		private Runnable runnable;
	}

	private static class RunnableExecuteWorker implements WorkHandler<RunnableEvent> {

		@Override
		public void onEvent(RunnableEvent event) throws Exception {
			event.getRunnable().run();
		}
	}

	private EventFactory<RunnableEvent> createEventFactory() {
		return new EventFactory<SingleProducerDisruptorAsyncTaskExecutor.RunnableEvent>() {

			@Override
			public RunnableEvent newInstance() {
				return new RunnableEvent();
			}
		};
	}

	private RunnableExecuteWorker[] createWorkers(int size) {
		if (size > 0) {
			RunnableExecuteWorker[] result = new RunnableExecuteWorker[size];
			for (int i = 0; i < result.length; i++) {
				result[i] = new RunnableExecuteWorker();
			}
			return result;
		}
		return null;
	}

	private ExceptionHandler<RunnableEvent> exceptionHandler = new ExceptionHandler<RunnableEvent>() {

		@Override
		public void handleOnStartException(Throwable ex) {
			getLogger().error("Error while start worker pool", ex);
		}

		@Override
		public void handleOnShutdownException(Throwable ex) {
			getLogger().error("Error while shutdown worker pool", ex);
		}

		@Override
		public void handleEventException(Throwable ex, long sequence, RunnableEvent event) {
			getLogger().error("Error while handling runnable event", ex);
		}
	};
	private Executor executor;

	public SingleProducerDisruptorAsyncTaskExecutor(int ringBufferSize, int numWorkers, String threadNamePattern) {
		this(ringBufferSize, numWorkers, threadNamePattern, new BlockingWaitStrategy());
	}

	public SingleProducerDisruptorAsyncTaskExecutor(int ringBufferSize, int numWorkers, String threadNamePattern,
			WaitStrategy waitStrategy) {
		this.ringBuffer = RingBuffer.createSingleProducer(this.createEventFactory(), ringBufferSize, waitStrategy);
		this.workerPool = new WorkerPool<>(ringBuffer, ringBuffer.newBarrier(), exceptionHandler,
				this.createWorkers(numWorkers));
		this.executor = Executors.newCachedThreadPool(new ThreadFactory() {

			private AtomicInteger idSeed = new AtomicInteger(0);

			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, String.format(threadNamePattern, idSeed.incrementAndGet()));
			}
		});
	}

	@Override
	public void execute(Runnable runnable) {
		long sequence = this.ringBuffer.next();
		try {
			RunnableEvent event = this.ringBuffer.get(sequence);
			event.setRunnable(runnable);
		} finally {
			this.ringBuffer.publish(sequence);
		}
	}

	@Override
	public void start() {
		this.workerPool.start(this.executor);
	}

	@Override
	public void shutdown() throws Exception {
		if (this.workerPool.isRunning()) {
			this.workerPool.drainAndHalt();
		}
	}
}
