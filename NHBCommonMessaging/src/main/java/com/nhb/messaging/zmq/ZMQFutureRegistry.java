package com.nhb.messaging.zmq;

import java.util.Map;
import java.util.concurrent.CancellationException;

import org.cliffc.high_scale_lib.NonBlockingHashMap;

import com.nhb.common.vo.ByteArray;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ZMQFutureRegistry {

    private final Map<ByteArray, DefaultZMQFuture> registry = new NonBlockingHashMap<>();

    public ByteArray wrap(byte[] messageId) {
        return ByteArray.newInstanceWithJavaSafeHashCodeCalculator(messageId);
    }

    public void put(byte[] key, DefaultZMQFuture future) {
        this.put(wrap(key), future);
    }

    public void put(ByteArray key, DefaultZMQFuture future) {
        this.registry.put(key, future);
    }

    public DefaultZMQFuture remove(byte[] key) {
        return this.remove(wrap(key));
    }

    public DefaultZMQFuture remove(ByteArray key) {
        return this.registry.remove(key);
    }

    public boolean containsKey(byte[] key) {
        return this.containsKey(wrap(key));
    }

    public boolean containsKey(ByteArray key) {
        return this.registry.containsKey(key);
    }

    public DefaultZMQFuture get(byte[] key) {
        return this.get(wrap(key));
    }

    public DefaultZMQFuture get(ByteArray key) {
        return this.registry.get(key);
    }

    public int remaining() {
        return this.registry.size();
    }

    public void cancelAll() {
        Throwable cause = new CancellationException("Future cancelled");
        for (DefaultZMQFuture future : this.registry.values()) {
            try {
                future.setFailedAndDone(cause);
            } catch (Exception e) {
                log.warn("error while cancel future", e);
            }
        }
        this.registry.clear();
    }
}