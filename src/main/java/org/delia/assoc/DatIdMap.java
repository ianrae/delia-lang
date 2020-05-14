package org.delia.assoc;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DatIdMap {
	private Map<String,Integer> datIdMap = new ConcurrentHashMap<>(); //key(type.field),datId
	private Map<Integer,String> tblNameMap = new ConcurrentHashMap<>(); //datId,tblName
	
	public Integer get(String key) {
		return datIdMap.get(key);
	}
	public void put(String key, int datId) {
		datIdMap.put(key, datId);
		tblNameMap.put(datId, "?"); //we will replace this shortly
	}
	public int size() {
		return datIdMap.size();
	}
	
	public void attachTblName(int datId, String tblName) {
		if (tblNameMap.containsKey(datId)) {
			tblNameMap.put(datId, tblName);
		}
	}
	
	public String getAssocTblName(int datId) {
		return tblNameMap.get(datId);
	}

}
