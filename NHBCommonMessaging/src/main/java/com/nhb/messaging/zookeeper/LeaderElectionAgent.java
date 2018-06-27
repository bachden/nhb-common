package com.nhb.messaging.zookeeper;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LeaderElectionAgent {

	private static final String DEFAULT_NODE_PREFIX = "candidate_";

	private static final ZooKeeper initZooKeeper(String connnection, int sessionTimeoutMillis,
			long connectTimeoutMillis) throws IOException, InterruptedException, TimeoutException {
		final CountDownLatch connectedSignal = new CountDownLatch(1);
		ZooKeeper zooKeeper = new ZooKeeper(connnection, sessionTimeoutMillis, event -> {
			if (KeeperState.SyncConnected.equals(event.getState())) {
				connectedSignal.countDown();
			}
		});
		if (!connectedSignal.await(connectTimeoutMillis, TimeUnit.MILLISECONDS)) {
			zooKeeper.close();
			throw new TimeoutException("Connection timeout after " + connectTimeoutMillis + " milliseconds");
		}
		return zooKeeper;
	}

	public static final LeaderElectionAgent newCandidate(ZooKeeper zooKeeper, String rootPath, byte[] data) {
		return new LeaderElectionAgent(zooKeeper, rootPath, DEFAULT_NODE_PREFIX, true, data);
	}

	public static final LeaderElectionAgent newCandidate(String connnection, int sessionTimeoutMillis,
			long connectTimeoutMillis, String rootPath, byte[] data)
			throws IOException, InterruptedException, TimeoutException {
		return newCandidate(initZooKeeper(connnection, sessionTimeoutMillis, connectTimeoutMillis), rootPath, data);
	}

	public static final LeaderElectionAgent newClient(ZooKeeper zooKeeper, String rootPath) {
		return new LeaderElectionAgent(zooKeeper, rootPath, DEFAULT_NODE_PREFIX, false, null);
	}

	public static final LeaderElectionAgent newClient(String connnection, int sessionTimeoutMillis,
			long connectTimeoutMillis, String rootPath) throws IOException, InterruptedException, TimeoutException {
		return newClient(initZooKeeper(connnection, sessionTimeoutMillis, connectTimeoutMillis), rootPath);
	}

	private final ZooKeeper zooKeeper;

	private final String prefix;
	private final String rootNodePath;
	private String watchedNodePath;

	@Getter
	private String path;

	@Getter
	private final boolean isCandidate;

	private final AtomicBoolean isLeader = new AtomicBoolean(false);

	@Getter
	private String leaderPath = null;

	private final AtomicBoolean started = new AtomicBoolean(false);

	@Setter
	private BiConsumer<String, Boolean> leaderChangeCallback;

	@Setter
	private BiConsumer<String, byte[]> leaderDataChangeCallback;

	private final AtomicInteger version = new AtomicInteger(0);

	private byte[] myData;

	@Getter
	private byte[] leaderData;

	private LeaderElectionAgent(ZooKeeper zooKeeper, String rootPath, String prefix, boolean isCandidate, byte[] data) {
		this.zooKeeper = zooKeeper;
		this.isCandidate = isCandidate;
		this.prefix = prefix;
		this.rootNodePath = rootPath;
		this.myData = data;
	}

	public boolean isLeader() {
		return this.isLeader.get();
	}

	private String createNode(final String node, final boolean watch, final boolean ephemeral, byte[] data) {
		String createdNodePath = null;
		int tryCountdown = 10;
		while (tryCountdown-- > 0) {
			try {
				final Stat nodeStat = zooKeeper.exists(node, watch);
				if (nodeStat == null) {
					createdNodePath = zooKeeper.create(node, data, Ids.OPEN_ACL_UNSAFE,
							(ephemeral ? CreateMode.EPHEMERAL_SEQUENTIAL : CreateMode.PERSISTENT));
				} else {
					createdNodePath = node;
				}
				break;
			} catch (InterruptedException e) {
				throw new RuntimeException("Interupted", e);
			} catch (KeeperException e) {
				if (tryCountdown == 0) {
					throw new RuntimeException("Cannot create node: " + node + ", ephemeral: " + ephemeral, e);
				}
				try {
					Thread.sleep(200);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}

		return createdNodePath;
	}

	private void findLeader() {
		try {
			List<String> childNodePaths = zooKeeper.getChildren(this.rootNodePath, event -> {
				findLeader();
			});

			Collections.sort(childNodePaths);

			if (isCandidate) {
				String nodeName = path.substring(path.lastIndexOf('/') + 1);
				int index = childNodePaths.indexOf(nodeName);
				if (index == 0) {
					this.isLeader.set(true);
				} else {
					final String tobeWatchedNodePath = this.rootNodePath + "/" + childNodePaths.get(index - 1);
					if (this.watchedNodePath == null || !tobeWatchedNodePath.equals(this.watchedNodePath)) {
						watchedNodePath = tobeWatchedNodePath;
						zooKeeper.exists(watchedNodePath, true);
					}
				}
			}

			String newLeaderPath = childNodePaths.size() == 0 ? null : this.rootNodePath + "/" + childNodePaths.get(0);
			if (this.leaderPath == null || !this.leaderPath.equals(newLeaderPath)) {
				this.leaderPath = newLeaderPath;

				Runnable dataChangeTrigger = null;
				if (!this.isLeader() && leaderPath != null) {
					// if I'm not the leader, watch him
					zooKeeper.exists(leaderPath, new Watcher() {

						@Override
						public void process(WatchedEvent event) {
							if (EventType.NodeDataChanged.equals(event.getType())) {
								Runnable dataChangeTrigger = updateLeaderData();
								if (dataChangeTrigger != null) {
									dataChangeTrigger.run();
								}
							}
						}
					});
					dataChangeTrigger = updateLeaderData();
				} else if (leaderPath == null) {
					dataChangeTrigger = updateLeaderData();
				}

				triggerLeaderChange();
				if (dataChangeTrigger != null) {
					dataChangeTrigger.run();
				}
			}
		} catch (KeeperException | InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	private void triggerLeaderChange() {
		if (this.leaderChangeCallback != null) {
			this.leaderChangeCallback.accept(leaderPath, this.isLeader());
		}
	}

	private void ensureRootPath() {
		String rootPath = this.createNode(this.rootNodePath, false, false, null); // create root node
		if (rootPath == null) {
			throw new RuntimeException("Cannot make sure root node exists");
		}
	}

	public void start() {
		if (this.started.compareAndSet(false, true)) {
			ensureRootPath();
			if (this.isCandidate) {
				this.path = createNode(rootNodePath + "/" + prefix, false, true, myData);
			}
			this.findLeader();
		}

	}

	public void updateData(byte[] data) {
		if (this.started.get()) {
			int tryCountdown = 10;
			while (tryCountdown-- > 0) {
				try {
					this.zooKeeper.setData(this.getPath(), data, version.getAndIncrement());
					this.myData = data;
					break;
				} catch (InterruptedException e) {
					throw new RuntimeException("Interrupted while updating data");
				} catch (KeeperException e) {
					log.warn("Writing data error, retrying");
					if (tryCountdown == 0) {
						throw new RuntimeException("Writing data error, reach max try", e);
					}
				}
			}
		} else {
			throw new IllegalStateException("LeaderElectionAgent must be started before write data");
		}
	}

	private Runnable updateLeaderData() {
		if (this.started.get()) {
			if (leaderPath != null) {
				try {
					byte[] data = this.zooKeeper.getData(leaderPath, true, null);
					if (this.leaderData == null || !Arrays.equals(leaderData, data)) {
						this.leaderData = data;
						return () -> {
							triggerLeaderDataChange();
						};
					}
				} catch (InterruptedException | KeeperException e) {
					throw new RuntimeException("Interrupted while updating data");
				}
			} else {
				if (this.leaderData != null) {
					this.leaderData = null;
					return () -> {
						triggerLeaderDataChange();
					};
				}
			}
		} else {
			throw new IllegalStateException("LeaderElectionAgent must be started before read leader data");
		}
		return null;
	}

	private void triggerLeaderDataChange() {
		if (leaderDataChangeCallback != null) {
			leaderDataChangeCallback.accept(leaderPath, leaderData);
		}
	}

	public void stop() {
		if (this.started.compareAndSet(true, false)) {
			try {
				this.zooKeeper.close();
			} catch (InterruptedException e) {
				e.printStackTrace();
				log.error("Error while closing zooKeeper", e);
			}
		}
	}
}
