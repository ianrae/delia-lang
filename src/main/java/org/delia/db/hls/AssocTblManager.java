package org.delia.db.hls;

import java.util.Map;

import org.delia.assoc.DatIdMap;
import org.delia.db.TableExistenceService;
import org.delia.type.DStructType;

public class AssocTblManager {
//	private TableExistenceService existSvc;
//	private Map<String,String> existsMap = new HashMap<>(); //assocTblName,""
	private DatIdMap datIdMap;
	
	public AssocTblManager(TableExistenceService existSvc, DatIdMap datIdMap) {
		this(existSvc, datIdMap, null);
	}
	public AssocTblManager(TableExistenceService existSvc, DatIdMap datIdMap, Map<String,String> existsMapParam) {
//		this.existSvc = existSvc;
		this.datIdMap = datIdMap;
//		if (existsMapParam != null) {
//			this.existsMap = existsMapParam;
//		}
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