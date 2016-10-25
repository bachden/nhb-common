package net.sf.jsi.examples;

import com.infomatiq.jsi.Point;
import com.infomatiq.jsi.Rectangle;
import com.infomatiq.jsi.SpatialIndex;
import com.infomatiq.jsi.rtree.RTree;

import gnu.trove.TIntProcedure;
import nhb.common.BaseLoggable;
import nhb.common.utils.Initializer;

public class NearestN extends BaseLoggable {

	static {
		Initializer.bootstrap(NearestN.class);
	}

	public static void main(String[] args) {
		new NearestN().run();
	}

	private class NullProc implements TIntProcedure {
		public boolean execute(int i) {
			return true;
		}
	}

	private void run() {
		int rowCount = 1000;
		int columnCount = 1000;
		int count = rowCount * columnCount;
		long start, end;

		getLogger().info("Creating " + count + " rectangles");
		final Rectangle[] rects = new Rectangle[count];
		int id = 0;
		for (int row = 0; row < rowCount; row++)
			for (int column = 0; column < rowCount; column++) {
				rects[id++] = new Rectangle(row, column, row + 0.5f, column + 0.5f); //
			}

		getLogger().info("Indexing " + count + " rectangles");
		start = System.currentTimeMillis();
		SpatialIndex si = new RTree();
		si.init(null);
		for (id = 0; id < count; id++) {
			si.add(rects[id], id);
		}
		end = System.currentTimeMillis();
		getLogger().info("Average time to index rectangle = " + ((end - start) / (count / 1000.0)) + " us");

		final Point p = new Point(36.3f, 84.3f);
		getLogger().info("Querying for the nearest 3 rectangles to " + p);
		si.nearestN(p, new TIntProcedure() {
			public boolean execute(int i) {
				getLogger().info("Rectangle " + i + " " + rects[i] + ", distance=" + rects[i].distance(p));
				return true;
			}
		}, 3, Float.MAX_VALUE);

		// Run a performance test, find the 3 nearest rectangles
		final int[] ret = new int[1];
		getLogger().info("Running 10000 queries for the nearest 3 rectangles");
		start = System.currentTimeMillis();
		for (int row = 0; row < 100; row++) {
			for (int column = 0; column < 100; column++) {
				p.x = row + 0.6f;
				p.y = column + 0.7f;
				si.nearestN(p, new TIntProcedure() {
					public boolean execute(int i) {
						ret[0]++;
						return true; // don't do anything with the results, for
										// a performance test.
					}
				}, 3, Float.MAX_VALUE);
			}
		}
		end = System.currentTimeMillis();
		getLogger().info("Average time to find nearest 3 rectangles = " + ((end - start) / (10000 / 1000.0)) + " us");
		getLogger().info("total time = " + (end - start) + "ms");
		getLogger().info("total returned = " + ret[0]);

		// Run a performance test, find the 3 nearest rectangles
		getLogger().info("Running 30000 queries for the nearest 3 rectangles");

		TIntProcedure proc = new NullProc();
		start = System.currentTimeMillis();
		for (int row = 0; row < 300; row++) {
			for (int column = 0; column < 100; column++) {
				p.x = row + 0.6f;
				p.y = column + 0.7f;
				si.nearestN(p, proc, 3, Float.MAX_VALUE);
			}
		}
		end = System.currentTimeMillis();
		getLogger().info("Average time to find nearest 3 rectangles = " + ((end - start) / (30000 / 1000.0)) + " us");
		getLogger().info("total time = " + (end - start) + "ms");
		getLogger().info("total returned = " + ret[0]);
	}
}
