package nhb.common.zookeeper.leaderelection.test;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class TestLeaderElectionClient extends TestLeaderElection {

	public static void main(String[] args) throws IOException, InterruptedException, TimeoutException {
		TestLeaderElectionClient app = new TestLeaderElectionClient();
		app.init(false);
		app.start();
	}
}
 