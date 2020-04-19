package org.delia.log;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.delia.util.StringUtil;

/**
 * The application can provide a list of log levels.
 * Each element is a full or partial class path or package name followed by "=" and a log level
 * 
 * IMPORTANT - not thread-safe (uses TreeMap)
 * 
 * IMPORTANT: The list must be in order from most-specific to least specific. The first element
 *  in the list that matches is used.!
 * 
 *  #always put most specific ones first
 *  com.foo.bar.cart.CartService=ERROR
 *  com.foo.bar.product=DEBUG
 *  com.foo.bar=INFO
 *  
 * @author ian
 *
 */
public class LogLevelMapBuilder {
	private Map<String,LogLevel> levelMap = new TreeMap<>(); //sorted

	public void buildMap(List<String> levelMapList) {
		if (levelMapList == null || levelMapList.isEmpty()) {
			return;
		}
		
		int index = 0;
		for(String s: levelMapList) {
			String[] ar = s.split("=");
			if (ar.length == 2) {
				String partialClassName = ar[0];
				LogLevel level = null;
				try {
					level = LogLevel.valueOf(ar[1]);
				} catch (Exception e) {
					System.err.println("invalid log level line: " + ar[1]);
				}
				
				if (StringUtil.hasText(partialClassName) && level != null) {
					//id is to maintain same order as levelMapList
					String id = String.format("%03d-%s", index, partialClassName);
					levelMap.put(id, level);
				}
			}
		}
	}
	
	public LogLevel calcLevel(String name, LogLevel defaultLevel) {
		for(String key: levelMap.keySet()) {
			String partialPath = key.substring(key.indexOf('-') + 1);
			if (name.contains(partialPath)) {
				return levelMap.get(key);
			}
		}
		return defaultLevel;
	}


}
