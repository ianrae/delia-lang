package org.delia.assoc;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.delia.type.DType;

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
	public void putFull(String key, int datId, String tblName) {
		datIdMap.put(key, datId);
		tblNameMap.put(datId, tblName);
	}
	public int size() {
		return datIdMap.size();
	}
	
	public void attachTblName(int datId, String tblName) {
		//put into map even if not in datIdMap because if we deleted a field,
		//then this info will be needed during schema migration
		tblNameMap.put(datId, tblName);
	}
	
	public String getAssocTblName(int datId) {
		return tblNameMap.get(datId);
	}

	//AddressCustomerDat1, so "Address" is left type
	public boolean isLeftType(String assocTblName, DType dtype) {
		return assocTblName.startsWith(dtype.getName());
	}

}
