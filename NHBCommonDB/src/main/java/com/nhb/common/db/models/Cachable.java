package com.nhb.common.db.models;

import java.util.Collection;

import com.hazelcast.core.IMap;

public interface Cachable<KeyClass, BeanClass> {
	IMap<KeyClass, BeanClass> getCachedMap();
	
	boolean cache(BeanClass bean);
	
	boolean cache(Collection<BeanClass> beans);
	
	void removeAllCached();
}
