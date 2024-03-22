package org.delia.hld;

import org.delia.compiler.ast.AST;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.error.DeliaError;
import org.delia.type.*;
import org.delia.typebuilder.MentionContext;
import org.delia.typebuilder.PreTypeRegistry;

import java.util.List;

public class NewTypePreRunner extends ServiceBase {
    private final String defaultSchema;
    private PreTypeRegistry preRegistry;
    private DTypeRegistry actualRegistry; //holds builtIn types only.
    private String currentSchema;

    public NewTypePreRunner(FactoryService factorySvc, DTypeRegistry actualRegistry, String defaultSchema) {
        super(factorySvc);
        this.preRegistry = new PreTypeRegistry();
        this.actualRegistry = actualRegistry;
        this.defaultSchema = defaultSchema;
    }

    public void executeStatements(List<AST.StatementAst> extL, List<DeliaError> allErrors) {
        for (AST.StatementAst exp : extL) {
//			ResultValue res = executeStatement(exp);
            executeStatement(exp);
//			if (! res.ok) {
//				allErrors.addAll(res.errors);
//			}
        }

        //create errors for undefined types
        for (DTypeName typeName : preRegistry.getUndefinedTypes()) {
            MentionContext mention = preRegistry.getMap().get(typeName);
            String msg = String.format("Can't find definition of type '%s' mentioned in type '%s'", typeName, mention.parentType);
            DeliaError err = new DeliaError("undefined-type", msg);
            log.logError(err.toString());
            allErrors.add(err);
        }
    }

    private void executeStatement(AST.StatementAst exp) {
        if (exp instanceof AST.SchemaAst) {
            AST.SchemaAst ast = (AST.SchemaAst) exp;
            this.currentSchema = calcSchema(ast.schemaName);
        }
        if (exp instanceof AST.TypeAst) {
            AST.TypeAst ast = (AST.TypeAst) exp;
            DType dtype = createType(ast);
            preRegistry.addTypeDefinition(dtype);
        }
    }
    private String calcSchema(String schema) {
        if (defaultSchema != null && defaultSchema.equals(schema)) {
            return null;
        }
        return schema;
    }

    public DType createType(AST.TypeAst typeStatementExp) {
        //et.clear();  TODO: why??
        if (typeStatementExp.schemaName == null) {
            typeStatementExp.schemaName = currentSchema;
        }
        typeStatementExp.schemaName = calcSchema(typeStatementExp.schemaName);

        if (typeStatementExp.isScalarType) {
            DType dtype = new DTypeImpl(Shape.INTEGER, typeStatementExp.schemaName, typeStatementExp.typeName, null); //not correct values. will fix later
            return dtype;
        }

        //build struct type
        OrderedMap omap = new OrderedMap();
        for (AST.TypeFieldAst fieldExp : typeStatementExp.fields) {
            DTypeName dtypeName = new DTypeName(typeStatementExp.schemaName, typeStatementExp.typeName);
            DType fieldType = getTypeForField(fieldExp, dtypeName);
//			omap.add(fieldExp.name, fieldType, fieldExp.isOptional, fieldExp.isUnique, fieldExp.isPrimaryKey, fieldExp.isSerial);
            DValue defaultVal = null;
            omap.add(fieldExp.fieldName, fieldType, fieldExp.isOptional, fieldExp.isUnique, fieldExp.isPrimaryKey, fieldExp.isSerial, defaultVal);
        }

        DType dtype = new DStructTypeImpl(Shape.STRUCT, typeStatementExp.schemaName, typeStatementExp.typeName, null, omap, null);
        return dtype;
    }

    private DType getTypeForField(AST.TypeFieldAst fieldExp, DTypeName parentTypeName) {
        DType strType = actualRegistry.getType(BuiltInTypes.STRING_SHAPE);
        DType intType = actualRegistry.getType(BuiltInTypes.INTEGER_SHAPE);
//        DType longType = actualRegistry.getType(BuiltInTypes.LONG_SHAPE);
        DType numberType = actualRegistry.getType(BuiltInTypes.NUMBER_SHAPE);
        DType boolType = actualRegistry.getType(BuiltInTypes.BOOLEAN_SHAPE);
        DType dateType = actualRegistry.getType(BuiltInTypes.DATE_SHAPE);
        DType blobType = actualRegistry.getType(BuiltInTypes.BLOB_SHAPE);

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
            if (fieldExp.schemaName == null) {
                fieldExp.schemaName = currentSchema;
            }
            DTypeName dtypeName = new DTypeName(fieldExp.schemaName, fieldExp.typeName);
            DType possibleStruct = preRegistry.getType(dtypeName);
            if (possibleStruct != null) {
                return possibleStruct;
            } else {
                DType dtype = new DStructTypeImpl(Shape.STRUCT, fieldExp.schemaName, fieldExp.typeName, null, null, null);
                preRegistry.addMentionedType(dtype, parentTypeName);
                return dtype;
            }
        }
    }

    public PreTypeRegistry getPreRegistry() {
        return preRegistry;
    }

//    public DTypeRegistry getActualRegistry() {
//        return actualRegistry;
//    }
}