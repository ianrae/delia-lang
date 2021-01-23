package org.delia.hld;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import org.delia.assoc.DatIdMap;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.hls.AliasInfo;
import org.delia.relation.RelationInfo;
import org.delia.type.DStructType;
import org.delia.type.TypePair;
import org.delia.util.DRuleHelper;
import org.delia.util.DValueHelper;

public class HLDAliasManager extends ServiceBase {
	private Map<String,AliasInfo> map = new HashMap<>(); //key is type or type.field
	private Map<String,AliasInfo> assocMap = new HashMap<>(); //key is type.field
	protected int nextAliasIndex = 0;
	private DatIdMap datIdMap;
	private Stack<String> scopeStack = new Stack<>();
	
	public HLDAliasManager(FactoryService factorySvc, DatIdMap datIdMap) {
		super(factorySvc);
		this.datIdMap = datIdMap;
	}
	
	public String createAlias() {
//		char ch = (char) ('a' + nextAliasIndex++);
//		String alias = String.format("%c", ch);
		String alias = String.format("t%d", nextAliasIndex++);
//		System.out.println("ALIAS " + alias);
		return alias;
	}
	
	public String dumpToString() {
		List<AliasInfo> list = new ArrayList<>();
		for(String key: map.keySet()) {
			list.add(map.get(key));
		}
		for(String key: assocMap.keySet()) {
			list.add(assocMap.get(key));
		}
		List<AliasInfo> sortedList = list.stream()
        .sorted(Comparator.comparing(AliasInfo::getAlias))
        .collect(Collectors.toList());		
		
		StringJoiner joiner = new StringJoiner(",");
		for(AliasInfo info: sortedList) {
			if (info.fieldName == null) {
				String s = String.format("%s=%s", info.alias, info.tblName);
				joiner.add(s);
			} else {
				String assoc = info.tblType == null ? String.format("(%s)", info.tblName) : "";
				String s = String.format("%s=.%s%s", info.alias, info.fieldName, assoc);
				joiner.add(s);
			}
		}
		return joiner.toString();
	}
	
	public AliasInfo createMainTableAlias(DStructType structType) {
		AliasInfo info = getMainTableAlias(structType);
		if (info != null) {
			return info;
		}
		
		info = new AliasInfo();
		info.alias = createAlias();
		info.structType = structType;
		info.fieldName = null;
		
		info.tblType = structType;
		info.tblName = info.tblType.getName();
		
		String key = makeMainTableKey(structType.getName());
		map.put(key, info);
		return info;
	}

	public AliasInfo createFieldAlias(RelationInfo relinfo) {
		AliasInfo info1 = getFieldAlias(relinfo.nearType, relinfo.fieldName);
		if (info1 != null) {
			return info1;
		}
		AliasInfo info2 = getFieldAlias(relinfo.otherSide.nearType, relinfo.otherSide.fieldName);
		if (info2 != null) {
			return info2;
		}
		return createFieldAlias(relinfo.nearType, relinfo.fieldName);
	}
	//note. Customer.addr is an alias for Address (not Customer)
	public AliasInfo createFieldAlias(DStructType structType, String fieldName) {
		AliasInfo info = getFieldAlias(structType, fieldName);
		if (info != null) {
			return info;
		}
		info = new AliasInfo();
		info.alias = createAlias();
		info.structType = structType;
		info.fieldName = fieldName;
		
		TypePair pair = DValueHelper.findField(structType, fieldName);
		info.tblType = (DStructType) pair.type;
		info.tblName = info.tblType.getName();
		
		String key = String.format("%s.%s", structType.getName(), fieldName);
		map.put(key, info); //TODO: support scope later
		return info;
	}
	public AliasInfo createOrGetFieldAliasAdditional(DStructType structType, String fieldName) {
		AliasInfo info = getFieldAliasAdditional(structType, fieldName);
		if (info != null) {
			return info;
		}
		info = new AliasInfo();
		info.alias = createAlias();
		info.structType = structType;
		info.fieldName = fieldName;
		
		TypePair pair = DValueHelper.findField(structType, fieldName);
		info.tblType = (DStructType) pair.type;
		info.tblName = info.tblType.getName();
		
		String key = String.format("ADD_%s.%s", structType.getName(), fieldName);
		map.put(key, info); //TODO: support scope later
		return info;
	}
	public AliasInfo getFieldAlias(DStructType structType, String fieldName) {
		String key = String.format("%s.%s", structType.getName(), fieldName);
		return map.get(key);
	}
	public AliasInfo getFieldAliasAdditional(DStructType structType, String fieldName) {
		String key = String.format("ADD_%s.%s", structType.getName(), fieldName);
		return map.get(key);
	}
	
	
	
	public AliasInfo createAssocAlias(DStructType structType, String fieldName, String assocTbl) {
		AliasInfo info = getAssocAlias(structType, fieldName, assocTbl);
		if (info != null) {
			return info;
		}
		//if is other side of same relation we don't need to add it again
		TypePair pair = DValueHelper.findField(structType, fieldName);
		RelationInfo relinfo = DRuleHelper.findMatchingRuleInfo(structType, pair);
//		if (relinfo != null && relinfo.otherSide != null) {
//			boolean isSelfJoin = relinfo.nearType == relinfo.farType;
//			if (! isSelfJoin && getAssocAlias(relinfo.otherSide.nearType, relinfo.otherSide.fieldName, assocTbl) != null) {
//				return;
//			}
//		}
				
		info = new AliasInfo();
		info.alias = createAlias();
		info.structType = structType;
		info.fieldName = fieldName;
		
		info.tblType = null;
		info.tblName = assocTbl;
		
		String key = String.format("%s.%s", structType.getName(), fieldName);
		assocMap.put(key, info);
		return info;
	}
	public AliasInfo findAssocAlias(RelationInfo relinfo, String assocTbl) {
		AliasInfo info = getAssocAliasSimple(relinfo.nearType, relinfo.fieldName);
		if (info != null) {
			return info;
		}
		info = getAssocAliasSimple(relinfo.farType, relinfo.otherSide.fieldName);
		if (info != null) {
			return info;
		}
		return null;
	}
	
	private AliasInfo getMainTableAlias(DStructType structType) {
		String key = makeMainTableKey(structType.getName());
		return map.get(key);
	}
	private AliasInfo getAssocAlias(DStructType structType, String fieldName, String assocTbl) {
		String key = String.format("%s.%s", structType.getName(), fieldName);
		AliasInfo info = assocMap.get(key);
		if (info != null) {
			return info;
		}

		//check other side
		TypePair pair = DValueHelper.findField(structType, fieldName);
		RelationInfo relinfo = DRuleHelper.findMatchingRuleInfo(structType, pair);
		if (relinfo != null && relinfo.otherSide != null) {
			boolean isSelfJoin = relinfo.nearType == relinfo.farType;
			if (isSelfJoin) return null;
			
			key = String.format("%s.%s", relinfo.otherSide.nearType.getName(), relinfo.otherSide.fieldName);
			info = assocMap.get(key);
			if (info != null) {
				return info;
			}
		}
		return null;
	}
	private AliasInfo getAssocAliasSimple(DStructType structType, String fieldName) {
		String key = String.format("%s.%s", structType.getName(), fieldName);
		AliasInfo info = assocMap.get(key);
		if (info != null) {
			return info;
		}
		return null;
	}

	public DatIdMap getDatIdMap() {
		return datIdMap;
	}

	public void pushScope(String scope) {
		scopeStack.push(scope);
	}

	public void popScope() {
		scopeStack.pop();
	}
	private String makeMainTableKey(String typeName) {
		String prefix = scopeStack.isEmpty() ? "" : scopeStack.peek();
		String key = String.format("%s%s", prefix, typeName);
		return key;
	}

}