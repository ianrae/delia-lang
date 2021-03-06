package org.delia.rule;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.FilterOpExp;
import org.delia.compiler.ast.IdentExp;
import org.delia.compiler.ast.IntegerExp;
import org.delia.compiler.ast.LongExp;
import org.delia.compiler.ast.NullExp;
import org.delia.compiler.ast.NumberExp;
import org.delia.compiler.ast.RuleExp;
import org.delia.compiler.ast.StructFieldExp;
import org.delia.compiler.ast.TypeStatementExp;
import org.delia.compiler.astx.XNAFMultiExp;
import org.delia.compiler.astx.XNAFSingleExp;
import org.delia.core.DateFormatService;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.rule.rules.CompareOpRule;
import org.delia.rule.rules.RelationManyRule;
import org.delia.rule.rules.RelationOneRule;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.TypePair;
import org.delia.util.DeliaExceptionHelper;

public class RuleBuilder extends ServiceBase {
	private static class RuleDefPair {
		public XNAFSingleExp exp;
		public String fieldName;
	}
	
	private DTypeRegistry registry;
	private RuleFunctionFactory ruleFactory;

	public RuleBuilder(FactoryService factorySvc, DTypeRegistry registry) {
		super(factorySvc);
		this.registry = registry;
		this.ruleFactory = factorySvc.createRuleFunctionFactory();
	}
	
	public void addRules(DType dtype, TypeStatementExp typeStatementExp) {
		for(RuleExp ruleExp: typeStatementExp.ruleSetExp.ruleL) {
			if (ruleExp.opExpr instanceof FilterOpExp) {
				FilterOpExp foe = (FilterOpExp) ruleExp.opExpr;
				RuleOperand oper1 = createOperand(foe.op1, dtype);
				RuleOperand oper2 = createOperand(foe.op2, dtype);
				RuleGuard guard = createGuard(oper1, oper2); //new AlwaysRuleGuard();
				DateFormatService fmtSvc = this.factorySvc.getDateFormatService();
				CompareOpRule rule = new CompareOpRule(guard, oper1, foe.op, oper2, fmtSvc);
				dtype.getRawRules().add(rule);
			} else if (ruleExp.opExpr instanceof XNAFMultiExp) {
				XNAFMultiExp rfe = (XNAFMultiExp) ruleExp.opExpr;
				DRule rule = ruleFactory.createRule(rfe, 0, dtype);
				if (rule != null) {
					dtype.getRawRules().add(rule);
				} else {
					DeliaExceptionHelper.throwError("unknown-rule", "Type %s: unknown rule '%s'", dtype.getName(), ruleExp.strValue());
				}
			}
		}
	}
	
	public List<String> findSizeofUpgrades(TypeStatementExp typeStatementExp) {
		List<String> fieldNameL = new ArrayList<>();
		for(RuleExp ruleExp: typeStatementExp.ruleSetExp.ruleL) {
			if (ruleExp.opExpr instanceof XNAFMultiExp) {
				XNAFMultiExp rfe = (XNAFMultiExp) ruleExp.opExpr;
				RuleDefPair pair = findSizeOfRule(rfe);
				if (pair == null) {
					continue;
				}
				
				IntegerExp arg = (IntegerExp) pair.exp.argL.get(0);
				if (arg.val.intValue() == 64) {
					fieldNameL.add(pair.fieldName);
				}
			}
		}
		return fieldNameL;
	}
	
	private RuleDefPair findSizeOfRule(XNAFMultiExp rfe) {
		//only one func. TODO: fix later!!
		XNAFSingleExp qfe0 = rfe.qfeL.get(0);
		XNAFSingleExp qfe = qfe0;

		String fieldName = null;
		if (rfe.qfeL.size() == 2) {
			XNAFSingleExp qfe1 = rfe.qfeL.get(1);
			if (!qfe0.isRuleFn && qfe1.isRuleFn) {
				fieldName = qfe0.funcName;
				qfe = qfe1;
			}
		}

		if (qfe.funcName.equals("sizeof")) {
			RuleDefPair pair = new RuleDefPair();
			pair.exp = qfe;
			pair.fieldName = fieldName;
			return pair;
		}
		return null;
	}

	private RuleGuard createGuard(RuleOperand oper1, RuleOperand oper2) {
		List<String> fields1 = oper1 == null ? null : oper1.getFieldList();
		List<String> fields2 = oper2 == null ? null : oper2.getFieldList();

		List<String> combinedL = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(fields1)) {
			combinedL.addAll(fields1);
		}
		if (CollectionUtils.isNotEmpty(fields2)) {
			combinedL.addAll(fields2);
		}
		
		if (combinedL.isEmpty()) {
			return new AlwaysRuleGuard();
		} else {
			return new NotNullGuard(combinedL);
		}
	}

	private RuleOperand createOperand(Exp op1, DType dtype) {
		if (op1 instanceof XNAFMultiExp) {
			XNAFMultiExp rfe = (XNAFMultiExp) op1;
			List<XNAFSingleExp> qfeL = rfe.qfeL;
			if (qfeL.size() == 1) {
				XNAFSingleExp qfe = qfeL.get(0);
				if (qfe.isRuleFn) {
					//add args later!!
					DRule rule = ruleFactory.createRule(rfe, 0, dtype);
					RuleRuleOperand rro = new RuleRuleOperand(qfe.funcName, rule, null);
					return rro;
				} else if (qfe.argL.size() == 0) {
					//is just a fieldname
					StructDValueRuleOperand oper = new StructDValueRuleOperand(qfe.funcName);
					return oper;
				}
			} else if (qfeL.size() == 2) {
				XNAFSingleExp qfe1 = qfeL.get(0);
				XNAFSingleExp qfe2 = qfeL.get(1);
				//add args later!!
				DRule rule = ruleFactory.createRule(rfe, 1, dtype);
				RuleRuleOperand rro = new RuleRuleOperand(qfe2.funcName, rule, qfe1.funcName);
				return rro;
			}
			return null;
		} else if (op1 instanceof IdentExp) {
			IdentExp ident = (IdentExp) op1;
			StructDValueRuleOperand oper = new StructDValueRuleOperand(ident.name());
			return oper;
		} else if (op1 instanceof NullExp) {
			return new ScalarRuleOperand(null);
		} else if (op1 instanceof IntegerExp) {
			return new ScalarRuleOperand(((IntegerExp) op1).val);
		} else if (op1 instanceof LongExp) {
			return new ScalarRuleOperand(((LongExp) op1).val);
		} else if (op1 instanceof NumberExp) {
			return new ScalarRuleOperand(((NumberExp) op1).val);
		} else {
			String s= op1.strValue();
			ScalarRuleOperand oper = new ScalarRuleOperand(s);
			return oper;
		}
	}

	public void addRelationRules(DStructType dtype, TypeStatementExp typeStatementExp) {
		for(TypePair pair: dtype.getAllFields()) {
			DType possibleStruct = pair.type;
			if (possibleStruct == null) {
				continue;
			}
			if (possibleStruct.isStructShape()) {
				StructFieldExp fieldExp = getFieldExp(typeStatementExp, pair.name);
				if (fieldExp.isPrimaryKey || fieldExp.isOne) {
					StructDValueRuleOperand oper = new StructDValueRuleOperand(pair.name);
					RuleGuard guard = new AlwaysRuleGuard(); 
					String relName = getRelationName(fieldExp, pair.name);
					RelationOneRule rule = new RelationOneRule(guard, oper, dtype, registry, fieldExp.isParent, relName);
					rule.nameIsExplicit = fieldExp.relationName != null;
					dtype.getRawRules().add(rule);
				} else if (fieldExp.isMany) {
					StructDValueRuleOperand oper = new StructDValueRuleOperand(pair.name);
					RuleGuard guard = new AlwaysRuleGuard(); 
					String relName = getRelationName(fieldExp, pair.name);
					RelationManyRule rule = new RelationManyRule(guard, oper, dtype, registry, relName);
					rule.nameIsExplicit = fieldExp.relationName != null;
					dtype.getRawRules().add(rule);
				}
			}
		}
	}
	
	private String getRelationName(StructFieldExp fieldExp, String fieldName) {
		if (fieldExp.relationName != null) {
			return fieldExp.relationName;
		}
//		String name = String.format("__rule%d", factorySvc.getNextGeneratedRuleId());
		return fieldName;
	}

	private StructFieldExp getFieldExp(TypeStatementExp typeStatementExp, String fieldName) {
		for(StructFieldExp fieldExp: typeStatementExp.structExp.argL) {
			if (fieldExp.fieldName.equals(fieldName)) {
				return fieldExp;
			}
		}
		return null;
	}

}
