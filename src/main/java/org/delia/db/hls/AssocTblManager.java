package org.delia.db.hls;

import java.util.HashMap;
import java.util.Map;

import org.delia.db.TableExistenceService;
import org.delia.type.DStructType;
import org.delia.util.DeliaExceptionHelper;

public class AssocTblManager {
	private TableExistenceService existSvc;
	private Map<String,String> existsMap = new HashMap<>();
	
	public AssocTblManager(TableExistenceService existSvc) {
		this.existSvc = existSvc;
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
		
		DeliaExceptionHelper.throwError("cant-find-assoc-tbl", "Can't find assoc table: %s or %s", assocTblName1, assocTblName2);
		return null; //unknown table flip ? "AddressCustomerAssoc" : "CustomerAddressAssoc"; //type1 on left
	}
	private String buildName(DStructType type1, DStructType type2) {
		String tblName = String.format("%s%sAssoc", type1.getName(), type2.getName());
		return tblName;
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
		if (existsMap.containsKey(assocTblName1)) {
			return "rightv";
		}
		
		String assocTblName2 = buildName(type2, type1);
		if (existsMap.containsKey(assocTblName2)) {
			return "leftv";
		}
		
		return null; //trouble
	}
	//produce "leftv" for normal table, "rightv" for flipped table
	public String getAssocLeftField(DStructType type1, DStructType type2) {
		getTableFor(type1, type2); //fill in existsMap
		
		String assocTblName1 = buildName(type1,type2);
		if (existsMap.containsKey(assocTblName1)) {
			return "leftv";
		}
		
		String assocTblName2 = buildName(type2, type1);
		if (existsMap.containsKey(assocTblName2)) {
			return "rightv";
		}
		
		return null; //trouble
	}
	
}