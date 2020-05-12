package org.delia.db.hls;

import java.util.HashMap;
import java.util.Map;

import org.delia.type.DStructType;
import org.delia.type.TypePair;

public class AliasAllocator {
	protected int nextAliasIndex = 0;
	private Map<String,String> aliasMap = new HashMap<>(); //typeName,alias
	
	public AliasAllocator() {
	}
	
	public void createAlias(String name) {
		char ch = (char) ('a' + nextAliasIndex++);
		String s = String.format("%c", ch);
		aliasMap.put(name, s);
	}

	public String findOrCreateFor(DStructType structType) {
		if (! aliasMap.containsKey(structType.getName())) {
			createAlias(structType.getName());
		}
		return aliasMap.get(structType.getName());
	}
	public String buildTblAlias(DStructType structType) {
		String alias = findOrCreateFor(structType);
		String s = String.format("%s as %s", structType.getName(), alias);
		return s;
	}
	public String buildAlias(DStructType pairType, TypePair pair) {
		String alias = findOrCreateFor(pairType);
		String s = String.format("%s.%s", alias, pair.name);
		return s;
	}
	public String buildAlias(DStructType pairType, String fieldName) {
		String alias = findOrCreateFor(pairType);
		String s = String.format("%s.%s", alias, fieldName);
		return s;
	}
	
	//----
	public String findOrCreateForAssoc(String tblName) {
		if (! aliasMap.containsKey(tblName)) {
			createAlias(tblName);
		}
		return aliasMap.get(tblName);
	}
	public String buildTblAliasAssoc(String tblName) {
		String alias = findOrCreateForAssoc(tblName);
		String s = String.format("%s as %s", tblName, alias);
		return s;
	}
	public String buildAliasAssoc(String tblName, String fieldName) {
		String alias = findOrCreateForAssoc(tblName);
		String s = String.format("%s.%s", alias, fieldName);
		return s;
	}

}