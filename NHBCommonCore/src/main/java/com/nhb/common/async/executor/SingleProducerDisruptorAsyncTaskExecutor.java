package com.nhb.common.async.executor;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.WaitStrategy;

class SingleProducerDisruptorAsyncTaskExecutor extends DisruptorAsyncTaskExecutor {

	public SingleProducerDisruptorAsyncTaskExecutor(int ringBufferSize, int numWorkers, String threadNamePattern) {
		super(ringBufferSize, numWorkers, threadNamePattern);
	}

	public SingleProducerDisruptorAsyncTaskExecutor(int ringBufferSize, int numWorkers, String threadNamePattern,
			WaitStrategy waitStrategy) {
		super(ringBufferSize, numWorkers, threadNamePattern, waitStrategy);
	}

	@Override
	protected RingBuffer<RunnableEvent> createRingBuffer(EventFactory<RunnableEvent> factory, int bufferSize,
			WaitStrategy waitStrategy) {
		return RingBuffer.createSingleProducer(factory, bufferSize, waitStrategy);
	}

}
