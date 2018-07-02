package com.nhb.messaging.zmq;

import java.util.function.Supplier;

import com.nhb.common.data.PuElement;

public interface ZMQSender {

	boolean isRunning();

	boolean isInitialized();

	void init(ZMQSocketRegistry registry, ZMQSenderConfig config);

	void setSentCountEnabled(boolean enabled);

	long getSentCount();

	void start();

	void stop();

	void setFutureSupplier(Supplier<ZMQFuture> futureSupplier);

	ZMQFuture send(PuElement data);
}
