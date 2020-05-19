package org.delia.db.hls;

import java.util.HashMap;
import java.util.Map;

import org.delia.type.DStructType;
import org.delia.type.TypePair;

public class AliasAllocator {
	protected int nextAliasIndex = 0;
	private Map<String,String> aliasMap = new HashMap<>(); //typeName,alias or typeName.instanceKey,alias
	
	public AliasAllocator() {
	}
	
	public String createAlias(String name) {
		char ch = (char) ('a' + nextAliasIndex++);
		String alias = String.format("%c", ch);
		aliasMap.put(name, alias);
		return alias;
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

	//use when can have multiple joins to same table. each needs unique alias
	public AliasInstance findOrCreateAliasInstance(DStructType structType, String instanceKey) {
		String key = String.format("%s.%s", structType.getName(), instanceKey);
		if (! aliasMap.containsKey(key)) {
			createAlias(key);
		}
		
		AliasInstance aliasInst = new AliasInstance();
		aliasInst.alias = aliasMap.get(key);
		aliasInst.instanceKey = instanceKey;
		aliasInst.structType = structType;
		return aliasInst;
	}
	public String buildTblAlias(AliasInstance aliasInst) {
		String s = String.format("%s as %s", aliasInst.structType.getName(), aliasInst.alias);
		return s;
	}
	public String buildAlias(AliasInstance aliasInst, String fieldName) {
		String s = String.format("%s.%s", aliasInst.alias, fieldName);
		return s;
	}

}