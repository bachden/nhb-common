package com.nhb.common.db.sql;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import com.mysql.cj.jdbc.MysqlDataSource;
import com.nhb.common.BaseLoggable;
import com.nhb.common.utils.ObjectUtils;
import com.nhb.common.utils.ObjectUtils.Setter;
import com.nhb.common.utils.PrimitiveTypeUtils;

public class MysqlDataSourceCreator extends BaseLoggable implements DataSourceCreator {

	@Override
	public DataSource createDataSource(Properties props) throws Exception {
		if (props != null) {
			MysqlDataSource ds = new MysqlDataSource();

			final Map<String, Setter> setters = ObjectUtils.findAllClassSetters(MysqlDataSource.class);

			for (Object keyObj : props.keySet()) {
				String key = (String) keyObj;

				if (key.equalsIgnoreCase("dataSourceCreatorClass")) {
					continue;
				}

				String value = (String) props.get(key);

				if (value == null) {
					continue;
				}

				Setter setter = setters.get(key);
				if (setter != null && setter.isUsingMethod()) {
					Class<?> paramType = setter.getParamType();
					Object castValue = null;
					if (PrimitiveTypeUtils.isPrimitiveOrWrapperType(paramType)) {
						if (paramType == Boolean.class || paramType == Boolean.TYPE) {
							castValue = PrimitiveTypeUtils.getBooleanValueFrom(value);
						} else if (paramType == String.class) {
							castValue = value;
						} else if (paramType == Integer.class || paramType == Integer.TYPE) {
							castValue = PrimitiveTypeUtils.getIntegerValueFrom(value);
						} else if (paramType == Long.class || paramType == Long.TYPE) {
							castValue = PrimitiveTypeUtils.getLongValueFrom(value);
						} else if (paramType == Float.class || paramType == Float.TYPE) {
							castValue = PrimitiveTypeUtils.getFloatValueFrom(value);
						} else if (paramType == Double.class || paramType == Double.TYPE) {
							castValue = PrimitiveTypeUtils.getDoubleValueFrom(value);
						} else if (paramType == Byte.class || paramType == Byte.TYPE) {
							castValue = PrimitiveTypeUtils.getByteValueFrom(value);
						} else if (paramType == Short.class || paramType == Short.TYPE) {
							castValue = PrimitiveTypeUtils.getShortValueFrom(value);
						} else if (paramType == Character.class || paramType == Character.TYPE) {
							castValue = PrimitiveTypeUtils.getCharValueFrom(value);
						}
						if (castValue != null) {
							setter.set(ds, castValue);
							getLogger().debug("{} = {}", key, castValue);
						} else {
							getLogger().warn("Setter for key {} has param type {} cannot be interpreted from value {}",
									key, paramType.getName(), value);
						}
					} else {
						getLogger().warn("Setter for key " + key + " doesn't allow primitive type, ignore by default");
					}
				}
			}
			return ds;
		}
		return null;
	}

	public static void main(String[] args) throws Exception {
		Properties props = new Properties();
		try (InputStream inStream = new FileInputStream("conf/mysql-default.properties")) {
			props.load(inStream);
		}
		DataSource createDataSource = new MysqlDataSourceCreator().createDataSource(props);
		try (Connection conn = createDataSource.getConnection()) {
			Statement stmt = conn.createStatement();
			ResultSet result = stmt.executeQuery("SHOW TABLES");
			while (result.next()) {
				System.out.println("Table: " + result.getObject(1));
			}
		}
	}

}
