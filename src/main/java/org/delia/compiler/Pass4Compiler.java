package org.delia.compiler;

import org.delia.compiler.ast.AST;
import org.delia.compiler.ast.Exp;
import org.delia.core.FactoryService;
import org.delia.db.DBType;
import org.delia.error.DeliaError;
import org.delia.error.ErrorFormatter;
import org.delia.hld.HLDFirstPassResults;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeName;
import org.delia.type.TypePair;
import org.delia.util.DValueHelper;

import java.util.ArrayList;
import java.util.List;

public class Pass4Compiler extends CompilerPassBase {

    private final Pass4RuleChecker ruleChecker;

    public Pass4Compiler(FactoryService factorySvc, HLDFirstPassResults firstPassResults, DBType dbType, ErrorFormatter errorFormatter) {
        super(factorySvc, firstPassResults, dbType, errorFormatter);
        this.ruleChecker = new Pass4RuleChecker(factorySvc, firstPassResults, dbType, registry, errorFormatter);
    }

    public CompilerPassResults process(AST.DeliaScript script) {
        CompilerPassResults results = new CompilerPassResults();
//        results.list = list;

        for (AST.StatementAst statement : script.statements) {
            if (statement instanceof AST.SchemaAst) {
                AST.SchemaAst exp = (AST.SchemaAst) statement;
            } else if (statement instanceof AST.TypeAst) {
                AST.TypeAst typeExp = (AST.TypeAst) statement;
                checkPrimaryKeys(typeExp, results);
                checkInheritedFields(typeExp, results);
                chkRules(typeExp, results);
            } else if (statement instanceof AST.InsertStatementAst) {
                AST.InsertStatementAst insExp = (AST.InsertStatementAst) statement;
//                chkAssocCrudInsert(insExp, results);
                chkSerialInsert(insExp, results);
//            } else if (exp instanceof UpdateStatementExp) {
//                UpdateStatementExp insExp = (UpdateStatementExp) exp;
//                chkAssocCrudUpdate(insExp, results);
//            } else if (exp instanceof InputFunctionDefStatementExp) {
//                InputFunctionDefStatementExp funcExp = (InputFunctionDefStatementExp) exp;
//                chkInputFunc(funcExp, results);
            } else if (statement instanceof AST.LetStatementAst) {
                checkLet((AST.LetStatementAst) statement, results);
            }
        }
        return results;
    }

    private DTypeName createDTypename(String schema, String typeName) {
        return new DTypeName(schema, typeName);
    }

    private void chkRules(AST.TypeAst typeExp, CompilerPassResults results) {
        ruleChecker.checkType(typeExp, results);
    }

    private void chkSerialInsert(AST.InsertStatementAst insExp, CompilerPassResults results) {
        DTypeName dtypeName = createDTypename(insExp.schemaName, insExp.typeName);
        DType type = registry.getType(dtypeName);
        DStructType structType = (DStructType) type;
        for (AST.InsertFieldStatementAst field : insExp.fields) {
            if (structType.fieldIsSerial(field.fieldName)) {
                results.addError("serial-value-cannot-be-provided", String.format("Type '%s': serial field '%s' must not have a value specified", structType.getName(), field.fieldName), insExp, et);
            }
        }
    }

    private void checkInheritedFields(AST.TypeAst typeExp, CompilerPassResults results) {
        DTypeName dtypeName = createDTypename(typeExp.schemaName, typeExp.typeName);
        DType type = registry.getType(dtypeName);
        if (!type.isStructShape()) {
            return;
        }
        DStructType structType = (DStructType) type;
        if (structType.getBaseType() != null) {
            DStructType baseType = (DStructType) structType.getBaseType();
            for (TypePair pair : baseType.getAllFields()) {
                if (structType.getDeclaredFields().containsKey(pair.name)) {
                    String msg = String.format("field '%s' already defined in type '%s' or its base type(s)", pair.name, typeExp.typeName);
                    DeliaError err = createError("type-field-already-defined-in-base-type", msg, typeExp);
                    results.errors.add(err);
                }
            }
        }

        for (String fieldName : structType.getDeclaredFields().keySet()) {
            checkModifiers(structType, fieldName, results, typeExp);
        }

    }

    private void checkModifiers(DStructType structType, String fieldName, CompilerPassResults results, AST.TypeAst typeExp) {
        boolean isOptional = structType.fieldIsOptional(fieldName);
        boolean isPrimaryKey = structType.fieldIsPrimaryKey(fieldName);
        boolean isSerial = structType.fieldIsSerial(fieldName);
        boolean isUnique = structType.fieldIsUnique(fieldName);

        if (isOptional && isPrimaryKey) {
            String msg = String.format("optional and primaryKey cannot be used together - field '%s'", fieldName);
            addError(results, "optional-primarykey-not-allowed", msg, typeExp);
        }
        if (isOptional && isSerial) {
            String msg = String.format("optional and serial cannot be used together - field '%s'", fieldName);
            addError(results, "optional-serial-not-allowed", msg, typeExp);
        }

        if (isUnique && isPrimaryKey) {
            String msg = String.format("unique and primaryKey cannot be used together - field '%s'", fieldName);
            addError(results, "unique-primarykey-not-allowed", msg, typeExp);
        }
    }

    private void checkPrimaryKeys(AST.TypeAst typeExp, CompilerPassResults results) {
        DTypeName dtypeName = createDTypename(typeExp.schemaName, typeExp.typeName);
        DType type = registry.getType(dtypeName);
        if (!type.isStructShape()) {
            return;
        }
        DStructType structType = (DStructType) type;
        TypePair pair = DValueHelper.findPrimaryKeyFieldPair(structType);
        if (pair == null) {
            return;
        }

        if (!keyFieldIsAllowedType(pair)) {
            String msg = String.format("type '%s': primary key type %s is not allowed in field '%s'", typeExp.typeName, pair.type.getName(), pair.name);
            DeliaError err = createError("primary-key-type-not-allowed", msg, typeExp);
            results.errors.add(err);
        }
        if (!serialFieldIsAllowedType(pair, structType)) {
            String msg = String.format("type '%s': serial %s is not allowed in field '%s'", typeExp.typeName, pair.type.getName(), pair.name);
            DeliaError err = createError("primary-key-type-not-allowed", msg, typeExp);
            results.errors.add(err);
        }
    }

    private boolean keyFieldIsAllowedType(TypePair pair) {
        switch (pair.type.getShape()) {
            case INTEGER:
//            case LONG:
            case BOOLEAN:
            case STRING:
            case DATE:
                return true;
            case NUMBER:
            case STRUCT:
            case RELATION:
            default:
                return false;
        }
    }

    private boolean serialFieldIsAllowedType(TypePair pair, DStructType structType) {
        if (!structType.fieldIsSerial(pair.name)) {
            return true;
        }

        switch (pair.type.getShape()) {
            case INTEGER:
//            case LONG:
                return true;
            case STRING:
            case DATE:
            case BOOLEAN:
            case NUMBER:
            case STRUCT:
            case RELATION:
            default:
                return false;
        }
    }

    private void checkLet(AST.LetStatementAst statement, CompilerPassResults results) {
        if (statement.whereClause != null) {
            MyNullFinderVisitor visitor = new MyNullFinderVisitor();
            statement.whereClause.visit(visitor);
            if (visitor.expCount == 2 && visitor.allFields.size() > 0) {
                results.addError("null-filter-not-allowed", "[null] is not allowed", statement, et);
            }
        }
    }

    private static class MyNullFinderVisitor implements Exp.ExpVisitor {
        public List<Exp.NullExp> allFields = new ArrayList<>();
        public int expCount;

        @Override
        public void visit(Exp.ExpBase exp) {
            expCount++;
            if (exp instanceof Exp.NullExp) {
                this.allFields.add((Exp.NullExp) exp);
            }
        }
    }


}
