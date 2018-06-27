package nhb.common.zookeeper.leaderelection.test;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.nhb.common.data.PuValue;

public class TestLeaderElectionMember extends TestLeaderElection {

	public static void main(String[] args) throws IOException, InterruptedException, TimeoutException {
		TestLeaderElectionMember app = new TestLeaderElectionMember();
		app.init(true);
		app.start();

		Thread.sleep(5000);
		app.getAgent().updateData(PuValue.fromObject("updated data").toBytes());
	}
}
