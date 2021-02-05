package org.delia.db.schema.modify;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.delia.core.FactoryService;
import org.delia.core.RegAwareServiceBase;
import org.delia.rule.DRule;
import org.delia.rule.rules.IndexRule;
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

/**
 * Converts the registry of types into a schema definition object,
 * that can be saved as JSON.
 * @author ian
 *
 */
public class SchemaDefinitionGenerator extends RegAwareServiceBase {
	public static final int VERSION = 3;
	
	public SchemaDefinitionGenerator(DTypeRegistry registry, FactoryService factorySvc) {
		super(registry, factorySvc);
	}
	
	public SchemaDefinition generate() {
		SchemaDefinition schema = new SchemaDefinition();
		schema.ver = VERSION;
		
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
			
			SxTypeInfo typeInfo = new SxTypeInfo();
			typeInfo.nm = type.getName();
			typeInfo.ba = type.getBaseType() == null ? "" : type.getBaseType().getName();
			
			int i = 0;
			if (type.isStructShape()) {
				DStructType dtype = (DStructType) type;
				for(TypePair pair: dtype.getAllFields()) {
					typeInfo.flds.add(genField(dtype, pair));
					i++;
				}
				
				addUniqueFieldsConstraints(dtype, schema);
			}
			
			schema.types.add(typeInfo);
		}
		
		return schema;
	}
	
	private void addUniqueFieldsConstraints(DType type, SchemaDefinition schema) {
		for(DRule rule: type.getRawRules()) {
			if (rule instanceof UniqueFieldsRule) {
				UniqueFieldsRule ufr = (UniqueFieldsRule) rule;
				List<String> list = ufr.getOperList().stream().map(x -> x.getSubject()).collect(Collectors.toList());
				
				SxOtherInfo otherInfo = new SxOtherInfo();
				otherInfo.nm = type.getName();
				otherInfo.args = list;
				otherInfo.ct = "uniqueFields";
				schema.others.add(otherInfo);
			} else if (rule instanceof IndexRule) {
				IndexRule ufr = (IndexRule) rule;
				List<String> list = ufr.getOperList().stream().map(x -> x.getSubject()).collect(Collectors.toList());
				
				SxOtherInfo otherInfo = new SxOtherInfo();
				otherInfo.nm = type.getName();
				otherInfo.args = list;
				otherInfo.ct = "index";
				schema.others.add(otherInfo);
			}
		}
	}
	
	private boolean isBuiltInType(DType type) {
		for(BuiltInTypes bintype: BuiltInTypes.values()) {
			if (type.getName().equals(bintype.name())) {
				return true;
			}
		}
		return false;
	}
	
	private SxFieldInfo genField(DStructType dtype, TypePair pair) {
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
		
		SxFieldInfo fieldInfo = new SxFieldInfo();
		fieldInfo.f = pair.name;
		fieldInfo.flgs = flags;
		fieldInfo.t = getTypeAsString(pair);
		fieldInfo.sz = calcSizeofStr(dtype, pair);
		fieldInfo.datId = datId;
		return fieldInfo;
	}
	private int calcSizeofStr(DStructType dtype, TypePair pair) {
		int n = DRuleHelper.getSizeofField(dtype, pair.name);
		if (n != 0) {
			return n;
		}
		return 0;
	}

	private String getTypeAsString(TypePair pair) {
		try {
			BuiltInTypes fieldType = BuiltInTypes.valueOf(pair.type.getName());
			return BuiltInTypes.getDeliaTypeName(fieldType);
		} catch (Exception e) {
		}
		return pair.type.getName();
	}
}