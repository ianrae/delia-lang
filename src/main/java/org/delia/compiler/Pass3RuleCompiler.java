package org.delia.compiler;

import java.util.List;
import java.util.Map;

import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.FilterOpExp;
import org.delia.compiler.ast.IdentExp;
import org.delia.compiler.ast.RuleExp;
import org.delia.compiler.ast.StructFieldExp;
import org.delia.compiler.ast.TypeStatementExp;
import org.delia.compiler.astx.XNAFMultiExp;
import org.delia.compiler.astx.XNAFSingleExp;
import org.delia.core.FactoryService;
import org.delia.error.DeliaError;
import org.delia.runner.InternalCompileState;
import org.delia.type.BuiltInTypes;
import org.delia.typebuilder.RuleFuncFactory;

public class Pass3RuleCompiler extends CompilerPassBase {

	public Pass3RuleCompiler(FactoryService factorySvc, ErrorLineFinder errorLineFinder, InternalCompileState execCtx) {
		super(factorySvc, errorLineFinder, execCtx);
	}

	@Override
	public CompilerResults process(List<Exp> list) {
		throw new RuntimeException("not allowed");
	}

	public void processTypeExp(TypeStatementExp typeExp, Map<String, String> fieldMap, CompilerResults results) {
		if (typeExp.hasRules()) {
			for(RuleExp ruleExp: typeExp.ruleSetExp.ruleL) {
				checkRule(results, ruleExp, typeExp, fieldMap);
			}
		}
		
	}

	private void addError(CompilerResults results, String id, String msg, Exp exp) {
		DeliaError err = createError(id, msg, exp);
		results.errors.add(err);
	}

	private boolean isScalar(StructFieldExp qfe) {
		return BuiltInTypes.isBuiltInScalarType(qfe.typeName);
	}

	private void checkRule(CompilerResults results, RuleExp ruleExp, TypeStatementExp typeExp, Map<String, String> fieldMap) {
		if (ruleExp.opExpr instanceof FilterOpExp) {
			//check op1 and op2
			FilterOpExp foe = (FilterOpExp) ruleExp.opExpr;
			Exp exp = foe.op1;
			checkRuleOperand(results, exp, typeExp, ruleExp, fieldMap);
			exp = foe.op2;
			checkRuleOperand(results, exp, typeExp, ruleExp, fieldMap);
		}
		//TODO do funcs later
	}
	private void checkRuleOperand(CompilerResults results, Exp exp, TypeStatementExp typeExp, RuleExp ruleExp, Map<String, String> fieldMap) {
		if (exp instanceof IdentExp) {
			if (! findField(exp.strValue(), typeExp, fieldMap)) {
				String ruleText = ruleExp.toString();
				String msg = String.format("Rule '%s' - can't find field '%s'", ruleText, exp.strValue());
				addError(results, "rule-op-not-found", msg, exp);
			}
		} else if (exp instanceof XNAFMultiExp) {
			XNAFMultiExp rfe = (XNAFMultiExp) exp;
			if (rfe.qfeL.size() == 1) {
				XNAFSingleExp qfe = rfe.qfeL.get(0);
				if (qfe.argL.size() == 0) {
					//might be a fieldName or fn
					if (!qfe.isRuleFn && findField(qfe.funcName, typeExp, fieldMap)) {
						return;
					}
					
					RuleFuncFactory ruleFactory = new RuleFuncFactory(factorySvc);
					if (ruleFactory.createRule(rfe, 0) != null) {
						return;
					}
					
					String msg = String.format("unknown field or function: '%s'", qfe.funcName);
					addError(results, "rule-unknown-field-or-fn", msg, qfe);
				}
			} else if (rfe.qfeL.size() == 2) {
				XNAFSingleExp qfe0 = rfe.qfeL.get(0);
				XNAFSingleExp qfe1 = rfe.qfeL.get(1);
				if (!qfe0.isRuleFn && qfe1.isRuleFn) {
					RuleFuncFactory ruleFactory = new RuleFuncFactory(factorySvc);
					if (ruleFactory.createRule(rfe, 0) != null) {
						return;
					}
					
					String msg = String.format("unknown field or function: '%s'", qfe1.funcName);
					addError(results, "rule-unknown-field-or-fn", msg, qfe1);
				}
			}
		}
	}

	/**
	 * Note the fieldMap is of base types fields.
	 */
	private boolean findField(String targetFieldName, TypeStatementExp typeExp, Map<String, String> fieldMap) {
		if (fieldMap != null) {
			if (fieldMap.containsKey(targetFieldName)) {
				return true;
			}
		}
		
		for(StructFieldExp fieldExp: typeExp.structExp.argL) {
			if (fieldExp.getFieldName().equals(targetFieldName)) {
				return true;
			}
		}
		return false;
	}
}