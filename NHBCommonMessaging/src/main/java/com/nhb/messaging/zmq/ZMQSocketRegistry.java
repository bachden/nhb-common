package com.nhb.messaging.zmq;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.zeromq.ZMQ;

import com.nhb.common.Loggable;

import lombok.Getter;

public class ZMQSocketRegistry implements Loggable {
	private static final Set<String> TCP_UDP = new HashSet<>(Arrays.asList("tcp", "udp"));

	private final List<ZMQ.Socket> openedSockets = new LinkedList<>();

	@Getter
	private final int ioThreads;

	@Getter
	private final ZMQ.Context context;

	public ZMQSocketRegistry() {
		this(1, true);
	}

	public ZMQSocketRegistry(int ioThreads, boolean autoDestroy) {
		if (ioThreads <= 0) {
			throw new IllegalArgumentException("ioThreads cannot be zero or negative");
		}
		this.ioThreads = ioThreads;
		this.context = ZMQ.context(ioThreads);
		if (autoDestroy) {
			Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

				@Override
				public void run() {
					ZMQSocketRegistry.this.destroy();
				}
			}, "ZeroMQ socket registry shutdown hook"));
		}
	}

	public void destroy() {
		for (ZMQ.Socket socket : this.openedSockets) {
			socket.setLinger(0);
			try {
				Thread.sleep(30);
			} catch (InterruptedException e1) {
				getLogger().error("Thread interupted while sleeping aftter socket.setLinger(0)", e1);
				e1.printStackTrace();
			}
			socket.close();

			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		context.close();
	}

	public ZMQSocket openSocket(String addr, ZMQSocketType type) {
		return this.openSocket(addr, type, null);
	}

	public ZMQSocket openSocket(String address, ZMQSocketType type, ZMQSocketOptions options) {
		ZMQ.Socket socket = this.getContext().socket(type.getFlag());
		openedSockets.add(socket);

		String protocol = extractProtocol(address);

		if (type.getFlag() == ZMQ.SUB || type.getFlag() == ZMQ.XSUB) {
			if (options == null || options.getTopics() == null || options.getTopics().size() == 0) {
				socket.subscribe(new byte[0]);
			} else {
				for (byte[] topic : options.getTopics()) {
					socket.subscribe(topic);
				}
			}
		}

		Function<Integer, Void> onCloseCallback = new Function<Integer, Void>() {

			@Override
			public Void apply(Integer linger) {
				ZMQSocketRegistry.this.closeSocket(socket, linger);
				return null;
			}
		};

		if (type.isClient()) {
			socket.connect(address);
			return new ZMQSocket(socket, -1, address, onCloseCallback);
		} else {
			int port = extractPort(address);
			if (port == -1 && TCP_UDP.contains(protocol.toLowerCase())) {
				int minPort = options == null ? -1 : options.getMinPort();
				int maxPort = options == null ? -1 : options.getMaxPort();
				if (minPort > 0) {
					if (maxPort > minPort) {
						port = socket.bindToRandomPort(address, minPort, maxPort);
					} else {
						port = socket.bindToRandomPort(address, minPort);
					}
				} else {
					port = socket.bindToRandomPort(address);
				}
				address += ":" + port;
			} else {
				port = -1;
				socket.bind(address);
			}

			if (type == ZMQSocketType.PUB_BIND || type == ZMQSocketType.PUB_CONNECT || type == ZMQSocketType.SUB_BIND
					|| type == ZMQSocketType.SUB_CONNECT) {
				try {
					Thread.sleep(options == null ? 200 : options.getPubSubSleepingTime());
				} catch (InterruptedException e) {
					throw new RuntimeException("Thead interupted while sleeping because opening pub/xpub socket");
				}
			}

			return new ZMQSocket(socket, port, address, onCloseCallback);
		}
	}

	private static final int extractPort(String address) {
		Matcher m = Pattern.compile(":(\\d+)").matcher(address);
		if (m.find()) {
			return Integer.valueOf(m.group(1));
		}
		return -1;
	}

	private static final String extractProtocol(String address) {
		Matcher m = Pattern.compile("^([a-zA-Z]+)://").matcher(address);
		if (m.find()) {
			return m.group(1);
		}
		return null;
	}

	public void closeSocket(ZMQ.Socket socket, int linger) {
		if (this.openedSockets.contains(socket)) {
			synchronized (this.openedSockets) {
				if (this.openedSockets.contains(socket)) {
					openedSockets.remove(socket);
					socket.setLinger(linger);
					socket.close();
				}
			}
		}
	}

	public void closeSocket(ZMQ.Socket socket) {
		this.closeSocket(socket, 0);
	}
}
