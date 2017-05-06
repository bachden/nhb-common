package com.nhb.common.predicate.sql;

import com.nhb.common.predicate.Predicate;

import lombok.Getter;

public class SqlPredicateThreadLocal extends ThreadLocal<Predicate> {

	@Getter
	private final String sql;

	public SqlPredicateThreadLocal(String sql) {
		this.sql = sql;
	}

	@Override
	protected Predicate initialValue() {
		return SqlPredicateParser.parse(this.sql);
	}
}
