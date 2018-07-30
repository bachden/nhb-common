package com.nhb.messaging.zmq;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.zeromq.ZMQ;

import com.nhb.common.Loggable;

import lombok.Getter;

public class ZMQSocketRegistry implements Loggable {
	private static final Set<String> TCP_UDP = new HashSet<>(Arrays.asList("tcp", "udp"));

	private final List<ZMQSocket> openedSockets = new LinkedList<>();

	@Getter
	private final int ioThreads;

	@Getter
	private final ZMQ.Context context;

	public ZMQSocketRegistry() {
		this(1);
	}

	public ZMQSocketRegistry(int ioThreads) {
		if (ioThreads <= 0) {
			throw new IllegalArgumentException("ioThreads cannot be zero or negative");
		}
		this.ioThreads = ioThreads;
		this.context = ZMQ.context(ioThreads);
		getLogger().debug("ZMQ version: {}", ZMQ.getVersionString());
	}

	public void destroy() {
		List<ZMQSocket> sockets = this.openedSockets.stream().filter(socket -> socket.getSocketType().isClient())
				.collect(Collectors.toList());
		sockets.forEach(socket -> socket.close());

		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.err.println("Terminate context...");
		context.term();
	}

	private ZMQSocket createSocket(String address, ZMQSocketType type, ZMQSocketOptions options) {
		ZMQ.Socket socket = this.getContext().socket(type.getFlag());

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

		if (type.isClient()) {
			socket.connect(address);
			return new ZMQSocket(socket, -1, address, type, this::closeSocket);
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

			return new ZMQSocket(socket, port, address, type, this::closeSocket);
		}
	}

	public ZMQSocket openSocket(String address, ZMQSocketType type, ZMQSocketOptions options) {
		ZMQSocket socket = this.createSocket(address, type, options);
		if (socket != null) {
			synchronized (this.openedSockets) {
				this.openedSockets.add(socket);
				return socket;
			}
		}
		return null;
	}

	public ZMQSocket openSocket(String addr, ZMQSocketType type) {
		return this.openSocket(addr, type, null);
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

	private void closeSocket(ZMQSocket socket) {
		if (this.openedSockets.contains(socket)) {
			synchronized (this.openedSockets) {
				if (this.openedSockets.contains(socket)) {
					System.err.println("[" + Thread.currentThread().getName() + "] -> Closing socket at: "
							+ socket.getAddress() + " " + socket.getSocketType());
					socket.setLinger(0);
					socket.getSocket().close();
					this.openedSockets.remove(socket);
				}
			}
		}
	}
}
