package nhb.common.test.geo.gvm;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Scanner;

import nhb.common.BaseLoggable;
import nhb.common.utils.Initializer;

public class ClusterPoints extends BaseLoggable {

	static {
		Initializer.bootstrap(ClusterPoints.class);
	}

	public static void main(String[] args) throws IOException {
		new ClusterPoints().cluster("cross", 2);
		new ClusterPoints().cluster("gmouse", 3);
		new ClusterPoints().cluster("umouse", 3);
		new ClusterPoints().cluster("faithful", 2);
	}

	private void cluster(String name, int capacity) throws IOException {
		Scanner scanner = new Scanner(new File("../cluster-common/R/" + name + ".txt"));
		ArrayList<double[]> pts = new ArrayList<double[]>();
		while (scanner.hasNext()) {
			double[] pt = new double[2];
			pt[0] = scanner.nextDouble();
			pt[1] = scanner.nextDouble();
			pts.add(pt);
		}
		scanner.close();

		Collections.shuffle(pts, new Random(0L));

		// DblClusters<List<double[]>> clusters = new
		// DblClusters<List<double[]>>(2, capacity);
		// clusters.setKeyer(new DblListKeyer<double[]>());
		// for (double[] pt : pts) {
		// ArrayList<double[]> key = new ArrayList<double[]>();
		// key.add(pt);
		// clusters.add(1.0, pt, key);
		// }
		//
		// final List<DblResult<List<double[]>>> results = clusters.results();
		// for (int i = 0; i < results.size(); i++) {
		// for (double[] pt : results.get(i).getKey()) {
		// getLogger().debug(String.format("%3.3f %3.3f %d%n", pt[0], pt[1], i +
		// 1));
		// }
		// }
	}

}
