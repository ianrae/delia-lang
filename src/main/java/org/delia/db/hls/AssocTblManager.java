package org.delia.db.hls;

import java.util.Map;

import org.delia.assoc.DatIdMap;
import org.delia.type.DStructType;

public class AssocTblManager {
	private DatIdMap datIdMap;
	
	public AssocTblManager(DatIdMap datIdMap) {
		this(datIdMap, null);
	}
	public AssocTblManager(DatIdMap datIdMap, Map<String,String> existsMapParam) {
		this.datIdMap = datIdMap;
	}
	
	public DatIdMap getDatIdMap() {
		return datIdMap;
	}
	
	
	public String xgetAssocLeftField(DStructType type1, String assocTbl) {
		if (assocTbl.startsWith(type1.getName())) {
			return "leftv";
		} else {
			return "rightv";
		}
	}
	public String xgetAssocRightField(DStructType type1, String assocTbl) {
		if (assocTbl.startsWith(type1.getName())) {
			return "rightv";
		} else {
			return "leftv";
		}
	}
	
}