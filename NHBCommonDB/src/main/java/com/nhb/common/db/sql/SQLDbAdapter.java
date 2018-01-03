package com.nhb.common.db.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import com.nhb.common.BaseLoggable;

public abstract class SQLDbAdapter extends BaseLoggable {

	private String dataSourceName = "default";
	private Properties config;
	private SQLDataSourceManager dataSourceManager;

	public SQLDbAdapter(SQLDataSourceManager dataSourceManager) {
		this.dataSourceManager = dataSourceManager;
	}

	public Connection getRawConnection() {
		if (getConfig() != null) {
			try {
				return DriverManager.getConnection(getConfig().getProperty("url"), getConfig());
			} catch (SQLException e) {
				getLogger().error("cannot get connection from config: " + getConfig(), e);
			}
		}
		return null;
	}

	public DataSource getDataSource() {
		if (this.getDataSourceName() != null) {
			return this.dataSourceManager.getDataSource(getDataSourceName());
		}
		return null;
	}

	public Connection getConnection() {
		if (this.getDataSourceName() != null) {
			try {
				return this.dataSourceManager.getDataSource(getDataSourceName()).getConnection();
			} catch (SQLException e) {
				getLogger().error("get connection from datasource error", e);
			}
		}
		return null;
	}

	public String getDataSourceName() {
		return this.dataSourceName;
	}

	protected void setDataSourceName(String dataSourceName) {
		this.dataSourceName = dataSourceName;
	}

	public Properties getConfig() {
		if (config != null) {
			return config;
		} else {
			return this.dataSourceManager.getConfig(getDataSourceName());
		}
	}

	public void setConfig(Properties config) {
		this.config = config;
	}

	public SQLDataSourceManager getDataSourceManager() {
		return dataSourceManager;
	}

	public void setDataSourceManager(SQLDataSourceManager dataSourceManager) {
		this.dataSourceManager = dataSourceManager;
	}
}
