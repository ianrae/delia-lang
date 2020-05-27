package org.delia.assoc;

public class DatIdMapHelper {

	public static String createKey(String typeName, String fieldName) {
		String key = String.format("%s.%s", typeName, fieldName);
		return key;
	}
}
