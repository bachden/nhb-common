package nhb.test.common.cassandra;

import java.util.List;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Cluster.Builder;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

public class TestCassandra {

	public static void main(String[] args) {
		Builder builder = new Builder();
		builder.addContactPoint("localhost");
		try (Cluster cluster = builder.build(); Session session = cluster.connect()) {
			session.execute("insert into gaia_als.log (id, application_id) values (uuid(), uuid())");
			ResultSet rs = session.execute("select * from gaia_als.log");
			List<Row> rows = rs.all();
			for (Row row : rows) {
				System.out.println(row.toString());
			}
		}
	}

}
