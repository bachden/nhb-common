/*
 * Copyright (c) 2008-2015, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nhb.common.predicate.sql;

import com.nhb.common.predicate.Predicate;

/**
 * This class contains methods related to conversion of sql query to predicate.
 */
public class SqlPredicate implements Predicate {

	private static final long serialVersionUID = -6563529786134881341L;

	private Predicate predicate;

	public SqlPredicate() {
		// do nothing
	}

	public SqlPredicate(String sql) {
		this.parse(sql);
	}

	@Override
	public boolean apply(Object obj) {
		if (this.predicate != null) {
			return this.predicate.apply(obj);
		}
		return false;
	}

	public void parse(String sql) {
		if (sql != null) {
			// FilteredObject obj = PredicateBuilder.newFilteredObject();
		}
	}
}
