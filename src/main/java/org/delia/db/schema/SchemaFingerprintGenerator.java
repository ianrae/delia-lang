package org.delia.db.schema;

import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.delia.rule.DRule;
import org.delia.rule.rules.RelationManyRule;
import org.delia.rule.rules.RelationOneRule;
import org.delia.rule.rules.UniqueFieldsRule;
import org.delia.type.BuiltInTypes;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.TypePair;
import org.delia.util.DRuleHelper;
import org.delia.util.StringUtil;

public class SchemaFingerprintGenerator {
	public static final int SCHEMA_VERSION = 2;
	//v2 - feb2021 - add support for sizeof
	//v1 - may2020 - initial version
	
	
	private DTypeRegistry registry;

	public String createFingerprint(DTypeRegistry registry) {
		this.registry = registry;
		String s = String.format("(v%d)", SCHEMA_VERSION);
		
		//because of re-executing with forward decls some types are in registry.orderedList twice
		//use a map to ensure only do each type once
		Map<DType,DType> dupMap = new ConcurrentHashMap<>();
		
		List<DType> list = registry.getOrderedList();
		for(DType type: list) {
			if (isBuiltInType(type) || !type.isStructShape()) {
				continue;
			}
			
			type = registry.getType(type.getName()); //get the real one (avoid earlier ones form re-execution
			if (dupMap.containsKey(type)) {
				continue;
			}
			dupMap.put(type, type);
			
			s += String.format("%s", type.getName());
			String parent = type.getBaseType() == null ? "" : type.getBaseType().getName();
			s += String.format(":struct:%s{", parent);
			
			int i = 0;
			if (type.isStructShape()) {
				DStructType dtype = (DStructType) type;
				for(TypePair pair: dtype.getAllFields()) {
					if (i > 0) {
						s += ",";
					}
					s += genField(dtype, pair);
					i++;
				}
			}
			
			s += addUniqueFieldsConstraints(type);
			s += "}\n";
		}
		
		return s;
	}

	private String addUniqueFieldsConstraints(DType type) {
		StringJoiner joiner = new StringJoiner(";");
		for(DRule rule: type.getRawRules()) {
			if (rule instanceof UniqueFieldsRule) {
				UniqueFieldsRule ufr = (UniqueFieldsRule) rule;
				List<String> list = ufr.getOperList().stream().map(x -> x.getSubject()).collect(Collectors.toList());
				String s = String.format("UFR(%s)", StringUtil.flatten(list));
				joiner.add(s);
			}
		}
		return joiner.toString();
	}

	private String genField(DStructType dtype, TypePair pair) {
		String flags = "";
		if (dtype.fieldIsOptional(pair.name)) {
			flags += "O";
		}
		if (dtype.fieldIsPrimaryKey(pair.name)) {
			flags += "P";
		}
		if (dtype.fieldIsUnique(pair.name)) {
			flags += "U";
		}
		if (dtype.fieldIsSerial(pair.name)) {
			flags += "S";
		}
		
		//relation codes
		// a - relation one parent
		// b - relation one         (child)
		// c = relation many parent
		// d = relation many        (child) -can this occur?
		int datId = 0;
		RelationOneRule oneRule = DRuleHelper.findOneRule(dtype.getName(), pair.name, registry);
		if (oneRule != null) {
			flags += oneRule.relInfo.isParent ? "a" : "b"; 
			datId = oneRule.relInfo.getDatId() == null ? 0 : oneRule.relInfo.getDatId();
		} else {
			RelationManyRule manyRule = DRuleHelper.findManyRule(dtype.getName(), pair.name, registry);
			if (manyRule != null) {
				flags += manyRule.relInfo.isParent ? "c" : "d"; 
				datId = manyRule.relInfo.getDatId() == null ? 0 : manyRule.relInfo.getDatId();
			}
		}
		
		String fldType = getTypeAsString(pair);
		String sizeofStr = calcSizeofStr(dtype, pair);
		String s = String.format("%s:%s%s:%s/%d", pair.name, fldType, sizeofStr, flags, datId);
		return s;
	}
	private String calcSizeofStr(DStructType dtype, TypePair pair) {
		int n = DRuleHelper.getSizeofField(dtype, pair.name);
		if (n != 0) {
			return String.format("(%d)", n);
		}
		return "";
	}

	private String getTypeAsString(TypePair pair) {
		try {
			BuiltInTypes fieldType = BuiltInTypes.valueOf(pair.type.getName());
			return BuiltInTypes.getDeliaTypeName(fieldType);
		} catch (Exception e) {
		}
		return pair.type.getName();
	}

	private boolean isBuiltInType(DType type) {
		for(BuiltInTypes bintype: BuiltInTypes.values()) {
			if (type.getName().equals(bintype.name())) {
				return true;
			}
		}
		return false;
	}
}