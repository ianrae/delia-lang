package org.delia.hld;


import org.delia.compiler.ast.AST;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.error.ErrorTracker;
import org.delia.type.*;
import org.delia.typebuilder.FutureDeclError;
import org.delia.typebuilder.PreTypeRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class NewTypeBuilder extends ServiceBase {

    static class PossiblePrimaryKeyInfo {
        TypePair pair;
        boolean isPrimaryKey;

        public PossiblePrimaryKeyInfo(TypePair pair, boolean isPrimaryKey) {
            this.pair = pair;
            this.isPrimaryKey = isPrimaryKey;
        }

        public PrimaryKey createPrimaryKey() {
            PrimaryKey prikey = new PrimaryKey(pair);
            return prikey;
        }
    }

    private DTypeRegistry registry;
    private NewRelationRuleBuilder ruleBuilder; //TODO: later add full rule builder
    private PreTypeRegistry preRegistry;

    public NewTypeBuilder(FactoryService factorySvc, DTypeRegistry registry, PreTypeRegistry preRegistry) {
        super(factorySvc);
        this.registry = registry;
        this.ruleBuilder = new NewRelationRuleBuilder(factorySvc, registry);
        this.preRegistry = preRegistry;
    }

    public ErrorTracker getErrorTracker() {
        return et;
    }

    public DType createType(AST.TypeAst typeStatementExp) {
        et.clear();
        if (typeStatementExp.isScalarType) {
            return createScalarType(typeStatementExp);
        }

        //first detect any int with sizeof(64) (need to be upgraded to long)
//		List<String> upgradeFieldL = ruleBuilder.findSizeofUpgrades(typeStatementExp);

        //build struct type
        OrderedMap omap = new OrderedMap();
        for (AST.TypeFieldAst fieldExp : typeStatementExp.fields) {
            String fieldName = fieldExp.fieldName;
            DType fieldType = getTypeForField(fieldExp, typeStatementExp.loc);
            if (fieldType.isShape(Shape.INTEGER)) {
//				if (upgradeFieldL.contains(fieldName)) {
//					//TODO: this probably break inheritance since we're making the child of an int into a long. fix lateer
//					fieldType = registry.getType(BuiltInTypes.LONG_SHAPE);
//				}
            } else if (fieldType.isShape(Shape.BLOB) && fieldExp.isUnique) {
                String msg = String.format("blob field '%s': cannot be unique, in type '%s'", fieldName, typeStatementExp.typeName);
                FutureDeclError future = new FutureDeclError("blob-unique-not-allowed", msg, typeStatementExp.loc);
                future.baseTypeName = typeStatementExp.baseName;
                et.addNoLog(future);
            }

            omap.add(fieldName, fieldType, fieldExp.isOptional, fieldExp.isUnique, fieldExp.isPrimaryKey, fieldExp.isSerial);
        }

        DType baseType = null;
        if (!typeStatementExp.baseName.equals("struct")) {
            DTypeName dtypeName = new DTypeName(typeStatementExp.baseSchemaName, typeStatementExp.baseName);
            baseType = registry.getType(dtypeName);
            if (baseType == null) {
                String msg = String.format("can't find base type '%s' for type '%s'", typeStatementExp.baseName, typeStatementExp.typeName);
                FutureDeclError future = new FutureDeclError("unknown-base-type", msg, typeStatementExp.loc);
                future.baseTypeName = typeStatementExp.baseName;
                et.addNoLog(future);
//				return null;
            }
        }

        DStructType dtype = findOrCreateType(typeStatementExp.schemaName, typeStatementExp.typeName, baseType, omap);
        dtype.getRawRules().clear(); //reset
//		addRules(dtype, typeStatementExp);
        ruleBuilder.addRelationRules(dtype, typeStatementExp);
        DTypeName typeName = new DTypeName(typeStatementExp.schemaName, typeStatementExp.typeName);
        registry.registerType(typeName, dtype);

        if (et.errorCount() > 0) {
            return null;
        }
        return dtype;
    }

    /**
     * Due to forward-ref re-execute, a struct type may already exist.
     * We want each type to only have one DStructType instance.
     *
     * @param typeName type name
     * @return DType
     */
    private DStructType findOrCreateType(String schema, String typeName, DType baseType, OrderedMap omap) {
        DTypeName dtypeName = new DTypeName(schema, typeName);
        DType dtype = preRegistry.getType(dtypeName);

        List<PossiblePrimaryKeyInfo> possibleL = new ArrayList<>();
        PossiblePrimaryKeyInfo priInfo = baseType == null ? null : findPrimaryKeyFieldPair(baseType);
        if (priInfo != null) {
            possibleL.add(priInfo);
        }

        for (String fieldName : omap.orderedList) {
            if (omap.isPrimaryKey(fieldName)) {
                TypePair pair = new TypePair(fieldName, omap.map.get(fieldName));
                possibleL.add(new PossiblePrimaryKeyInfo(pair, true));
            }
        }

        //if haven't found anything, we'll consider unique fields
        if (possibleL.isEmpty()) {
            for (String fieldName : omap.orderedList) {
                if (omap.isUnique(fieldName)) {
                    TypePair pair = new TypePair(fieldName, omap.map.get(fieldName));
                    possibleL.add(new PossiblePrimaryKeyInfo(pair, false));
                }
            }
        }

        PrimaryKey prikey;
        if (possibleL.isEmpty()) {
            prikey = null;
        } else if (possibleL.size() == 1) {
            prikey = possibleL.get(0).createPrimaryKey();
        } else {
            //jan2024: fix. if find multiple possible keys but only one is marked primaryKey  then use it alone.
            List<PossiblePrimaryKeyInfo> list = possibleL.stream().filter(x -> x.isPrimaryKey).collect(Collectors.toList());
            if (list.size() == 1) {
                prikey = list.get(0).createPrimaryKey();
            } else {
                List<TypePair> pairL = possibleL.stream().map(x -> x.pair).collect(Collectors.toList());
                prikey = new PrimaryKey(pairL);
            }
        }

        if (dtype == null) {
            return new DStructTypeImpl(Shape.STRUCT, schema, typeName, baseType, omap, prikey);
        } else {
            DStructType structType = (DStructType) dtype;
            DStructTypeInternal structTypeImpl = (DStructTypeInternal) dtype;
            structTypeImpl.finishStructInitialization(baseType, omap, prikey);
            return structType;
        }
    }

    private static PossiblePrimaryKeyInfo findPrimaryKeyFieldPair(DType inner) {
        if (!inner.isStructShape()) {
            return null;
        }

        //first, look for primaryKey fields
        DStructType dtype = (DStructType) inner;
        for (TypePair pair : dtype.getAllFields()) {
            if (dtype.fieldIsPrimaryKey(pair.name)) {
                return new PossiblePrimaryKeyInfo(pair, true);
            }
        }

        //otherwise, look for unique fields
        for (TypePair pair : dtype.getAllFields()) {
            if (dtype.fieldIsUnique(pair.name) && !dtype.fieldIsOptional(pair.name)) {
                return new PossiblePrimaryKeyInfo(pair, false);
            }
        }
        return null;
    }

    private DType createScalarType(AST.TypeAst typeStatementExp) {
        DType baseType = null;
        if (!BuiltInTypes.isBuiltInScalarType(typeStatementExp.baseName)) {
            DTypeName dtypeName = new DTypeName(typeStatementExp.baseSchemaName, typeStatementExp.baseName);
            baseType = registry.getType(dtypeName);
            if (baseType == null) {
                String msg = String.format("can't find base type '%s' for scalar type '%s'", typeStatementExp.baseName, typeStatementExp.typeName);
                FutureDeclError future = new FutureDeclError("unknown-scalar-base-type", msg, typeStatementExp.loc);
                future.baseTypeName = typeStatementExp.baseName;
                et.addNoLog(future);
                return null;
            }
        } else {
            BuiltInTypes builtInType = BuiltInTypes.fromDeliaTypeName(typeStatementExp.baseName);
            baseType = registry.getType(builtInType);
        }

        DTypeName dtypeName = new DTypeName(typeStatementExp.schemaName, typeStatementExp.typeName);
        DType dtype = preRegistry.getType(dtypeName);
        DTypeImpl dtypeimpl = (DTypeImpl) dtype;
        dtypeimpl.finishScalarInitialization(baseType.getShape(), typeStatementExp.typeName, baseType);
//		addRules(dtype, typeStatementExp);
        DTypeName typeName = new DTypeName(typeStatementExp.schemaName, typeStatementExp.typeName);
        registry.registerType(typeName, dtype);
        return dtype;
    }

//	private void addRules(DType dtype, TypeStatementExp typeStatementExp) {
//		ruleBuilder.addRules(dtype, typeStatementExp);
//	}

    private DType getTypeForField(AST.TypeFieldAst fieldExp, AST.Loc loc) {
        DType strType = registry.getType(BuiltInTypes.STRING_SHAPE);
        DType intType = registry.getType(BuiltInTypes.INTEGER_SHAPE);
//        DType longType = registry.getType(BuiltInTypes.LONG_SHAPE);
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
//        } else if (s.equals("long")) {
//            return longType;
        } else if (s.equals("number")) {
            return numberType;
        } else if (s.equals("date")) {
            return dateType;
        } else if (s.equals("blob")) {
            return blobType;
        } else {
            //this only works if subtypes defined _before_ they are used.
            DTypeName dtypeName = new DTypeName(fieldExp.schemaName, fieldExp.typeName);
            DType possibleStruct = registry.getType(dtypeName);
            if (possibleStruct != null) {
                return possibleStruct;
            } else {
                possibleStruct = preRegistry.getType(dtypeName);
                if (possibleStruct != null) {
                    return possibleStruct;
                }

                String msg = String.format("can't find field type '%s'.", fieldExp.typeName);
                FutureDeclError future = new FutureDeclError("unknown-field-type", msg, loc);
                future.baseTypeName = fieldExp.typeName;
                et.addNoLog(future);
                return null;
            }
        }
    }

}
