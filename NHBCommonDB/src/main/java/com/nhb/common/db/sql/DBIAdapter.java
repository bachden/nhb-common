package com.nhb.common.db.sql;

import javax.sql.DataSource;

import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;

import com.nhb.common.db.sql.daos.AbstractDAO;

public class DBIAdapter extends SQLDbAdapter {

	private DBI dbi;
	private DataSource dataSource;

	public DBIAdapter(SQLDataSourceManager dataSourceManager) {
		super(dataSourceManager);
	}

	public DBIAdapter(DataSource dataSource) {
		super(null);
		this.dataSource = dataSource;
	}

	public DBIAdapter(SQLDataSourceManager dataSourceManager, String dataSourceName) {
		super(dataSourceManager);
		this.setDataSourceName(dataSourceName);
	}

	@Override
	public DataSource getDataSource() {
		if (this.dataSource != null) {
			return this.dataSource;
		}
		return super.getDataSource();
	}

	public DBI getDBI() {
		if (this.dbi == null) {
			synchronized (this) {
				if (this.dbi == null) {
					this.dbi = new DBI(this.getDataSource());
				}
			}
		}
		return dbi;
	}

	public Handle newHandle() {
		return this.getDBI().open();
	}

	public <T extends AbstractDAO> T openDAO(Class<T> daoClass) {
		return this.getDBI().open(daoClass);
	}
}
