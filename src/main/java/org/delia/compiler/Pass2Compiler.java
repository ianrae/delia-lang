package org.delia.compiler;

import org.apache.commons.lang3.StringUtils;
import org.delia.compiler.ast.AST;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.ExpFieldHelper;
import org.delia.core.ConfigureService;
import org.delia.core.ConfigureServiceImpl;
import org.delia.core.FactoryService;
import org.delia.db.DBType;
import org.delia.dval.TypeDetector;
import org.delia.error.DeliaError;
import org.delia.error.ErrorFormatter;
import org.delia.hld.CrudAction;
import org.delia.hld.HLDFirstPassResults;
import org.delia.type.*;
import org.delia.util.DValueHelper;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Enforce language rules that parser can't enforce.
 * Types are not yet available.
 *
 * @author ian
 */
public class Pass2Compiler extends CompilerPassBase {

    private List<String> syntheticIds = new ArrayList<>();

    public Pass2Compiler(FactoryService factorySvc, HLDFirstPassResults firstPassResults, DBType dbType, ErrorFormatter errorFormatter) {
        super(factorySvc, firstPassResults, dbType, errorFormatter);
    }

    public CompilerPassResults process(AST.DeliaScript script, String currentSchema) {
        CompilerPassResults results = new CompilerPassResults();
//		results.list = list;

        for (AST.StatementAst statement : script.statements) {
            if (statement instanceof AST.SchemaAst) {
                AST.SchemaAst exp = (AST.SchemaAst) statement;
            } else if (statement instanceof AST.TypeAst) {
                AST.TypeAst typeExp = (AST.TypeAst) statement;
                checkTypeStatement(results, typeExp);
                //pretyperegistry already set schema
            } else if (statement instanceof AST.LetStatementAst) {
                AST.LetStatementAst letAST = (AST.LetStatementAst) statement;
                checkLetStatement(results, letAST);
            } else if (statement instanceof AST.ConfigureStatementAst) {
                AST.ConfigureStatementAst configAST = (AST.ConfigureStatementAst) statement;
                checkConfigureStatement(results, configAST);
			} else if (statement instanceof AST.UpdateStatementAst) {
                AST.UpdateStatementAst upExp = (AST.UpdateStatementAst) statement;
				checkUpdateStatement(results, upExp);
            } else if (statement instanceof AST.UpsertStatementAst) {
                AST.UpsertStatementAst upExp = (AST.UpsertStatementAst) statement;
                checkUpsertStatement(results, upExp);
            } else if (statement instanceof AST.DeleteStatementAst) {
                AST.DeleteStatementAst delAST = (AST.DeleteStatementAst) statement;
                checkDeleteStatement(results, delAST);
            } else if (statement instanceof AST.InsertStatementAst) {
                AST.InsertStatementAst insAST = (AST.InsertStatementAst) statement;
                checkInsertStatement(results, insAST);
            }
        }
        return results;
    }

    private DTypeName createDTypename(String schema, String typeName) {
        return new DTypeName(schema, typeName);
    }

    private void checkUpdateStatement(CompilerPassResults results, AST.UpdateStatementAst statement) {
        DTypeName dtypeName = createDTypename(statement.schemaName, statement.typeName);
        DStructType structType = (DStructType) registry.getType(dtypeName);
        if (structType == null) {
            DeliaError err = et.add("type.not.found", String.format("update '%s': unknown type '%s'", statement.typeName, statement.typeName));
            results.addError(err, statement);
            return;
        }
        chkRelationCrud(statement.fields, structType, true, "update", results);
        chkEmptyType(structType, "update", results, statement.loc);
    }

    private void chkRelationCrud(List<AST.InsertFieldStatementAst> fields, DStructType structType, boolean allowed, String statementName, CompilerPassResults results) {
        for (AST.InsertFieldStatementAst field : fields) {
            if (field.crudAction != null) {
                if (!allowed) {
                    String msg = String.format("%s '%s': action '%s' not allowed in %s statement'", statementName, structType.getName(), field.crudAction, statementName);
                    results.addError("crud-action-not-allowed", msg, field, et);
                } else {
                    DType fieldType = DValueHelper.findFieldType(structType, field.fieldName);
                    if (!fieldType.isStructShape()) {
                        String msg = String.format("%s '%s': field '%s': action '%s' is only allowed on relation fields'",
                                statementName, structType.getName(), field.fieldName, field.crudAction);
                        results.addError("crud-action-not-allowed-on-scalar-fields", msg, field, et);
                    }
                }

                try {
                    CrudAction crudAction = CrudAction.valueOf(field.crudAction.toUpperCase(Locale.ROOT));
                } catch (IllegalArgumentException e) {
                    String msg = String.format("%s '%s': field '%s': unknown action '%s' '",
                            statementName, structType.getName(), field.fieldName, field.crudAction);
                    results.addError("crud-action-unknown", msg, field, et);
                }

                if (field.crudAction.equals("update")) {
                    if (field.listExp == null) {
                        String msg = String.format("%s '%s': field '%s': update action requires a list of values'",
                                statementName, structType.getName(), field.fieldName);
                        results.addError("crud-action-update-not-list", msg, field, et);
                    } else if (field.listExp.listL.size() % 2 != 0) {
                        String msg = String.format("%s '%s': field '%s': update action requires a list of values with an even number of elements'",
                                statementName, structType.getName(), field.fieldName);
                        results.addError("crud-action-update-wrong-list-size", msg, field, et);
                    }
                }
            }
        }
    }

    private void chkEmptyType(DStructType structType, String statementName, CompilerPassResults results, AST.Loc loc) {
        if (structType.getAllFields().isEmpty()) {
            String msg = String.format("%s '%s': can't insert empty type'", statementName, structType.getName());
            String errId = String.format("cant-%s-empty-type", statementName);
            results.addError(errId, msg, null, et).setLoc(loc);
        }
    }

    private void checkUpsertStatement(CompilerPassResults results, AST.UpsertStatementAst statement) {
        DTypeName dtypeName = createDTypename(statement.schemaName, statement.typeName);
        DStructType structType = (DStructType) registry.getType(dtypeName);
        if (structType == null) {
            results.addError("type.not.found", String.format("upsert '%s': unknown type '%s'", statement.typeName, statement.typeName), statement, et);
            return;
        }
        chkRelationCrud(statement.fields, structType, false, "upsert", results);
        chkEmptyType(structType, "upsert", results, statement.loc);
    }

    private void checkDeleteStatement(CompilerPassResults results, AST.DeleteStatementAst statement) {
        DTypeName dtypeName = createDTypename(statement.schemaName, statement.typeName);
        DStructType structType = (DStructType) registry.getType(dtypeName);
        if (structType == null) {
            results.addError("type.not.found", String.format("delete '%s': unknown type '%s'", statement.typeName, statement.typeName), statement, et);
            return;
        }
    }

    private void checkInsertStatement(CompilerPassResults results, AST.InsertStatementAst statement) {
        DTypeName dtypeName = createDTypename(statement.schemaName, statement.typeName);
        DStructType structType = (DStructType) registry.getType(dtypeName);
        if (structType == null) {
            results.addError("type.not.found", String.format("insert '%s': unknown type '%s'", statement.typeName, statement.typeName), statement, et);
            return;
        }
        chkRelationCrud(statement.fields, structType, false, "insert", results);
        chkEmptyType(structType, "insert", results, statement.loc);

        List<TypePair> allFields = structType.getAllFields();
        for (AST.InsertFieldStatementAst field : statement.fields) {
            Optional<TypePair> pair = allFields.stream().filter(f -> f.name.equals(field.fieldName)).findAny();
            boolean exists = pair.isPresent();
            if (!exists) {
                if (syntheticIds.contains(field.fieldName)) {
                    //do nothing. may be valid synthetic id
                } else {
                    String msg = String.format("insert '%s'. This type does not have a field named '%s'", structType.getName(), field.fieldName);
                    addError(results, "unknown-field", msg, field);
                }
            } else {
                if (field.varExp != null) {
                    //we'll check in another compiler pass
                } else if (field.listExp != null) {
                    for (Exp.ElementExp ff : field.listExp.listL) {
                        Exp.ValueExp vexp = (Exp.ValueExp) ff;
                        DType valueType = vexp.value.getType();
                        chkInsertField(results, field, valueType, pair, structType);
                    }
                } else if (field.valueExp.value != null) {
                    DType valueType = field.valueExp.value.getType();
                    chkInsertField(results, field, valueType, pair, structType);
                }

            }
        }
    }

    private void chkInsertField(CompilerPassResults results, AST.InsertFieldStatementAst field, DType valueType, Optional<TypePair> pair, DStructType structType) {
        DType dtype = pair.get().type;
        if (dtype.isStructShape()) {
            TypePair pkpair = DValueHelper.findPrimaryKeyFieldPair(dtype);
            dtype = pkpair.type;
        }
        if (!dtype.getName().equals(valueType.getName())) {
            if (typeIsCompatible(dtype, valueType)) {
                //we will promote when do insert
            } else {
                String msg = String.format("insert '%s': field '%s' has type '%s' but type '%s' encountered", structType.getName(),
                        field.fieldName, dtype.getName(), valueType.getName());
                addError(results, "insert-wrong-field-type", msg, field);
            }
        }
    }

    private boolean typeIsCompatible(DType dtype, DType valueType) {
        if (dtype.getShape().equals(valueType.getShape())) {
            return true;
        }
        switch (dtype.getShape()) {
            case INTEGER:
//                return isOneOf(valueType, Shape.LONG, Shape.NUMBER);
                return isOneOf(valueType, Shape.NUMBER);
//            case LONG:
//                return isOneOf(valueType, Shape.INTEGER, Shape.NUMBER);
            case NUMBER:
//                return isOneOf(valueType, Shape.INTEGER, Shape.LONG);
                return isOneOf(valueType, Shape.INTEGER);
            case DATE:
//                return isOneOf(valueType, Shape.STRING, Shape.LONG); //TODO: is this correct? that you can use long for date
                return isOneOf(valueType, Shape.STRING); //TODO: is this correct? that you can use long for date
            case BLOB:
                return isOneOf(valueType, Shape.STRING);
            case STRING:
            case BOOLEAN:
            case STRUCT:
            case RELATION:
            default:
                return false;
        }
    }

    private boolean isOneOf(DType valueType, Shape... shapes) {
        for (Shape shape : shapes) {
            if (valueType.getShape().equals(shape)) {
                return true;
            }
        }
        return false;
    }

    private boolean isAStruct(AST.LetStatementAst statement) {
        if (statement.typeName == null) return false; //eg let x = 5

        if (BuiltInTypes.fromDeliaTypeName(statement.typeName) != null) {
            return false;
        }

        DTypeName dtypeName = createDTypename(statement.schemaName, statement.typeName);
        DType dd = registry.getType(dtypeName);
        return (dd == null || dd.isStructShape()); //return true if null
    }
    private void checkLetStatement(CompilerPassResults results, AST.LetStatementAst statement) {
        if (! isAStruct(statement)) return;

        DTypeName dtypeName = createDTypename(statement.schemaName, statement.typeName);
        DStructType structType = (DStructType) registry.getType(dtypeName);
        if (structType == null) {
            results.addError("type.not.found", String.format("let '%s': unknown type '%s'", statement.typeName, statement.typeName), statement, et);
            return;
        }

        List<String> allFuncs = Arrays.asList("orderBy", "distinct", "fks", "count", "exists", "first", "last", "ith",
                "min", "max", "limit", "offset", "fetch");
        for (Exp.FunctionExp func : ExpFieldHelper.extractFuncs(statement.fieldAndFuncs)) {
            String funcName = func.fnName;
            if (funcName.contains(".")) {
                funcName = StringUtils.substringAfterLast(func.fnName, ".");
            }
            if (!allFuncs.contains(funcName)) {
                String msg = String.format("let '%s': unknown function: '%s'", structType.getName(), funcName);
                addError(results, "unknown-field", msg, statement);
            }
        }

        chkFieldIntArg(results, statement, structType, "ith");
        chkFieldIntArg(results, statement, structType, "limit");
        chkFieldIntArg(results, statement, structType, "offset");
        chkFieldArgsExist(results, statement, "orderBy", structType);
        chkFieldArgsExist(results, statement, "fetch", structType);

        //TODO need something like scopes list to check all fields. .addr.country.id. Error if field not exist
    }

    private void chkFieldIntArg(CompilerPassResults results, AST.LetStatementAst statement, DStructType structType, String fnName) {
        Exp.FunctionExp ithFUnc = ExpFieldHelper.findFunc(statement.fieldAndFuncs, fnName);
        if (ithFUnc != null) {
            if (ithFUnc.argsL.isEmpty() || ithFUnc.argsL.size() > 1) {
                String msg = String.format("let '%s': %s() must have a single int arg, such as %s(0)", structType.getName(), fnName, fnName);
                String errId = String.format("%s-invalid-index", fnName);
                addError(results, errId, msg, statement);
            } else {
                String str = ithFUnc.argsL.get(0).strValue();
                if (!TypeDetector.isIntValue(str)) {
                    String msg = String.format("let '%s': %s(%s) does not have an integer argument, such as %s(0)", structType.getName(), fnName, str, fnName);
                    String errId = String.format("%s-invalid-index", fnName);
                    addError(results, errId, msg, statement);
                }
            }

        }

    }

    private void chkFieldArgsExist(CompilerPassResults results, AST.LetStatementAst statement, String funcName, DStructType structType) {
        Exp.FunctionExp func = ExpFieldHelper.findFunc(statement.fieldAndFuncs, funcName);
        if (func != null) {
            List<Exp.ValueExp> fieldArgs = func.argsL.stream().filter(x -> x instanceof Exp.ValueExp)
                    .map(x -> (Exp.ValueExp) x).collect(Collectors.toList());

            for (Exp.ValueExp vexp : fieldArgs) {
                String fieldName = vexp.strValue();
                TypePair pair = DValueHelper.findField(structType, fieldName);
                if (pair == null) {
                    String msg = String.format("let '%s': %s contains unknown field '%s'", structType.getName(), funcName, fieldName);
                    addError(results, "unknown-field", msg, statement);
                }
            }
        }
    }


//	private void checkUpsertStatement(CompilerResults results, UpsertStatementExp upExp) {
//		OptionExp optionExp = upExp.optionExp;
//		if (optionExp != null && !optionExp.strValue().equals("noUpdate")) {
//			String msg = String.format("upsert: unknown option '%s'", optionExp.strValue());
//			addError(results, "configure-unknown-var", msg, upExp);
//		}
//	}
//
    private void checkConfigureStatement(CompilerPassResults results, AST.ConfigureStatementAst statement) {
		log.log("config %s:%s", statement.configName, statement.scalarElem.strValue());

		ConfigureService configSvc = factorySvc.getConfigureService();
		boolean b = configSvc.validate(statement.configName);
		if (! b) {
			String msg = String.format("configure: unknown variable '%s'", statement.configName);
			addError(results, "configure-unknown-var", msg, statement);
		} else {
		    //we are only keeping an approx track of synthetic-ids. not tracking types (do that at runtime)
            if (statement.configName.endsWith(ConfigureServiceImpl.SYNTHETIC_IDS_TARGET)) {
                String sidName = statement.scalarElem.strValue();
                syntheticIds.add(sidName);
            }
        }
	}

    private void checkTypeStatement(CompilerPassResults results, AST.TypeAst typeExp) {
//		log.log("type " + typeExp.strValue());
        checkTypeName(typeExp, results);
        if (!typeExp.isScalarType) {
            Map<String,String> fieldMap = new HashMap<>();
            for (AST.TypeFieldAst sfe : typeExp.fields) {
                checkModifiers(sfe, results);

                if (fieldMap.containsKey(sfe.fieldName)) {
                    String msg = String.format("field '%s' appears more than once in type '%s'", sfe.fieldName, typeExp.typeName);
                    addError(results, "duplicate-field-name", msg, sfe);
                }
                fieldMap.put(sfe.fieldName, "");

                if (isScalar(sfe)) {
                    if (sfe.isRelation) {
                        String msg = String.format("relation cannot be used with scalar types - field '%s'", sfe.fieldName);
                        addError(results, "relation-wrong-type", msg, sfe);
                    } else if (sfe.isMany) {
                        String msg = String.format("'many' can only be used with relations - field '%s'", sfe.fieldName);
                        addError(results, "many-error", msg, sfe);
                    } else if (sfe.isOne) {
                        String msg = String.format("'one' can only be used with relations - field '%s'", sfe.fieldName);
                        addError(results, "many-error", msg, sfe);
                    }
                } else {
                    if (sfe.isMany && sfe.isOne) {
                        String msg = String.format("cannot use 'one' and 'many' on same field - field '%s'", sfe.fieldName);
                        addError(results, "one-and-many-error", msg, sfe);
                    }
                    if (sfe.isSerial) {
                        String msg = String.format("cannot use 'serial' on relation fields - field '%s'", sfe.fieldName);
                        addError(results, "serial-error", msg, sfe);
                    }
                    if (sfe.defaultVal != null) {
                        String msg = String.format("cannot use 'default' on relation fields - field '%s'", sfe.fieldName);
                        addError(results, "default-error", msg, sfe);
                    }
                }
            }
        }
    }

    private void checkTypeName(AST.TypeAst typeExp, CompilerPassResults results) {
        if (BuiltInTypes.isBuiltInScalarType(typeExp.typeName)) {
            String msg = String.format("Type name already used - type'%s'", typeExp.typeName);
            addError(results, "typename.in.use", msg, typeExp);
        }
    }

    private void checkModifiers(AST.TypeFieldAst qfe, CompilerPassResults results) {
        if (qfe.isOptional && qfe.isPrimaryKey) {
            String msg = String.format("optional and primaryKey cannot be used together - field '%s'", qfe.fieldName);
            addError(results, "optional-primarykey-not-allowed", msg, qfe);
        }
        if (qfe.isOptional && qfe.isSerial) {
            String msg = String.format("optional and serial cannot be used together - field '%s'", qfe.fieldName);
            addError(results, "optional-serial-not-allowed", msg, qfe);
        }

        if (qfe.isUnique && qfe.isPrimaryKey) {
            String msg = String.format("unique and primaryKey cannot be used together - field '%s'", qfe.fieldName);
            addError(results, "unique-primarykey-not-allowed", msg, qfe);
        }
        if (!qfe.isOptional && qfe.defaultVal != null) {
            String msg = String.format("default can only be used with optional fields - field '%s'", qfe.fieldName);
            addError(results, "default-non-optional-not-allowed", msg, qfe);
        }
    }

    private boolean isScalar(AST.TypeFieldAst qfe) {
        return BuiltInTypes.isBuiltInScalarType(qfe.typeName);
    }


//	private void checkLetStatement(CompilerResults results, LetStatementExp letExp) {
////		log.log("a" + letExp.strValue());
//		if (letExp.value instanceof QueryExp) {
//			QueryExp qexp = (QueryExp) letExp.value;
//
//			if (qexp.filter != null && qexp.filter.cond instanceof FilterOpExp) {
//				FilterOpExp fexp = (FilterOpExp) qexp.filter.cond;
//
//				OP op = OP.createFromString(fexp.op);
//				if (op != null) {
////					if (fexp.op1 instanceof NullExp) {
////						String msg = String.format("null not allowed with '%s'", qexp.filter.strValue());
////						DangError err = createError("null-not-allowed", msg, qexp.filter);
////						results.errors.add(err);
////					}
////					if (fexp.op2 instanceof NullExp) {
////						String msg = String.format("null not allowed with '%s'", qexp.filter.strValue());
////						DangError err = createError("null-not-allowed", msg, qexp.filter);
////						results.errors.add(err);
////					}
//				}
//			}
//		}
//	}
}