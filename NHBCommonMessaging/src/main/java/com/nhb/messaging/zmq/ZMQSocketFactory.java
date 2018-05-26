package com.nhb.messaging.zmq;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.nhb.common.Loggable;

/**
 * Use to create new socket to connect to predefine address
 * 
 * @author bachden
 *
 */
public class ZMQSocketFactory implements Loggable {

	private final ZMQSocketRegistry registry;
	private final String address;
	private final ZMQSocketType type;
	private final ZMQSocketOptions options;
	private final List<ZMQSocket> openedSockets = new CopyOnWriteArrayList<>();

	public ZMQSocketFactory(ZMQSocketRegistry registry, String address, ZMQSocketType type) {
		this(registry, address, type, null);
	}

	public ZMQSocketFactory(ZMQSocketRegistry registry, String address, ZMQSocketType type, ZMQSocketOptions options) {
		if (!type.isClient()) {
			throw new IllegalArgumentException("Factory cannot be used with server socket type (using bind)");
		}

		this.registry = registry;
		this.address = address;
		this.type = type;
		this.options = options;
	}

	private void applyOptions(ZMQSocket socket) {
		if (socket != null && this.options != null) {
			if (this.options.getHwm() >= 0) {
				socket.setHWM(this.options.getHwm());
			}
			if (this.options.getSndHWM() >= 0) {
				socket.setSndHWM(this.options.getSndHWM());
			}
			if (this.options.getRcvHWM() >= 0) {
				socket.setRcvHWM(this.options.getRcvHWM());
			}
		}
	}

	public ZMQSocket newSocket() {
		ZMQSocket socket = this.registry.openSocket(this.address, this.type, this.options);
		this.applyOptions(socket);
		this.openedSockets.add(socket);
		return socket;
	}

	public void destroy() {
		while (this.openedSockets.size() > 0) {
			ZMQSocket socket = this.openedSockets.remove(0);
			if (socket != null) {
				try {
					socket.close();
				} catch (Exception ex) {
					getLogger().error("Cannot close socket", ex);
				}
			}
		}
	}
}
