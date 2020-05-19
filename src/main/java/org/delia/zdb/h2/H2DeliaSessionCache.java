package org.delia.zdb.h2;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//store data here that can be shared across instances of ZExecutor.
public class H2DeliaSessionCache {
	public static class CacheData {
		
	}
	
	private Map<Object,CacheData> cacheMap = new ConcurrentHashMap<>();
	
	public synchronized CacheData findOrCreate(Object sessionIdentifier) {
		CacheData cache = cacheMap.get(sessionIdentifier);
		if (cache == null) {
			cache = new CacheData();
			cacheMap.put(sessionIdentifier, cache);
		}
		return cache;
	}
	
}
