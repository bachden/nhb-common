package com.nhb.common.db.sql.daos;

import java.io.Closeable;

public abstract class AbstractDAO implements Closeable {

	@Override
	public abstract void close();
}
