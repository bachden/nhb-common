package com.nhb.messaging.zmq;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.zeromq.ZMQ;

import com.nhb.common.Loggable;

import lombok.Getter;

public class ZMQSocketRegistry implements Loggable {
	private static final Set<String> TCP_UDP = new HashSet<>(Arrays.asList("tcp", "udp"));

	private final Map<String, Collection<ZMQ.Socket>> registry = new ConcurrentHashMap<>();
	private final Map<ZMQ.Socket, String> socketToAddress = new ConcurrentHashMap<>();

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
		System.out.println("Shutting down ZMQSocketRegistry instance...");
		synchronized (registry) {
			for (Entry<String, Collection<ZMQ.Socket>> entry : registry.entrySet()) {
				System.out.println(
						"There are " + entry.getValue().size() + " opened sockets with same addr: " + entry.getKey());
				int count = 0;
				for (ZMQ.Socket socket : entry.getValue()) {
					System.out.println("\t-> Closing socket " + (++count));
					socket.setLinger(0);
					socket.close();

					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			this.registry.clear();
			this.socketToAddress.clear();
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
		synchronized (registry) {
			ZMQ.Socket socket = this.getContext().socket(type.getFlag());

			Collection<ZMQ.Socket> openedSockets = this.registry.get(address);
			if (openedSockets == null) {
				openedSockets = new CopyOnWriteArrayList<>();
				this.registry.put(address, openedSockets);
			}

			openedSockets.add(socket);
			socketToAddress.put(socket, address);

			int port = extractPort(address);
			String protocol = extractProtocol(address);

			if (type == ZMQSocketType.SUB || type == ZMQSocketType.XSUB) {
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
				} else {
					socket.bind(address);
				}

				if (type == ZMQSocketType.PUB || type == ZMQSocketType.XPUB || type == ZMQSocketType.SUB
						|| type == ZMQSocketType.XSUB) {
					try {
						Thread.sleep(options == null ? 200 : options.getPubSubSleepingTime());
					} catch (InterruptedException e) {
						throw new RuntimeException("Thead interupted while sleeping because opening pub/xpub socket");
					}
				}
			}
			return new ZMQSocket(socket, port, address, () -> {
				this.closeSocket(socket);
			});
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

	public void closeSocket(ZMQ.Socket socket) {
		if (socket != null) {
			String addr = this.socketToAddress.get(socket);
			if (addr != null) {
				Collection<ZMQ.Socket> sockets = this.registry.get(addr);
				if (sockets != null) {
					synchronized (registry) {
						if (sockets.remove(socket)) {
							socket.setLinger(0);
							socket.close();
							socketToAddress.remove(socket);
						}
					}
				}
			}
		}
	}
}
