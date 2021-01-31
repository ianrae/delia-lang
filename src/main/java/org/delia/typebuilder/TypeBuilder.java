package org.delia.typebuilder;

import java.util.ArrayList;
import java.util.List;

import org.delia.compiler.ast.StructFieldExp;
import org.delia.compiler.ast.TypeStatementExp;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.error.ErrorTracker;
import org.delia.rule.RuleBuilder;
import org.delia.type.BuiltInTypes;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.OrderedMap;
import org.delia.type.PrimaryKey;
import org.delia.type.Shape;
import org.delia.type.TypePair;

public class TypeBuilder extends ServiceBase {

	private DTypeRegistry registry;
	private RuleBuilder ruleBuilder;
	private PreTypeRegistry preRegistry;

	public TypeBuilder(FactoryService factorySvc, DTypeRegistry registry, PreTypeRegistry preRegistry) {
		super(factorySvc);
		this.registry = registry;
		this.ruleBuilder = new RuleBuilder(factorySvc, registry);
		this.preRegistry = preRegistry;
	}
	
	public ErrorTracker getErrorTracker() {
		return et;
	}
	
	public DType createType(TypeStatementExp typeStatementExp) {
		et.clear();
		if (typeStatementExp.structExp == null ) {
			return createScalarType(typeStatementExp);
		}
		
		//first detect any int with sizeof(64) (need to be upgraded to long)
		List<String> upgradeFieldL = ruleBuilder.findSizeofUpgrades(typeStatementExp);
		
		//build struct type
		OrderedMap omap = new OrderedMap();
		for(StructFieldExp fieldExp: typeStatementExp.structExp.argL) {
			String fieldName = fieldExp.getFieldName();
			DType fieldType = getTypeForField(fieldExp);
			if (fieldType.isShape(Shape.INTEGER)) {
				if (upgradeFieldL.contains(fieldName)) {
					//TODO: this probably break inheritance since we're making the child of an int into a long. fix lateer
					fieldType = registry.getType(BuiltInTypes.LONG_SHAPE);
					System.out.println("llllllllllllllllllllll " + fieldName);
				}
			}
			omap.add(fieldName, fieldType, fieldExp.isOptional, fieldExp.isUnique, fieldExp.isPrimaryKey, fieldExp.isSerial);
		}
		
		DType baseType = null;
		if (! typeStatementExp.baseTypeName.equals("struct")) {
			baseType = registry.getType(typeStatementExp.baseTypeName);
			if (baseType == null) {
				String msg = String.format("can't find base type '%s' for type '%s'", typeStatementExp.baseTypeName, typeStatementExp.typeName);
				FutureDeclError future = new FutureDeclError("uknown-base-type", msg);
				future.baseTypeName = typeStatementExp.baseTypeName;
				et.addNoLog(future);
//				return null;
			}
		}
		
		DStructType dtype = findOrCreateType(typeStatementExp.typeName, baseType, omap); 
		dtype.getRawRules().clear(); //reset
		addRules(dtype, typeStatementExp);
		ruleBuilder.addRelationRules(dtype, typeStatementExp);
		registry.add(typeStatementExp.typeName, dtype);
		
		if (et.errorCount() > 0) {
			return null;
		}
		return dtype;
	}

	/**
	 * Due to forward-ref re-execute, a struct type may already exist.
	 * We want each type to only have one DStructType instance.
	 * @param typeName type name
	 * @return DType 
	 */
	private DStructType findOrCreateType(String typeName, DType baseType, OrderedMap omap) {
		DType dtype = preRegistry.getType(typeName);
		
		List<TypePair> possibleL = new ArrayList<>();
		TypePair pair = baseType == null ? null : findPrimaryKeyFieldPair(baseType);
		if (pair != null) {
			possibleL.add(pair);
		}
		
		for(String fieldName: omap.orderedList) {
			if (omap.isPrimaryKey(fieldName)) {
				pair = new TypePair(fieldName, omap.map.get(fieldName));
				possibleL.add(pair);
			}
		}
		
		//if haven't found anything, we'll consider unique fields
		if (possibleL.isEmpty()) {
			for(String fieldName: omap.orderedList) {
				if (omap.isUnique(fieldName)) {
					pair = new TypePair(fieldName, omap.map.get(fieldName));
					possibleL.add(pair);
				}
			}
		}
		
		PrimaryKey prikey;
		if (possibleL.isEmpty()) {
			prikey = null;
		} else if (possibleL.size() == 1) {
			prikey = new PrimaryKey(possibleL.get(0));
		} else {
			prikey = new PrimaryKey(possibleL);
		}
		
		if (dtype == null) {
			return new DStructType(Shape.STRUCT, typeName, baseType, omap, prikey);
		} else {
			DStructType structType = (DStructType) dtype;
			structType.finishStructInitialization(baseType, omap, prikey);
			return structType;
		}
	}
	
	private static TypePair findPrimaryKeyFieldPair(DType inner) {
		if (! inner.isStructShape()) {
			return null;
		}
		
		//first, look for primaryKey fields
		DStructType dtype = (DStructType) inner;
		for(TypePair pair: dtype.getAllFields()) {
			if (dtype.fieldIsPrimaryKey(pair.name)) {
				return pair;
			}
		}
		
		//otherwise, look for unique fields
		for(TypePair pair: dtype.getAllFields()) {
			if (dtype.fieldIsUnique(pair.name) && !dtype.fieldIsOptional(pair.name)) {
				return pair;
			}
		}
		return null;
	}

	private DType createScalarType(TypeStatementExp typeStatementExp) {
		DType baseType = null;
		if (! BuiltInTypes.isBuiltInScalarType(typeStatementExp.baseTypeName)) {
			baseType = registry.getType(typeStatementExp.baseTypeName);
			if (baseType == null) {
				String msg = String.format("can't find base type '%s' for scalar type '%s'", typeStatementExp.baseTypeName, typeStatementExp.typeName);
				FutureDeclError future = new FutureDeclError("uknown-scalar-base-type", msg);
				future.baseTypeName = typeStatementExp.baseTypeName;
				et.addNoLog(future);
				return null;
			}
		} else {
			BuiltInTypes builtInType = BuiltInTypes.fromDeliaTypeName(typeStatementExp.baseTypeName);
			baseType = registry.getType(builtInType);
		}

		DType dtype = preRegistry.getType(typeStatementExp.typeName);
		dtype.finishScalarInitialization(baseType.getShape(), typeStatementExp.typeName, baseType);
		addRules(dtype, typeStatementExp);
		registry.add(typeStatementExp.typeName, dtype);
		return dtype;
	}

	private void addRules(DType dtype, TypeStatementExp typeStatementExp) {
		ruleBuilder.addRules(dtype, typeStatementExp);
	}

	private DType getTypeForField(StructFieldExp fieldExp) {
		DType strType = registry.getType(BuiltInTypes.STRING_SHAPE);
		DType intType = registry.getType(BuiltInTypes.INTEGER_SHAPE);
		DType longType = registry.getType(BuiltInTypes.LONG_SHAPE);
		DType numberType = registry.getType(BuiltInTypes.NUMBER_SHAPE);
		DType boolType = registry.getType(BuiltInTypes.BOOLEAN_SHAPE);
		DType dateType = registry.getType(BuiltInTypes.DATE_SHAPE);
		DType blobType = registry.getType(BuiltInTypes.BLOB_SHAPE);
		
		String s = fieldExp.typeName;
		if (s.equals("string")) {
			return strType;
		} else if (s.equals("int")) {
			return intType;
		} else if (s.equals("boolean")) {
			return boolType;
		} else if (s.equals("long")) {
			return longType;
		} else if (s.equals("number")) {
			return numberType;
		} else if (s.equals("date")) {
			return dateType;
		} else if (s.equals("blob")) {
			return blobType;
		} else {
			//this only works if subtypes defined _before_ they are used.
			DType possibleStruct = registry.getType(fieldExp.typeName);
			if (possibleStruct != null) {
				return possibleStruct;
			} else {
				possibleStruct = preRegistry.getType(fieldExp.typeName);
				if (possibleStruct != null) {
					return possibleStruct;
				}
				
				String msg = String.format("can't find field type '%s'.", fieldExp.typeName);
				FutureDeclError future = new FutureDeclError("uknown-field-type", msg);
				future.baseTypeName = fieldExp.typeName;
				et.addNoLog(future);
				return null;
			}
		}
	}

}
