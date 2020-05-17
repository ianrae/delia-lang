package org.delia.db.hls;

import java.util.HashMap;
import java.util.Map;

import org.delia.assoc.DatIdMap;
import org.delia.db.TableExistenceService;
import org.delia.rule.rules.RelationManyRule;
import org.delia.type.DStructType;
import org.delia.type.TypePair;
import org.delia.util.DRuleHelper;
import org.delia.util.DeliaExceptionHelper;

public class AssocTblManager {
	private TableExistenceService existSvc;
	private Map<String,String> existsMap = new HashMap<>();
	private DatIdMap datIdMap;
	
	public AssocTblManager(TableExistenceService existSvc, DatIdMap datIdMap) {
		this.existSvc = existSvc;
		this.datIdMap = datIdMap;
	}
	public String getTableFor(DStructType type1, DStructType type2) {
		String assocTblName1 = buildName(type1,type2);
		if (existsMap.containsKey(assocTblName1)) {
			return assocTblName1;
		}
		
		String assocTblName2 = buildName(type2, type1);
		if (existsMap.containsKey(assocTblName2)) {
			return assocTblName2;
		}
		
		if (existSvc.doesTableExist(assocTblName1)) {
			existsMap.put(assocTblName1, "");
			return assocTblName1;
		}
		
		if (existSvc.doesTableExist(assocTblName2)) {
			existsMap.put(assocTblName2, "");
			return assocTblName2;
		}
		
		DeliaExceptionHelper.throwError("cant-find-assoc-tbl", "Can't find assoc table: %s", assocTblName1);
		return null; //unknown table flip ? "AddressCustomerAssoc" : "CustomerAddressAssoc"; //type1 on left
	}
	private String buildName(DStructType type1, DStructType type2) {
		//TODO hack hack. later fix for multiple relations;
		for(TypePair pair: type1.getAllFields()) {
			if (pair.type.isStructShape()) {
				RelationManyRule manyRule = DRuleHelper.findManyRule(type1, pair.name);
				if (manyRule != null) {
					int datId =manyRule.relInfo.getDatId();
					return datIdMap.getAssocTblName(datId);
				}
			}
		}
		return null;
//		String tblName = String.format("%s%sAssoc", type1.getName(), type2.getName());
//		return tblName;
	}
//	public boolean isFlipped(DStructType type1, DStructType type2) {
//		String tblName = getTableFor(type1, type2);
//		
//		String assocTblName2 = buildName(type2,type1);
//		return tblName.equals(assocTblName2);
//	}
	
	//produce "rightv" for normal table, "leftf" for flipped table
	public String getAssocRightField(DStructType type1, DStructType type2) {
		getTableFor(type1, type2); //fill in existsMap
		
		String assocTblName1 = buildName(type1,type2);
		if (assocTblName1.startsWith(type1.getName())) {
			return "rightv";
		} else {
			return "leftv";
		}
		
//		
//		if (existsMap.containsKey(assocTblName1)) {
//			return "rightv";
//		}
//		
//		String assocTblName2 = buildName(type2, type1);
//		if (existsMap.containsKey(assocTblName2)) {
//			return "leftv";
//		}
//		
//		return null; //trouble
	}
	//produce "leftv" for normal table, "rightv" for flipped table
	public String getAssocLeftField(DStructType type1, DStructType type2) {
		getTableFor(type1, type2); //fill in existsMap
		
		String assocTblName1 = buildName(type1,type2);
		
		if (assocTblName1.startsWith(type1.getName())) {
			return "leftv";
		} else {
			return "rightv";
		}
		
//		if (existsMap.containsKey(assocTblName1)) {
//		}
		
//		String assocTblName2 = buildName(type2, type1);
//		if (existsMap.containsKey(assocTblName2)) {
//			return "rightv";
//		}
//		
//		return null; //trouble
	}
	public DatIdMap getDatIdMap() {
		return datIdMap;
	}
	
}