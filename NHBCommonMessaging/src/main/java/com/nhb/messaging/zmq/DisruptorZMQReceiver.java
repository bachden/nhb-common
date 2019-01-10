package com.nhb.messaging.zmq;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;

import org.zeromq.ZMQException;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.WorkHandler;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.nhb.common.Loggable;
import com.nhb.common.data.PuElement;
import com.nhb.common.data.msgpkg.PuElementTemplate;
import com.nhb.common.vo.ByteBufferInputStream;

import lombok.Getter;
import lombok.Setter;

public class DisruptorZMQReceiver implements ZMQReceiver, Loggable {

    @Getter
    private volatile boolean initialized = false;
    private final AtomicBoolean initializedCheckpoint = new AtomicBoolean(false);

    private volatile boolean running = false;
    private final AtomicBoolean runningCheckpoint = new AtomicBoolean(false);

    @Setter
    private volatile boolean receivedCountEnabled = false;
    private volatile long receivedCounter = 0;

    private ZMQSocket socket;
    private Thread pollingThread;
    private ZMQSocketRegistry socketRegistry;
    private ZMQReceiverConfig config;
    private Disruptor<ZMQEvent> messageHandler;

    private Exception startupException = null;
    private CountDownLatch startupDoneSignal = null;

    private ExceptionHandler<ZMQEvent> exceptionHandler = new ExceptionHandler<ZMQEvent>() {

        @Override
        public void handleEventException(Throwable ex, long sequence, ZMQEvent event) {
            getLogger().error("Error while handling ZMQEvent: {}", event.getPayload(), ex);
            if (event.getFuture() != null && !event.getFuture().isDone()) {
                event.getFuture().setFailedAndDone(ex);
            }
        }

        @Override
        public void handleOnStartException(Throwable ex) {
            getLogger().error("Error while starting sender disruptor", ex);
        }

        @Override
        public void handleOnShutdownException(Throwable ex) {
            getLogger().error("Error while shutting down sender disruptor", ex);
        }
    };;

    @Override
    public String getEndpoint() {
        if (this.socket != null) {
            return this.socket.getAddress();
        }
        return null;
    }

    @Override
    public long getReceivedCount() {
        return this.receivedCounter;
    }

    @Override
    public void init(ZMQSocketRegistry registry, ZMQReceiverConfig config) {
        if (this.initializedCheckpoint.compareAndSet(false, true)) {
            if (registry == null) {
                throw new NullPointerException("Socket registry cannot be null");
            } else if (config == null) {
                throw new NullPointerException("Config cannot be null");
            }

            config.validate();

            this.config = config;
            this.socketRegistry = registry;

            doInit();

            this.initialized = true;
        }
    }

    private void doInit() {
        this.setReceivedCountEnabled(config.isReceivedCountEnabled());

        ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat(config.getThreadNamePattern()).build();
        messageHandler = new Disruptor<>(ZMQEvent::new, config.getQueueSize(), threadFactory, ProducerType.SINGLE, config.getWaitStrategy());

        @SuppressWarnings("unchecked")
        WorkHandler<ZMQEvent>[] workers = new WorkHandler[config.getPoolSize()];
        for (int i = 0; i < workers.length; i++) {
            workers[i] = this::onReceive;
        }

        this.messageHandler.handleEventsWithWorkerPool(workers);
        this.messageHandler.setDefaultExceptionHandler(this.exceptionHandler);

        this.pollingThread = new Thread(this::pollData);
    }

    private void onReceive(ZMQEvent event) {
        this.config.getPayloadExtractor().extractPayload(event);
        this.config.getReceivedMessageHandler().onReceive(event);
    }

    private void pollData() {
        try {
            if (this.socket == null) {
                try {
                    this.socket = this.socketRegistry.openSocket(config.getEndpoint(), config.getSocketType());
                    this.socket.setReceiveTimeOut(100);
                } catch (Exception e) {
                    startupException = e;
                    if (this.socket != null) {
                        this.socket.close();
                        this.socket = null;
                    }
                }
            }
        } finally {
            if (this.startupDoneSignal != null) {
                this.startupDoneSignal.countDown();
            }
        }

        if (this.socket == null) {
            return;
        }

        this.pollingThread.setName("ZMQ Poller " + this.getEndpoint());

        final ByteBuffer buffer = ByteBuffer.allocateDirect(config.getBufferCapacity());
        while (this.runningCheckpoint.get() && !Thread.currentThread().isInterrupted()) {
            buffer.clear();
            int recv = 0;

            try {
                recv = this.socket.recvZeroCopy(buffer, buffer.capacity(), 0);
            } catch (ZMQException e) {
                e.printStackTrace();
                break;
            }

            if (recv == -1 && (Thread.currentThread().isInterrupted() || !this.runningCheckpoint.get())) {
                // context may be terminated or socket were closed
                break;
            } else if (buffer.position() > 0) {
                buffer.flip();
                try {
                    final PuElement payload = PuElementTemplate.getInstance().read(new ByteBufferInputStream(buffer));

                    if (this.receivedCountEnabled) {
                        this.receivedCounter++;
                    }

                    this.messageHandler.publishEvent((event, sequence) -> {
                        event.clear();
                        event.setPayload(payload);
                    });

                } catch (IOException e) {
                    getLogger().error("Cannot parse as puElement", e);
                }
            }
        }

        if (this.socket != null) {
            try {
                this.socket.unbind();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                this.socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.socket = null;
        }
    }

    @Override
    public boolean isRunning() {
        while (this.runningCheckpoint.get() && !this.running) {
            LockSupport.parkNanos(10);
        }
        return this.running;
    }

    @Override
    public void start() {
        if (this.runningCheckpoint.compareAndSet(false, true)) {
            this.running = false;

            startupDoneSignal = new CountDownLatch(1);
            startupException = null;

            try {
                this.messageHandler.start();
                this.pollingThread.start();
                startupDoneSignal.await();
            } catch (InterruptedException e) {
                startupException = e;
            }

            if (startupException != null) {
                this.messageHandler.shutdown();
                this.pollingThread.interrupt();
                this.runningCheckpoint.set(false);
                throw new RuntimeException("Cannot start receiver", startupException);
            } else {
                this.running = true;
            }
        }
    }

    @Override
    public void stop() {
        if (this.runningCheckpoint.compareAndSet(true, false)) {
            this.pollingThread.interrupt();
            this.messageHandler.shutdown();
            this.running = false;
        }
    }
}
