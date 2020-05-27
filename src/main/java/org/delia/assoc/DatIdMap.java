package org.delia.assoc;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.delia.type.DType;

public class DatIdMap {
	private static class DatInfo {
		public String tableName;
		public String left;
		public String right;
		
		public DatInfo() {
		}
		public DatInfo(String tableName, String left, String right) {
			this.tableName = tableName;
			this.left = left;
			this.right = right;
		}
	}
	
	private Map<String,Integer> datIdMap = new ConcurrentHashMap<>(); //key(type.field),datId
	private Map<Integer,DatInfo> tblNameMap = new ConcurrentHashMap<>(); //datId,tblName
	
	public Integer get(String key) {
		return datIdMap.get(key);
	}
	public void put(String key, int datId) {
		datIdMap.put(key, datId);
		tblNameMap.put(datId, new DatInfo()); //we will replace this shortly
	}
	public void putFull(String key, int datId, String tblName, String left, String right) {
		DatInfo info = new DatInfo(tblName, left, right);
		datIdMap.put(key, datId);
		tblNameMap.put(datId, info);
	}
	public int size() {
		return datIdMap.size();
	}
	
	public void attachTblName(int datId, String tblName, String left, String right) {
		//put into map even if not in datIdMap because if we deleted a field,
		//then this info will be needed during schema migration
		DatInfo info = new DatInfo(tblName, left, right);
		tblNameMap.put(datId, info);
	}
	
	public String getAssocTblName(int datId) {
		DatInfo info = tblNameMap.get(datId);
		return info == null ? null : info.tableName;
	}

	//AddressCustomerDat1, so "Address" is left type
	public boolean isLeftType(String assocTblName, DType dtype) {
		return assocTblName.startsWith(dtype.getName());
	}
	
	//used for consistency check
	public int getNumUniqueDatIds() {
		Map<Integer,String> tmp = new HashMap<>();
		for(String key: datIdMap.keySet()) {
			Integer id = datIdMap.get(key);
			tmp.put(id, "");
		}
		return tmp.size();
	}

}
