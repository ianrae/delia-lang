package org.delia.assoc;

import org.delia.type.DStructType;

public class DatIdMapHelper {

	public static String createKey(String typeName, String fieldName) {
		String key = String.format("%s.%s", typeName, fieldName);
		return key;
	}
	
	public static String getAssocLeftField(DStructType type1, String assocTbl) {
		if (assocTbl.startsWith(type1.getName())) {
			return "leftv";
		} else {
			return "rightv";
		}
	}
	public static String getAssocRightField(DStructType type1, String assocTbl) {
		if (assocTbl.startsWith(type1.getName())) {
			return "rightv";
		} else {
			return "leftv";
		}
	}
	
}
