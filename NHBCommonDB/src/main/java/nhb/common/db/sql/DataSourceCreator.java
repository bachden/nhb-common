package nhb.common.db.sql;

import java.util.Properties;

import javax.sql.DataSource;

import nhb.common.Loggable;

public interface DataSourceCreator extends Loggable {

	DataSource createDataSource(Properties props) throws Exception;
}
