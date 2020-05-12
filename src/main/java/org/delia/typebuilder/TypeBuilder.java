package org.delia.typebuilder;

import java.util.ArrayList;
import java.util.List;

import org.delia.compiler.ast.StructFieldExp;
import org.delia.compiler.ast.TypeStatementExp;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.error.ErrorTracker;
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

	public TypeBuilder(FactoryService factorySvc, DTypeRegistry registry) {
		super(factorySvc);
		this.registry = registry;
		this.ruleBuilder = new RuleBuilder(factorySvc, registry);
	}
	
	public ErrorTracker getErrorTracker() {
		return et;
	}
	
	//TODO: later support custom scalar types such as type Person string {}...
	
	public DType createType(TypeStatementExp typeStatementExp) {
		et.clear();
		if (typeStatementExp.structExp == null ) {
			return createScalarType(typeStatementExp);
		}
		
		//build struct type
		OrderedMap omap = new OrderedMap();
		for(StructFieldExp fieldExp: typeStatementExp.structExp.argL) {
			DType fieldType = getTypeForField(fieldExp);
			omap.add(fieldExp.getFieldName(), fieldType, fieldExp.isOptional, fieldExp.isUnique, fieldExp.isPrimaryKey, fieldExp.isSerial);
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
		//now using type replacer
//	    DType possibleStruct = registry.getType(typeName);
//	    if (possibleStruct != null && possibleStruct.isStructShape()) {
//	      DStructType dtype =  (DStructType) possibleStruct;
//	      dtype.internalAdjustType(baseType, omap);
//	      return dtype;
//	    }
		
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
		return new DStructType(Shape.STRUCT, typeName, baseType, omap, prikey);
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
		
		
		DType dtype = new DType(baseType.getShape(), typeStatementExp.typeName, baseType);
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
		} else {
			//this only works if subtypes defined _before_ they are used.
			//TODO: support any order later.
			DType possibleStruct = registry.getType(fieldExp.typeName);
			if (possibleStruct != null) {
				return possibleStruct;
			} else {
				String msg = String.format("can't find field type '%s'.", fieldExp.typeName);
				FutureDeclError future = new FutureDeclError("uknown-field-type", msg);
				future.baseTypeName = fieldExp.typeName;
				et.addNoLog(future);
				return null;
			}
		}
	}

}
