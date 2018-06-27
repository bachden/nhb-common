package nhb.common.zookeeper.leaderelection.test;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import com.nhb.common.data.PuValue;
import com.nhb.common.data.msgpkg.PuElementTemplate;
import com.nhb.messaging.zookeeper.LeaderElectionAgent;

import lombok.AccessLevel;
import lombok.Getter;

public class TestLeaderElection {

	protected static final String DEFAULT_ROOT_PATH = "/test_leader_election";

	@Getter(AccessLevel.PROTECTED)
	private LeaderElectionAgent agent;

	public void init(boolean isCandidate) throws IOException, InterruptedException, TimeoutException {
		final AtomicBoolean shutdownFlag = new AtomicBoolean(false);
		Thread keepAlive = new Thread(() -> {
			while (!shutdownFlag.get()) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			shutdownFlag.set(true);
		}));

		keepAlive.start();

		if (isCandidate) {
			agent = LeaderElectionAgent.newCandidate("localhost:2181", 10, 15000l, DEFAULT_ROOT_PATH,
					PuValue.fromObject("OK").toBytes());
		} else {
			agent = LeaderElectionAgent.newClient("localhost:2181", 10, 15000l, DEFAULT_ROOT_PATH);
		}
	}

	public void start() {
		agent.setLeaderChangeCallback((newLeaderPath, imLeader) -> { // imLeader should always false when memeber ==
																		// false
			if (newLeaderPath == null) {
				System.out.println("No leader found!!!");
			} else {
				try {
					System.out.println((imLeader ? "I'm the leader: " : "Leader: ") + newLeaderPath + (imLeader ? ""
							: (" --> data: " + PuElementTemplate.getInstance().read(agent.getLeaderData()))));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});

		agent.setLeaderDataChangeCallback((leaderPath, leaderData) -> {
			if (leaderData != null) {
				try {
					System.out.println("Leader data changed: " + PuElementTemplate.getInstance().read(leaderData));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});

		agent.start();
	}
}
