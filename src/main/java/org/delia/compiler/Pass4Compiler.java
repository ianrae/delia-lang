package org.delia.compiler;

import java.util.List;

import org.delia.compiler.ast.DsonFieldExp;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.IdentExp;
import org.delia.compiler.ast.InsertStatementExp;
import org.delia.compiler.ast.LetStatementExp;
import org.delia.compiler.ast.QueryExp;
import org.delia.compiler.ast.TypeStatementExp;
import org.delia.compiler.ast.UpdateStatementExp;
import org.delia.compiler.ast.inputfunction.IdentPairExp;
import org.delia.compiler.ast.inputfunction.InputFuncMappingExp;
import org.delia.compiler.ast.inputfunction.InputFunctionDefStatementExp;
import org.delia.compiler.astx.XNAFSingleExp;
import org.delia.core.FactoryService;
import org.delia.error.DeliaError;
import org.delia.relation.RelationCardinality;
import org.delia.rule.rules.RelationManyRule;
import org.delia.runner.InternalCompileState;
import org.delia.type.BuiltInTypes;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.TypePair;
import org.delia.util.DRuleHelper;
import org.delia.util.DValueHelper;

//final checks using registry
public class Pass4Compiler extends CompilerPassBase {
	private DTypeRegistry registry;
	
	public Pass4Compiler(FactoryService factorySvc, ErrorLineFinder errorLineFinder, InternalCompileState execCtx, DTypeRegistry registry) {
		super(factorySvc, errorLineFinder, execCtx);
		this.registry = registry;
	}

	@Override
	public CompilerResults process(List<Exp> list) {
		CompilerResults results = new CompilerResults();
		results.list = list;
		
		for(Exp exp: list) {
			if (exp instanceof TypeStatementExp) {
				TypeStatementExp typeExp = (TypeStatementExp) exp;
				checkPrimaryKeys(typeExp, results);
			} else if (exp instanceof InsertStatementExp) {
				InsertStatementExp insExp = (InsertStatementExp) exp;
				chkAssocCrudInsert(insExp, results);
			} else if (exp instanceof UpdateStatementExp) {
				UpdateStatementExp insExp = (UpdateStatementExp) exp;
				chkAssocCrudUpdate(insExp, results);
			} else if (exp instanceof InputFunctionDefStatementExp) {
				InputFunctionDefStatementExp funcExp = (InputFunctionDefStatementExp) exp;
				chkInputFunc(funcExp, results);
			} else if (exp instanceof LetStatementExp) {
				checkLet((LetStatementExp)exp, results);
			}
		}
		
		return results;
	}

	private void checkLet(LetStatementExp letExp, CompilerResults results) {
		if (!BuiltInTypes.isBuiltInScalarType(letExp.typeName)) {
			DType dtype = registry.getType(letExp.typeName);
			if (dtype != null && dtype.isStructShape()) {
				if (letExp.value instanceof QueryExp) {
					QueryExp qexp = (QueryExp) letExp.value;
					doMissingFilterCheck(results, qexp, letExp.typeName, letExp);
				}
			}
		}
	}
	private void doMissingFilterCheck(CompilerResults results, QueryExp queryExp, String typeName, Exp exp) {
		if (queryExp.filter == null) {
			String msg = String.format("update: filter is missing on '%s'", typeName);
			addError(results, "missing-filter", msg, exp);
		}
	}
	private void addError(CompilerResults results, String id, String msg, Exp exp) {
		DeliaError err = createError(id, msg, exp);
		results.errors.add(err);
	}

	private void chkInputFunc(InputFunctionDefStatementExp funcExp, CompilerResults results) {
		for(InputFuncMappingExp mappingExp: funcExp.getMappings()) {
			String alias = mappingExp.outputField.typeName();
			String fieldName = mappingExp.outputField.argName();
			
			if (! mappingExp.isValidInputField()) {
				String msg = String.format("input function '%s': invalid input field '%s'.", funcExp.funcName, mappingExp.inputField.strValue());
				DeliaError err = createError("input-function-invalid-input-field", msg, funcExp);
				results.errors.add(err);
			} else if (mappingExp.isSyntheticInputField()) {
				XNAFSingleExp sexp = mappingExp.getSingleExp();
				if (sexp.argL.size() != 1) {
					String msg = String.format("input function '%s': 'value' function must have a single parameter.", funcExp.funcName);
					DeliaError err = createError("input-function-invalid-synthetic-field", msg, funcExp);
					results.errors.add(err);
				}
			}
			
			if (mappingExp.outputViaTargetExp != null) {
				checkVia(mappingExp.outputViaTargetExp, funcExp, results);
			}
			
			
			String typeName = null;
			for(IdentPairExp pairExp: funcExp.argsL) {
				if (pairExp.val2.equals(alias)) {
					typeName = pairExp.val1;
				}
			}
			
			if (typeName == null) {
				String msg = String.format("input function '%s': unknown alias '%s'.", funcExp.funcName, alias);
				DeliaError err = createError("input-function-unknown-alias", msg, funcExp);
				results.errors.add(err);
				return;
			}			
			
			if (! registry.existsType(typeName)) {
				String msg = String.format("input function '%s': unknown type '%s'.", funcExp.funcName, typeName);
				DeliaError err = createError("input-function-unknown-typename", msg, funcExp);
				results.errors.add(err);
			} else {
				DType dtype = registry.getType(typeName);
				if (! dtype.isStructShape()) {
					String msg = String.format("input function '%s': type '%s' is not a struct.", funcExp.funcName, typeName);
					DeliaError err = createError("input-function-bad-typename", msg, funcExp);
					results.errors.add(err);
				}
				if (! DValueHelper.fieldExists(dtype, fieldName)) {
					String msg = String.format("input function '%s': unknown field '%s.%s'", funcExp.funcName, typeName, fieldName);
					DeliaError err = createError("input-function-unknown-field", msg, funcExp);
					results.errors.add(err);
				}
			}
		}
	}

	private void checkVia(IdentExp outputViaTargetExp, InputFunctionDefStatementExp funcExp, CompilerResults results) {
		boolean found = false;
		for(InputFuncMappingExp mappingExp: funcExp.getMappings()) {
			String fieldName = mappingExp.getInputField();
			if (outputViaTargetExp.strValue().equals(fieldName)) {
				found = true;
			}
		}	
		if (! found) {
			String msg = String.format("input function '%s': via [%s] references unknown input field", funcExp.funcName, outputViaTargetExp.strValue());
			DeliaError err = createError("input-function-unknown-via", msg, funcExp);
			results.errors.add(err);

		}
	}

	private void chkAssocCrudInsert(InsertStatementExp insExp, CompilerResults results) {
		for(Exp exp: insExp.dsonExp.argL) {
			if (exp instanceof DsonFieldExp) {
				DsonFieldExp fexp = (DsonFieldExp) exp;
				if (fexp.assocCrudAction != null) {
					String action = fexp.assocCrudAction.strValue();
					String msg = String.format("insert '%s': '%s' not allowed in insert statement'", insExp.typeName, action);
					DeliaError err = createError("assoc-crud-in-insert-not-allowed", msg, insExp);
					results.errors.add(err);
				}
			}
		}
	}
	private void chkAssocCrudUpdate(UpdateStatementExp insExp, CompilerResults results) {
		for(Exp exp: insExp.dsonExp.argL) {
			if (exp instanceof DsonFieldExp) {
				DsonFieldExp fexp = (DsonFieldExp) exp;
				if (fexp.assocCrudAction != null) {
					String fieldName = fexp.getFieldName();
					DStructType structType = (DStructType) registry.getType(insExp.typeName);
					RelationManyRule manyRule = DRuleHelper.findManyRule(structType, fieldName);
					if (manyRule == null) {
						String action = fexp.assocCrudAction.strValue();
						String msg = String.format("update '%s': '%s' on field '%s' not allowed. Field is not a many relation.'", insExp.typeName, action, fieldName);
						DeliaError err = createError("assoc-crud-in-update-not-allowed", msg, insExp);
						results.errors.add(err);
					} else if (!RelationCardinality.MANY_TO_MANY.equals(manyRule.relInfo.cardinality)) {
						String action = fexp.assocCrudAction.strValue();
						String msg = String.format("update '%s': '%s' on field '%s' not allowed. Field is not a many-to-many relation.'", insExp.typeName, action, fieldName);
						DeliaError err = createError("assoc-crud-in-update-not-allowed", msg, insExp);
						results.errors.add(err);
					}
				}
			}
		}
	}

	private void checkPrimaryKeys(TypeStatementExp typeExp, CompilerResults results) {
		DType type = registry.getType(typeExp.typeName);
		if (! type.isStructShape()) {
			return;
		}
		DStructType structType = (DStructType) type;
		TypePair pair = DValueHelper.findPrimaryKeyFieldPair(structType);
		if (pair == null) {
			return;
		}
		
		if (! keyFieldIsAllowedType(pair)) {
			String msg = String.format("type '%s': primary key type %s is not allowed in field '%s'", typeExp.typeName, pair.type.getName(), pair.name);
			DeliaError err = createError("primary-key-type-not-allowed", msg, typeExp);
			results.errors.add(err);
		}
		if (! serialFieldIsAllowedType(pair, structType)) {
			String msg = String.format("type '%s': serial %s is not allowed in field '%s'", typeExp.typeName, pair.type.getName(), pair.name);
			DeliaError err = createError("primary-key-type-not-allowed", msg, typeExp);
			results.errors.add(err);
		}
	}
	
	private boolean keyFieldIsAllowedType(TypePair pair) {
		switch(pair.type.getShape()) {
		case INTEGER:
		case LONG:
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
		if (! structType.fieldIsSerial(pair.name)) {
			return true;
		}
		
		switch(pair.type.getShape()) {
		case INTEGER:
		case LONG:
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
}