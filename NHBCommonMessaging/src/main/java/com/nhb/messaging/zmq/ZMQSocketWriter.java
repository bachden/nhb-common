package com.nhb.messaging.zmq;

import java.nio.ByteBuffer;

import org.zeromq.ZMQ;

import com.nhb.common.data.PuElement;
import com.nhb.common.vo.ByteBufferOutputStream;

public interface ZMQSocketWriter {

	boolean write(PuElement payload, ZMQSocket socket);

	/**
	 * <p>
	 * Create new zero-copy socket writer, be careful because zero-copy doesn't let
	 * you know how long it hold data before send
	 * </p>
	 * 
	 * <p>
	 * If you send message too fast, it may occur error
	 * </p>
	 * 
	 * @param bufferCapacity
	 * @return
	 */
	static ZMQSocketWriter newZeroCopyWriter(int bufferCapacity) {

		final ByteBuffer buffer = ByteBuffer.allocateDirect(bufferCapacity);

		return (payload, socket) -> {
			if (payload != null && socket != null) {
				buffer.clear();
				payload.writeTo(new ByteBufferOutputStream(buffer));
				buffer.flip();
				return socket.sendZeroCopy(buffer, buffer.remaining(), 0);
			}
			throw new NullPointerException("Payload and socket cannot be null");
		};
	}

	/**
	 * return default sending blocking mode writer (using flags 0) with default
	 * puElement buffer size
	 * 
	 * @return
	 */
	static ZMQSocketWriter newDefaultWriter() {

		return newDefaultWriter(PuElement.DEFAULT_BUFFER_SIZE);
	}

	/**
	 * return default sending blocking mode writer (using flags 0) with custom
	 * puElement buffer size
	 * 
	 * @param puElementBufferSize
	 * @return
	 */
	static ZMQSocketWriter newDefaultWriter(final int puElementBufferSize) {

		return (payload, socket) -> {
			if (payload != null && socket != null) {
				return socket.send(payload.toBytes(puElementBufferSize), 0);
			}
			throw new NullPointerException("Payload and socket cannot be null");
		};
	}

	/**
	 * return default sending non-blocking mode writer (using flags 0) with custom
	 * puElement buffer size
	 * 
	 * @param puElementBufferSize
	 * @return
	 */
	static ZMQSocketWriter newNonBlockingWriter(final int puElementBufferSize) {

		return (payload, socket) -> {
			if (payload != null && socket != null) {
				return socket.send(payload.toBytes(puElementBufferSize), ZMQ.NOBLOCK);
			}
			throw new NullPointerException("Payload and socket cannot be null");
		};
	}

	/**
	 * return default sending blocking mode writer (using flags 0) with default
	 * puElement buffer size
	 * 
	 * @return
	 */
	static ZMQSocketWriter newNonBlockingWriter() {

		return newNonBlockingWriter(PuElement.DEFAULT_BUFFER_SIZE);
	}

}
