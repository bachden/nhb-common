package nhb.common.db.cassandra.daos;

import nhb.common.db.cassandra.CassandraDataSource;

public final class CassandraDAOFactory {

	private CassandraDataSource dataSource;
	private ClassLoader classLoader = this.getClass().getClassLoader();

	public CassandraDAOFactory(CassandraDataSource dataSource) {
		this.dataSource = dataSource;
	}

	@SuppressWarnings("unchecked")
	public <T extends CassandraDAO> T newDAOInstance(Class<? extends CassandraDAO> clazz) {
		Class<?> _clazz = null;
		try {
			_clazz = this.classLoader.loadClass(clazz.getName());
		} catch (ClassNotFoundException e1) {
			throw new RuntimeException(e1);
		}
		if (_clazz != null) {
			try {
				T instance = (T) _clazz.newInstance();
				if (instance instanceof BaseCassandraDAO) {
					((BaseCassandraDAO) instance).setDataSource(dataSource);
				}
				return instance;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return null;
	}

	public ClassLoader getClassLoader() {
		return classLoader;
	}

	public void setClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}
}
