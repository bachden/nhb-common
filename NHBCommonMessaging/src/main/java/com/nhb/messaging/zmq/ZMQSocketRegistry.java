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
import com.nhb.common.utils.InetAddressUtils;

import lombok.Getter;

public class ZMQSocketRegistry implements Loggable {

	private static final Pattern PORT_PATTERN = Pattern.compile("\\d{1,5}");
	private static final Pattern ENDPOINT_PATTERN = Pattern.compile("(?i)^(.+):\\/\\/(.+)$");

	private static String[] extractEndpointSegments(String input) {
		if (input != null) {
			Matcher matcher = ENDPOINT_PATTERN.matcher(input.trim());
			if (matcher.find()) {
				String protocol = matcher.group(1);
				String hostAndPort = matcher.group(2);
				String[] arr = hostAndPort.split(":");
				if (arr.length > 1) {
					String maybePort = arr[arr.length - 1];
					if (PORT_PATTERN.matcher(maybePort).find()) {
						StringBuilder host = new StringBuilder();
						for (int i = 0; i < arr.length - 1; i++) {
							if (host.length() > 0) {
								host.append(":");
							}
							host.append(arr[i]);
						}
						return new String[] { protocol, host.toString(), maybePort };
					}
				}
				return new String[] { protocol, hostAndPort, null };
			}
		}
		return null;
	}

	private static Object[] inspectAddress(String address) {
		if (address != null) {
			String[] endpointSegments = extractEndpointSegments(address.trim());
			if (endpointSegments == null) {
				throw new IllegalArgumentException("Invalid address: " + address);
			}

			String protocol = endpointSegments[0].toLowerCase();
			String host = endpointSegments[1];
			if (!host.equals("*")) {
				String resolvedHost = InetAddressUtils.resolve(host);
				host = resolvedHost == null ? host : resolvedHost;
			}
			int port = endpointSegments[2] == null ? -1 : Integer.valueOf(endpointSegments[2]);

			return new Object[] { protocol + "://" + host + (port != -1 ? (":" + port) : ""), protocol, host, port };
		}
		throw new IllegalArgumentException("Invalid address: " + address);
	}

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
		System.err.println("[" + Thread.currentThread().getName() + "] --> Terminate context...");
		context.term();
	}

	private ZMQSocket createSocket(String address, ZMQSocketType type, ZMQSocketOptions options) {
		ZMQ.Socket socket = this.getContext().socket(type.getFlag());

		Object[] addressSegments = inspectAddress(address);

		address = addressSegments[0].toString();
		String protocol = addressSegments[1].toString();
		String host = InetAddressUtils.resolve(addressSegments[2].toString());
		int port = (int) addressSegments[3];

		address = protocol + "://" + host + (port != -1 ? (":" + port) : "");

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
