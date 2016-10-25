package nhb.common.db.sql.daos;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;

public abstract class BaseMySqlDAO extends AbstractDAO {
	@SqlUpdate("SET FOREIGN_KEY_CHECKS=:value")
	public abstract void setForeignKeyChecks(@Bind("value") boolean value);

	@SqlUpdate("SET UNIQUE_CHECKS=:value")
	public abstract void setUniqueChecks(@Bind("value") boolean value);

	@SqlQuery("SELECT LAST_INSERT_ID()")
	public abstract int getLastInsertedId();
}
