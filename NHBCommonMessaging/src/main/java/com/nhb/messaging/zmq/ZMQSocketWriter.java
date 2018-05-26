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

		return new ZMQSocketWriter() {

			private final ByteBuffer buffer = ByteBuffer.allocateDirect(bufferCapacity);

			@Override
			public boolean write(PuElement payload, ZMQSocket socket) {
				if (payload != null && socket != null) {
					this.buffer.clear();
					payload.writeTo(new ByteBufferOutputStream(buffer));
					this.buffer.flip();
					return socket.sendZeroCopy(buffer, buffer.remaining(), 0);
				}
				throw new NullPointerException("Payload and socket cannot be null");
			}
		};
	}

	static ZMQSocketWriter newDefaultWriter() {

		return new ZMQSocketWriter() {

			@Override
			public boolean write(PuElement payload, ZMQSocket socket) {
				if (payload != null && socket != null) {
					return socket.send(payload.toBytes(), ZMQ.NOBLOCK);
				}
				throw new NullPointerException("Payload and socket cannot be null");
			}
		};
	}
}
