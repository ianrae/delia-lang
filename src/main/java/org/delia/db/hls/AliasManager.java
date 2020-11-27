package org.delia.db.hls;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import org.delia.assoc.DatIdMap;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.hls.join.JTElement;
import org.delia.relation.RelationInfo;
import org.delia.rule.rules.RelationManyRule;
import org.delia.rule.rules.RelationOneRule;
import org.delia.type.DStructType;
import org.delia.type.TypePair;
import org.delia.util.DRuleHelper;
import org.delia.util.DValueHelper;

public class AliasManager extends ServiceBase {
	private Map<String,AliasInfo> map = new HashMap<>(); //key is type or type.field
	private Map<String,AliasInfo> assocMap = new HashMap<>(); //key is type.field
	protected int nextAliasIndex = 0;
	
	
	public AliasManager(FactoryService factorySvc) {
		super(factorySvc);
	}
	
	private String createAlias() {
		char ch = (char) ('a' + nextAliasIndex++);
		String alias = String.format("%c", ch);
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
	
	public void createMainTableAlias(DStructType structType) {
		AliasInfo info = getMainTableAlias(structType);
		if (info != null) {
			return;
		}
		
		info = new AliasInfo();
		info.alias = createAlias();
		info.structType = structType;
		info.fieldName = null;
		
		info.tblType = structType;
		info.tblName = info.tblType.getName();
		
		String key = String.format("%s", structType.getName());
		map.put(key, info);
	}
	public void createFieldAlias(DStructType structType, String fieldName) {
		AliasInfo info = getFieldAlias(structType, fieldName);
		if (info != null) {
			return;
		}
		info = new AliasInfo();
		info.alias = createAlias();
		info.structType = structType;
		info.fieldName = fieldName;
		
		TypePair pair = DValueHelper.findField(structType, fieldName);
		info.tblType = (DStructType) pair.type;
		info.tblName = info.tblType.getName();
		
		String key = String.format("%s.%s", structType.getName(), fieldName);
		map.put(key, info);
	}
	public void createAssocAlias(DStructType structType, String fieldName, String assocTbl) {
		AliasInfo info = getAssocAlias(structType, fieldName, assocTbl);
		if (info != null) {
			return;
		}
		//if is other side of same relation we don't need to add it again
		TypePair pair = DValueHelper.findField(structType, fieldName);
		RelationInfo relinfo = DRuleHelper.findMatchingRuleInfo(structType, pair);
		if (relinfo != null && relinfo.otherSide != null) {
			boolean isSelfJoin = relinfo.nearType == relinfo.farType;
			if (! isSelfJoin && getAssocAlias(relinfo.otherSide.nearType, relinfo.otherSide.fieldName, assocTbl) != null) {
				return;
			}
		}
				
		info = new AliasInfo();
		info.alias = createAlias();
		info.structType = structType;
		info.fieldName = fieldName;
		
		info.tblType = null;
		info.tblName = assocTbl;
		
		String key = String.format("%s.%s", structType.getName(), fieldName);
		assocMap.put(key, info);
	}
	
	public void buildAliases(HLSQuerySpan hlspan, DatIdMap datIdMap) {
		createMainTableAlias(hlspan.mtEl.structType);
		
		for(TypePair pair: hlspan.fromType.getAllFields()) {
			RelationOneRule oneRule = DRuleHelper.findOneRule(hlspan.fromType, pair.name);
			if (oneRule != null && (oneRule.relInfo.isParent || isFetched(hlspan, pair.name))) {
				createFieldAlias(hlspan.fromType, pair.name);
			} else {
				RelationManyRule manyRule = DRuleHelper.findManyRule(hlspan.fromType, pair.name);
				if (manyRule != null) {
					//many-to-one. many side is always the parent
					createFieldAlias(hlspan.fromType, pair.name);
					if (manyRule.relInfo.isManyToMany()) {
						String assocTbl = datIdMap.getAssocTblName(manyRule.relInfo.getDatId());
						createAssocAlias(hlspan.fromType, pair.name, assocTbl);
					}
				}
			}
		}
		
		for(JTElement el: hlspan.joinTreeL) {
			createFieldAlias(el.dtype, el.fieldName);
		}
	}
	
	private boolean isFetched(HLSQuerySpan hlspan, String fieldName) {
		if (hlspan.subEl != null) {
			return hlspan.subEl.fetchL.contains(fieldName);
		}
		return false;
	}

	public AliasInfo getMainTableAlias(DStructType structType) {
		String key = String.format("%s", structType.getName());
		return map.get(key);
	}
	public AliasInfo findAlias(DStructType structType) {
		String key = String.format("%s", structType.getName());
		AliasInfo info = map.get(key);
		if (info != null) {
			return info;
		}
		
		//look for structType match first
		for(String x: map.keySet()) {
			info = map.get(x);
			if (info.fieldName != null) {
				if (info.structType == structType) {
					return info; //TODO: won't work if multiple joins to same table.
				}
			}
		}

		//then tblType
		for(String x: map.keySet()) {
			info = map.get(x);
			if (info.fieldName != null) {
				if (info.tblType == structType) {
					return info; //TODO: won't work if multiple joins to same table.
				}
			}
		}
		return null; //oops!
	}
	public AliasInfo getFieldAlias(DStructType structType, String fieldName) {
		String key = String.format("%s.%s", structType.getName(), fieldName);
		return map.get(key);
	}
	public AliasInfo getAssocAlias(DStructType structType, String fieldName, String assocTbl) {
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
	public String buildTblAlias(AliasInfo info) {
		String s = String.format("%s as %s", info.tblName, info.alias);
		return s;
	}
	public String buildFieldAlias(AliasInfo info, String fieldName) {
		String s = String.format("%s.%s", info.alias, fieldName);
		return s;
	}
	public String buildTblAlias(AliasInfo info, boolean isBackards) {
		if (isBackards) {
			String otherTbl = info.structType.getName();
			String s = String.format("%s as %s", otherTbl, info.alias);
			return s;
		} else {
			return buildTblAlias(info);
		}
	}
	
}