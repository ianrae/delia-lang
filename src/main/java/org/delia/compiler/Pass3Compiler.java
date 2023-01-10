package org.delia.compiler;

import org.delia.compiler.ast.AST;
import org.delia.core.FactoryService;
import org.delia.db.DBType;
import org.delia.error.DeliaError;
import org.delia.error.ErrorFormatter;
import org.delia.hld.HLDFirstPassResults;
import org.delia.relation.RelationInfo;
import org.delia.type.*;
import org.delia.util.DRuleHelper;

import java.util.HashMap;
import java.util.Map;

//is the linking phase
//FUTURE: if we tracked field types we could check that .year() was with a Date field
public class Pass3Compiler extends CompilerPassBase {

    private boolean buildTypeMapFlag = false;

    public Pass3Compiler(FactoryService factorySvc, HLDFirstPassResults firstPassResults, DBType dbType, ErrorFormatter errorFormatter) {
        super(factorySvc, firstPassResults, dbType, errorFormatter);
    }

    public CompilerPassResults process(AST.DeliaScript script) {
        CompilerPassResults results = new CompilerPassResults();

        for (AST.StatementAst statement : script.statements) {
            if (statement instanceof AST.SchemaAst) {
                AST.SchemaAst exp = (AST.SchemaAst) statement;
            } else if (statement instanceof AST.TypeAst) {
                AST.TypeAst typeExp = (AST.TypeAst) statement;

                if (buildTypeMapFlag) {
//					if (typeMap.containsKey(typeExp.typeName)) {
//						String msg = String.format("type '%s' already defined. Cannot redefine", typeExp.typeName);
//						DeliaError err = createError("type-duplicate", msg, exp);
//						results.errors.add(err);
//					}
//					otherTypeChecks(typeExp, results);
//					typeMap.put(typeExp.typeName, buildFieldList(typeExp));
                } else {
                    otherTypeChecks(typeExp, results);
                }
//			} else if (exp instanceof LetStatementExp) {
//				LetStatementExp letExp = (LetStatementExp) exp;
////				log.log("a" + exp.strValue());
//				if (varMap.containsKey(letExp.varName)) {
//					String msg = String.format("var '%s' already defined. Cannot redefine", letExp.varName);
//					DeliaError err = createError("var-duplicate", msg, exp);
//					results.errors.add(err);
//				}
//				varMap.put(letExp.varName, null); //res part not needed
//
//				if (letExp.isType(LetStatementExp.USER_FUNC_TYPE)) {
//					checkFuncArgs(letExp, results);
//				}
//			} else if (exp instanceof UserFunctionDefStatementExp) {
//				UserFunctionDefStatementExp funcExp = (UserFunctionDefStatementExp) exp;
////				log.log("fn " + exp.strValue());
//				if (userFnMap.containsKey(funcExp.funcName)) {
//					String msg = String.format("function '%s' already defined. Cannot redefine", funcExp.funcName);
//					DeliaError err = createError("user-func-duplicate", msg, exp);
//					results.errors.add(err);
//				}
//				userFnMap.put(funcExp.funcName, funcExp);
//			} else if (exp instanceof InputFunctionDefStatementExp) {
//				InputFunctionDefStatementExp funcExp = (InputFunctionDefStatementExp) exp;
////				log.log("fn " + exp.strValue());
//				if (userFnMap.containsKey(funcExp.funcName)) {
//					String msg = String.format("input function '%s' already defined. Cannot redefine", funcExp.funcName);
//					DeliaError err = createError("input-func-duplicate", msg, exp);
//					results.errors.add(err);
//				}
//				inputFnMap.put(funcExp.funcName, funcExp);
            }
        }

//		//to handle forward declarations
//		for(Exp exp: list) {
//			if (exp instanceof TypeStatementExp) {
//				TypeStatementExp typeExp = (TypeStatementExp) exp;
//				finalTypeChecks(typeExp, results);
//			}
//		}

        return results;
    }
    private DTypeName createDTypename(String schema, String typeName) {
        return new DTypeName(schema, typeName);
    }


    private void otherTypeChecks(AST.TypeAst typeExp, CompilerPassResults results) {
        if (isScalarType(typeExp, results, false)) {
            checkRules(typeExp, null, results);
            return;
        }

        if (!typeExp.isScalarType) {
            DTypeName dtypeName = createDTypename(typeExp.schemaName, typeExp.typeName);
            DStructType structType = (DStructType) registry.getType(dtypeName);
            Map<String, String> fieldMap = new HashMap<>(); //local val. ok to not use ConcurrentHashMap here
            if (structType.getBaseType() != null) {
                DStructType baseType = (DStructType) structType.getBaseType();
                for (String fld : baseType.getDeclaredFields().keySet()) {
                    fieldMap.put(fld, fld);
                }

            }

            for (AST.TypeFieldAst sfe : typeExp.fields) {
                TypePair pair = structType.findField(sfe.fieldName);
                if (fieldMap.containsKey(sfe.fieldName)) {
                    String msg = String.format("field '%s' already defined in type '%s' or its base type(s)", sfe.fieldName, typeExp.typeName);
                    DeliaError err = createError("type-field-already-defined-in-base-type", msg, typeExp);
                    results.errors.add(err);
                }

                if (pair.type.isStructShape() && !sfe.isRelation) {
                    String msg = String.format("struct fields must use 'relation' - field '%s'", sfe.fieldName);
                    DeliaError err = createError("missing-relation-decl", msg, sfe);
                    results.errors.add(err);
                } else if (pair.type.isStructShape() && sfe.isRelation) {
                    if (!sfe.isOne && !sfe.isMany) {
                        String msg = String.format("type '%s' - relation field '%s' must specify 'primaryKey' or 'one' or 'many'", typeExp.typeName, sfe.fieldName);
                        DeliaError err = createError("relation-missing-one-or-many", msg, typeExp);
                        results.errors.add(err);
                    }

//					if (sfe.isParent && ! sfe.isOne) {
//						String msg = String.format("type '%s' - relation field '%s many' cannot use 'parent'", typeExp.typeName, sfe.fieldName);
//						DangError err = createError("relation-parent-not-allowed", msg, typeExp);
//						results.errors.add(err);
//					}

                    //TODO: fix this
                    DStructType otherSideType = findOtherSide(typeExp, sfe);
                    RelationInfo relinfo = DRuleHelper.findMatchingRuleInfo(structType, sfe.fieldName);
                    if (relinfo == null || relinfo.otherSide == null) { //one-way relation
                        if (sfe.isParent) {
                            String msg = String.format("type '%s' - one-sided relation field '%s' cannot use 'parent'", typeExp.typeName, sfe.fieldName);
                            DeliaError err = createError("relation-parent-not-allowed", msg, typeExp);
                            results.errors.add(err);
                        }
                    } else {
                        String otherSideFieldName = (relinfo == null) ? null : relinfo.otherSide.fieldName;
                        if (!sfe.isOptional && (otherSideFieldName != null && !otherSideType.fieldIsOptional(otherSideFieldName))) {
                            String msg = String.format("type '%s' - relation field '%s'. M-M relations are not supported", typeExp.typeName, sfe.fieldName);
                            DeliaError err = createError("relation-mandatory-mandatory-not-allowed", msg, typeExp);
                            results.errors.add(err);
                        }

                        if (sfe.isParent) {
                            //TODO fix
//							??? RelationInfo relinfo = DRuleHelper.findMatchingRuleInfo(otherSideType,)
//							if (otherSideType.isParent) {
//								String msg = String.format("type '%s' - relation field '%s'. Only one side of relation can use 'parent'", typeExp.typeName, sfe.fieldName);
//								DeliaError err = createError("relation-parent-not-allowed", msg, typeExp);
//								results.errors.add(err);
//							} else if (otherSideType.isMany) {
//								String msg = String.format("type '%s' - relation field '%s'. The 'many side of relation canno be 'parent'", typeExp.typeName, sfe.fieldName);
//								DeliaError err = createError("relation-parent-not-allowed-in-many", msg, typeExp);
//								results.errors.add(err);
//							}
//
//							if (sfe.isMany) {
//								String msg = String.format("type '%s' - relation field '%s' many. Cannot use 'parent'", typeExp.typeName, sfe.fieldName);
//								DeliaError err = createError("relation-parent-not-allowed", msg, typeExp);
//								results.errors.add(err);
//							} else if (sfe.isOne && !sfe.isOptional) {
//								String msg = String.format("type '%s' - relation field '%s'. Child-side cannot use 'parent'", typeExp.typeName, sfe.fieldName);
//								DeliaError err = createError("relation-parent-not-allowed", msg, typeExp);
//								results.errors.add(err);
//							}
                        }
                    }
                }
            }
        }
    }

    private DStructType findOtherSide(AST.TypeAst typeExp, AST.TypeFieldAst sfe) {
        DTypeName dtypeName = createDTypename(sfe.schemaName, sfe.typeName);
        DType otherType = registry.getType(dtypeName);
        return (DStructType) otherType;
    }

    /**
     * Only check rules once typeMap has been built.
     */
    private void checkRules(AST.TypeAst typeExp, Map<String, String> fieldMap, CompilerPassResults results) {
    }


//	private void finalTypeChecks(TypeStatementExp typeExp, CompilerResults results) {
//		if (isScalarType(typeExp, results, true)) {
//			return;
//		}
//
//		if (typeExp.baseTypeName.equals("struct")) {
//			checkPrimaryKeys(typeExp, results);
//		} else  {
//			//FUTURE: support forward references
//			if (!typeMap.containsKey(typeExp.baseTypeName)) {
//				String msg = String.format("type '%s' uses undefined base type '%s'", typeExp.typeName, typeExp.baseTypeName);
//				DeliaError err = createError("type-unknown-base-type", msg, typeExp);
//				results.errors.add(err);
//			}
//		}
//
//
//		Map<String,String> fieldMap = new HashMap<>(); //local var. ok to not use ConcurrentHashMap here
//		buildFieldMap(typeExp.baseTypeName, fieldMap);
//
//		if (typeExp.structExp != null) {
//			for(StructFieldExp sfe: typeExp.structExp.argL) {
//				isStructType(sfe, results, typeExp.typeName, true);
//			}
//		}
//	}
//
//	private void checkPrimaryKeys(TypeStatementExp typeExp, CompilerResults results) {
//		//what goes here?
//	}

    private boolean isScalarType(AST.TypeAst typeExp, CompilerPassResults results, boolean doTypeChecks) {
        if (BuiltInTypes.isBuiltInScalarType(typeExp.baseName)) {
            return true;
        }

        String typeName = typeExp.typeName;
        String baseTypeName = typeExp.baseName;
        int runawayCount = 100;
        while (true) {
            if (runawayCount-- == 0) {
                String msg = String.format("runaway on base type '%s'", baseTypeName);
                DeliaError err = createError("pass3-runaway", msg, typeExp);
                results.errors.add(err);
                return false;
            }
            if (BuiltInTypes.isBuiltInScalarType(baseTypeName)) {
                return true;
            } else if (baseTypeName.equals("struct")) {
                return false;
            } else if (isShapeType(baseTypeName)) {
                return true;
            }

            //note. base type may not have same schema as type
            DTypeName dtypeName = createDTypename(typeExp.schemaName, typeName);
            DType tt = registry.getType(dtypeName);
            tt = tt.getBaseType();

//            DTypeName dtypeName = createDTypename(typeExp.schemaName, baseTypeName);
//            DType tt = registry.getType(dtypeName);
//			TypeSpec tt = typeMap.get(baseTypeName);
            if (tt == null && doTypeChecks) {
                String msg = String.format("type '%s' uses undefined base type '%s'", typeName, baseTypeName);
                DeliaError err = createError("type-unknown-base-type", msg, typeExp);
                results.errors.add(err);
                return false;
            }

            return !tt.isStructShape();
//
//            typeName = baseTypeName;
//            baseTypeName = tt.getBaseType() == null ? null : tt.getBaseType().getName();
//
//            if (baseTypeName == null) {
//                return false;
//            }
        }
    }

    private boolean isShapeType(String baseTypeName) {
        BuiltInTypes bit = null;
        try {
            bit = BuiltInTypes.valueOf(baseTypeName);
        } catch (Exception e) {
        }
        return bit != null;
    }

}