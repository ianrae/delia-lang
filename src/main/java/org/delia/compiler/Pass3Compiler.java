package org.delia.compiler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.LetStatementExp;
import org.delia.compiler.ast.StructFieldExp;
import org.delia.compiler.ast.TypeStatementExp;
import org.delia.compiler.ast.UserFnCallExp;
import org.delia.compiler.ast.UserFunctionDefStatementExp;
import org.delia.compiler.ast.inputfunction.InputFunctionDefStatementExp;
import org.delia.core.FactoryService;
import org.delia.error.DeliaError;
import org.delia.runner.InternalCompileState;
import org.delia.runner.ResultValue;
import org.delia.runner.TypeSpec;
import org.delia.type.BuiltInTypes;

//is the linking phase
//TODO: if we tracked field types we could check that .year() was with a Date field
public class Pass3Compiler extends CompilerPassBase {
	private Map<String,TypeSpec> typeMap = new HashMap<>(); //short-lived obj. ok to not use ConcurrentHashMap here
	private Map<String,ResultValue> varMap = new HashMap<>(); //short-lived obj. ok to not use ConcurrentHashMap here
	private Map<String,UserFunctionDefStatementExp> userFnMap = new HashMap<>(); //short-lived obj. ok to not use ConcurrentHashMap here
	private Map<String,InputFunctionDefStatementExp> inputFnMap = new HashMap<>(); //short-lived obj. ok to not use ConcurrentHashMap here
	private boolean buildTypeMapFlag = true;
	private Pass3RuleCompiler ruleCompiler;
	
	public Pass3Compiler(FactoryService factorySvc, ErrorLineFinder errorLineFinder, InternalCompileState execCtx) {
		super(factorySvc, errorLineFinder, execCtx);
		this.ruleCompiler = new Pass3RuleCompiler(factorySvc, errorLineFinder, execCtx);
		
		//copy in existing types,vars,etc
		if (execCtx != null) {
			this.typeMap.putAll(execCtx.compiledTypeMap);
			this.varMap.putAll(execCtx.delcaredVarMap);
			this.userFnMap.putAll(execCtx.declaredUserFnMap);
			this.inputFnMap.putAll(execCtx.declaredInputFnMap);
		}
	}

	@Override
	public CompilerResults process(List<Exp> list) {
		CompilerResults results = new CompilerResults();
		results.list = list;
		
		for(Exp exp: list) {
			if (exp instanceof TypeStatementExp) {
				TypeStatementExp typeExp = (TypeStatementExp) exp;
//				log.log("a" + exp.strValue());
				
				if (buildTypeMapFlag) {
					if (typeMap.containsKey(typeExp.typeName)) {
						String msg = String.format("type '%s' already defined. Cannot redefine", typeExp.typeName);
						DeliaError err = createError("type-duplicate", msg, exp);
						results.errors.add(err);
					}
					otherTypeChecks(typeExp, results);
					typeMap.put(typeExp.typeName, buildFieldList(typeExp));
				} else {
					otherTypeChecks(typeExp, results);
				}
			} else if (exp instanceof LetStatementExp) {
				LetStatementExp letExp = (LetStatementExp) exp;
//				log.log("a" + exp.strValue());
				if (varMap.containsKey(letExp.varName)) {
					String msg = String.format("var '%s' already defined. Cannot redefine", letExp.varName);
					DeliaError err = createError("var-duplicate", msg, exp);
					results.errors.add(err);
				}
				varMap.put(letExp.varName, null); //res part not needed
				
				if (letExp.isType(LetStatementExp.USER_FUNC_TYPE)) {
					checkFuncArgs(letExp, results);
				}
			} else if (exp instanceof UserFunctionDefStatementExp) {
				UserFunctionDefStatementExp funcExp = (UserFunctionDefStatementExp) exp;
//				log.log("fn " + exp.strValue());
				if (userFnMap.containsKey(funcExp.funcName)) {
					String msg = String.format("function '%s' already defined. Cannot redefine", funcExp.funcName);
					DeliaError err = createError("user-func-duplicate", msg, exp);
					results.errors.add(err);
				}
				userFnMap.put(funcExp.funcName, funcExp);
			} else if (exp instanceof InputFunctionDefStatementExp) {
				InputFunctionDefStatementExp funcExp = (InputFunctionDefStatementExp) exp;
//				log.log("fn " + exp.strValue());
				if (userFnMap.containsKey(funcExp.funcName)) {
					String msg = String.format("input function '%s' already defined. Cannot redefine", funcExp.funcName);
					DeliaError err = createError("input-func-duplicate", msg, exp);
					results.errors.add(err);
				}
				inputFnMap.put(funcExp.funcName, funcExp);
			} 
		}
		
		//to handle forward declarations
		for(Exp exp: list) {
			if (exp instanceof TypeStatementExp) {
				TypeStatementExp typeExp = (TypeStatementExp) exp;
				finalTypeChecks(typeExp, results);
			} 
		}
		
		return results;
	}

	private TypeSpec buildFieldList(TypeStatementExp typeExp) {
		TypeSpec spec = new TypeSpec();
		spec.fieldL = new ArrayList<>();
		spec.typeExp = typeExp;
		spec.baseTypeName = typeExp.baseTypeName.equals("struct") ? null : typeExp.baseTypeName;
		if (typeExp.structExp != null) {
			for(StructFieldExp sfe: typeExp.structExp.argL) {
				spec.fieldL.add(sfe.fieldName);
			}
		}
		return spec;
	}

	private void otherTypeChecks(TypeStatementExp typeExp, CompilerResults results) {
		if (isScalarType(typeExp, results, false)) {
			checkRules(typeExp, null, results);
			return;
		}
		
		Map<String,String> fieldMap = new HashMap<>(); //local val. ok to not use ConcurrentHashMap here
		buildFieldMap(typeExp.baseTypeName, fieldMap);
		
		checkRules(typeExp, fieldMap, results);
		
		if (typeExp.structExp != null) {
			for(StructFieldExp sfe: typeExp.structExp.argL) {
				if (fieldMap.containsKey(sfe.fieldName)) {
					String msg = String.format("field '%s' already defined in type '%s' or its base type(s)", sfe.fieldName, typeExp.typeName);
					DeliaError err = createError("type-field-already-defined-in-base-type", msg, typeExp);
					results.errors.add(err);
				}

				if (isStructType(sfe, results, typeExp.typeName, false) && ! sfe.isRelation) {
					String msg = String.format("struct fields must use 'relation' - field '%s'", sfe.fieldName);
					DeliaError err = createError("missing-relation-decl", msg, sfe);
					results.errors.add(err);
				} else if (isStructType(sfe, results, typeExp.typeName, false) && sfe.isRelation) {
					if (! sfe.isOne && ! sfe.isMany) {
						String msg = String.format("type '%s' - relation field '%s' must specify 'primaryKey' or 'one' or 'many'", typeExp.typeName, sfe.fieldName);
						DeliaError err = createError("relation-missing-one-or-many", msg, typeExp);
						results.errors.add(err);
					}
					
//					if (sfe.isParent && ! sfe.isOne) {
//						String msg = String.format("type '%s' - relation field '%s many' cannot use 'parent'", typeExp.typeName, sfe.fieldName);
//						DangError err = createError("relation-parent-not-allowed", msg, typeExp);
//						results.errors.add(err);
//					}
					
					StructFieldExp otherSideExp = findOtherSide(typeExp, sfe);
					if (otherSideExp == null) { //one-way relation
						if (sfe.isParent) {
							String msg = String.format("type '%s' - one-sided relation field '%s' cannot use 'parent'", typeExp.typeName, sfe.fieldName);
							DeliaError err = createError("relation-parent-not-allowed", msg, typeExp);
							results.errors.add(err);
						}
					} else {
						if (!sfe.isOptional && !otherSideExp.isOptional) {
							String msg = String.format("type '%s' - relation field '%s'. M-M relations are not supported", typeExp.typeName, sfe.fieldName);
							DeliaError err = createError("relation-mandatory-mandatory-not-allowed", msg, typeExp);
							results.errors.add(err);
						}
						
						if (sfe.isParent) {
							if (otherSideExp.isParent) {
								String msg = String.format("type '%s' - relation field '%s'. Only one side of relation can use 'parent'", typeExp.typeName, sfe.fieldName);
								DeliaError err = createError("relation-parent-not-allowed", msg, typeExp);
								results.errors.add(err);
							}
							
							if (sfe.isMany) {
								String msg = String.format("type '%s' - relation field '%s' many. Cannot use 'parent'", typeExp.typeName, sfe.fieldName);
								DeliaError err = createError("relation-parent-not-allowed", msg, typeExp);
								results.errors.add(err);
							} else if (sfe.isOne && !sfe.isOptional) {
								String msg = String.format("type '%s' - relation field '%s'. Child-side cannot use 'parent'", typeExp.typeName, sfe.fieldName);
								DeliaError err = createError("relation-parent-not-allowed", msg, typeExp);
								results.errors.add(err);
							}
						}
					}
				}
			}
		}
	}
	/**
	 * Only check rules once typeMap has been built.
	 */
	private void checkRules(TypeStatementExp typeExp, Map<String, String> fieldMap, CompilerResults results) {
		if (!buildTypeMapFlag) {
			ruleCompiler.processTypeExp(typeExp, fieldMap, results);
		}
	}

	private StructFieldExp findOtherSide(TypeStatementExp typeExp, StructFieldExp sfeTarget) {
		TypeSpec tt = this.typeMap.get(sfeTarget.typeName);
		if (tt == null) {
			return null;
		}
		
		//TODO: support named relations
		if (tt.typeExp.structExp != null) {
			for(StructFieldExp sfe: tt.typeExp.structExp.argL) {
				if (sfe.typeName.equals(typeExp.typeName)) {
					return sfe;
				}
			}
		}
		
		return null;
	}

	private void finalTypeChecks(TypeStatementExp typeExp, CompilerResults results) {
		if (isScalarType(typeExp, results, true)) {
			return;
		}
		
		if (typeExp.baseTypeName.equals("struct")) {
			checkPrimaryKeys(typeExp, results);
		} else  {
			//TODO: support forward references
			if (!typeMap.containsKey(typeExp.baseTypeName)) {
				String msg = String.format("type '%s' uses undefined base type '%s'", typeExp.typeName, typeExp.baseTypeName);
				DeliaError err = createError("type-unknown-base-type", msg, typeExp);
				results.errors.add(err);
			}
		}
		
		
		Map<String,String> fieldMap = new HashMap<>(); //local var. ok to not use ConcurrentHashMap here
		buildFieldMap(typeExp.baseTypeName, fieldMap);
		
		if (typeExp.structExp != null) {
			for(StructFieldExp sfe: typeExp.structExp.argL) {
				isStructType(sfe, results, typeExp.typeName, true);
			}
		}
	}

	private void checkPrimaryKeys(TypeStatementExp typeExp, CompilerResults results) {
		// TODO Auto-generated method stub
		
	}

	private boolean isScalarType(TypeStatementExp typeExp, CompilerResults results, boolean doTypeChecks) {
		if (BuiltInTypes.isBuiltInScalarType(typeExp.baseTypeName)) {
			return true;
		}
		
		String typeName = typeExp.typeName;
		String baseTypeName = typeExp.baseTypeName;
		int runawayCount = 100;
		while(true) {
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
			
			TypeSpec tt = typeMap.get(baseTypeName);
			if (tt == null && doTypeChecks) {
				String msg = String.format("type '%s' uses undefined base type '%s'", typeName, baseTypeName);
				DeliaError err = createError("type-unknown-base-type", msg, typeExp);
				results.errors.add(err);
				return false;
			}
			
			typeName = baseTypeName;
			baseTypeName = tt.baseTypeName;
			
			if (baseTypeName == null) {
				return false;
			}
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

	private boolean isStructType(StructFieldExp sfe, CompilerResults results, String typeName, boolean doTypeChecks) {
		if (BuiltInTypes.fromDeliaTypeName(sfe.typeName) != null) {
			return false;
		}
		TypeSpec spec = typeMap.get(sfe.typeName);
		if (spec == null) {
			if (doTypeChecks) {
				String msg = String.format("type '%s' uses undefined type '%s'", typeName, sfe.typeName);
				DeliaError err = createError("type-unknown-base-type", msg, sfe);
				results.errors.add(err);
			}
			return false;
		}
		
		
		if (spec.baseTypeName == null) {
			return true;
		} else if (BuiltInTypes.valueOf(spec.baseTypeName) != null) {
			//TODO: fix. this won't handle double inheritance!
			return false;
		}
		
		return false;
	}

	private void buildFieldMap(String baseTypeName, Map<String, String> fieldMap) {
		if (baseTypeName == null || baseTypeName.equals("struct")) {
			return;
		}
		
		TypeSpec spec = typeMap.get(baseTypeName);
		for(String s: spec.fieldL) {
			fieldMap.put(s, s);
		}
		
		buildFieldMap(spec.baseTypeName, fieldMap); //**recursion**
	}

	private void checkFuncArgs(LetStatementExp letExp, CompilerResults results) {
		UserFnCallExp callExp = (UserFnCallExp) letExp.value;
		
		UserFunctionDefStatementExp userFnExp = this.userFnMap.get(callExp.funcName);
		
		if (userFnExp == null) {
			String msg = String.format("can't find function '%s'", callExp.funcName);
			DeliaError err = createError("user-func-not-found", msg, callExp);
			results.errors.add(err);
			return;
		}
		
		int nDecl = userFnExp.argsL.size();
		int nPassed = callExp.argL.size();
		if (nDecl != nPassed) {
			String msg = String.format("function '%s' has %d args, but only %d args passed.", callExp.funcName, nDecl, nPassed);
			DeliaError err = createError("user-func-wrong-num-args", msg, callExp);
			results.errors.add(err);
		}
	}

	public boolean isBuildTypeMapFlag() {
		return buildTypeMapFlag;
	}

	public void setBuildTypeMapFlag(boolean buildTypeMapFlag) {
		this.buildTypeMapFlag = buildTypeMapFlag;
	}
}