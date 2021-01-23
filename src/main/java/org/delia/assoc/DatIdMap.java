package org.delia.assoc;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.delia.hld.RelationSpec;
import org.delia.relation.RelationInfo;
import org.delia.util.DeliaExceptionHelper;

public class DatIdMap {
	public static class DatInfo {
		public String tableName; //eg CustomerAddressDat1
		public String left;  //eg. Customer.addr. Holds Customer pk values
		public String right; //eg. Address.cust Holds Address pk values
		
		public DatInfo() {
		}
		public DatInfo(String tableName, String left, String right) {
			this.tableName = tableName;
			this.left = left;
			this.right = right;
		}
		public boolean matchesLeft(RelationInfo relinfo) {
			String s = String.format("%s.%s", relinfo.nearType, relinfo.fieldName);
			return s.equals(left);
		}
		public boolean matchesRight(RelationInfo relinfo) {
			String s = String.format("%s.%s", relinfo.nearType, relinfo.fieldName);
			return s.equals(right);
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
	public DatInfo getAssocTblInfo(int datId) {
		DatInfo info = tblNameMap.get(datId);
		return info;
	}

	//AddressCustomerDat1, so "Address" is left type
	public boolean isLeftType(String assocTblName, RelationInfo relinfo) {
		Integer datId = relinfo.getDatId();
		if (datId == null) {
			DeliaExceptionHelper.throwError("bad-dat-id", "no DAT id for %s", assocTblName);
		}
		DatInfo info = tblNameMap.get(datId);
		return info.matchesLeft(relinfo);
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

	public RelationSpec createRelationSpec(RelationInfo relinfo) {
		RelationSpec relspec = new RelationSpec();
		relspec.relinfo = relinfo;
		if (isFlipped(relinfo)) {
			relspec.fieldName = relinfo.otherSide.fieldName;
			relspec.flipped = true;
			relspec.otherFieldName = relinfo.fieldName; //TODO: fix if one sided
			relspec.otherStructType = relinfo.nearType;
			relspec.structType = relinfo.farType;
		} else {
			relspec.fieldName = relinfo.fieldName;
			relspec.flipped = false;
			relspec.otherFieldName = relinfo.otherSide.fieldName; //TODO: fix if one sided
			relspec.otherStructType = relinfo.farType;
			relspec.structType = relinfo.nearType;
		}
		
		return relspec;
	}
	
	public boolean isFlipped(RelationInfo relinfo) {
		String field = getAssocFieldFor(relinfo);
		return field.equals("rightv");
	}
	
	public String getAssocFieldFor(RelationInfo relinfo) {
		Integer datId = relinfo.getDatId();
		if (datId == null) {
			DeliaExceptionHelper.throwError("bad-dat-id2", "no DAT id for field %s", relinfo.fieldName);
		}
		DatInfo info = tblNameMap.get(datId);
		if (info.matchesLeft(relinfo)) {
			return "leftv";
		} else {
			return "rightv";
		}
	}
	//other field
	public String getAssocOtherField(RelationInfo relinfo) {
		Integer datId = relinfo.getDatId();
		if (datId == null) {
			DeliaExceptionHelper.throwError("bad-dat-id3", "no DAT id for field %s", relinfo.fieldName);
		}
		DatInfo info = tblNameMap.get(datId);
		if (info.matchesLeft(relinfo)) {
			return "rightv";
		} else {
			return "leftv";
		}
	}

}
