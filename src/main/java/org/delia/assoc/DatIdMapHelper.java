package org.delia.assoc;

public class DatIdMapHelper {
	
	public static final String LEFTNAME = "leftName";
	public static final String RIGHTNAME = "rightName";

	public static String createKey(String typeName, String fieldName) {
		String key = String.format("%s.%s", typeName, fieldName);
		return key;
	}
	
	public static String getAssocTblField(boolean isLeft) {
		return isLeft ? "leftv" : "rightv";
	}
}
