package org.delia.hld.cond;

import java.util.List;

import org.delia.compiler.ast.BooleanExp;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.FilterExp;
import org.delia.compiler.ast.FilterOpExp;
import org.delia.compiler.ast.FilterOpFullExp;
import org.delia.compiler.ast.IdentExp;
import org.delia.compiler.ast.IntegerExp;
import org.delia.compiler.ast.ListExp;
import org.delia.compiler.ast.LongExp;
import org.delia.compiler.ast.QueryExp;
import org.delia.compiler.ast.QueryInExp;
import org.delia.compiler.ast.StringExp;
import org.delia.compiler.astx.XNAFMultiExp;
import org.delia.compiler.astx.XNAFNameExp;
import org.delia.compiler.astx.XNAFSingleExp;
import org.delia.hld.ValType;
import org.delia.runner.VarEvaluator;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.type.TypePair;
import org.delia.util.DValueHelper;
import org.delia.util.DeliaExceptionHelper;

public class FilterCondBuilder {

	private DTypeRegistry registry;
	private DStructType fromType;
	private VarEvaluator varEvaluator;

	public FilterCondBuilder(DTypeRegistry registry, DStructType fromType, VarEvaluator varEvaluator) {
		this.registry = registry;
		this.fromType = fromType;
		this.varEvaluator = varEvaluator;
	}
	
	public FilterCond build(QueryExp queryExp) {
		return build(queryExp, false);
	}
	public FilterCond build(QueryExp queryExp, boolean isVarRef) {
		if (isVarRef) {
			return new BooleanFilterCond(new BooleanExp(true)); //not really true but we need a filter
		}
		
		Exp cond = queryExp.filter.cond;
		return doBuild(cond);
	}
	private FilterCond doBuild(Exp cond) {
		if (cond instanceof BooleanExp) {
			BooleanExp exp = (BooleanExp) cond;
			return new BooleanFilterCond(exp);
		} else if (cond instanceof IntegerExp) {
			IntegerExp exp = (IntegerExp) cond;
			return new IntegerFilterCond(exp);
		} else if (cond instanceof LongExp) {
			LongExp exp = (LongExp) cond;
			return new LongFilterCond(exp);
		} else if (cond instanceof StringExp) {
			StringExp exp = (StringExp) cond;
			return new StringFilterCond(exp);
		} else if (cond instanceof IdentExp) {
			return handleVarReference((IdentExp)cond);
		} else if (cond instanceof FilterOpFullExp) {
			FilterOpFullExp exp = (FilterOpFullExp) cond;
			if (isAndOrExp(exp.opexp1)) {
				return doAndOrCond(exp);
			} else if (isOtherAndOrExp(exp)) {
				return doOtherAndOrCond(exp);
			} else if (exp.opexp1 instanceof FilterOpExp) {
				FilterOpExp foexp = (FilterOpExp) exp.opexp1;
				if (foexp.op1 instanceof XNAFMultiExp) {
					XNAFMultiExp xnaf = (XNAFMultiExp) foexp.op1;
					OpFilterCond opfiltercond = new OpFilterCond();
					opfiltercond.isNot = exp.negFlag;
					opfiltercond.op = new FilterOp(foexp.op);
					opfiltercond.val1 = buildValOrFunc(exp, foexp, xnaf); 
					opfiltercond.val2 = new FilterVal(createValType(foexp.op2), foexp.op2);
					return opfiltercond;
				} else if (foexp.op2 instanceof XNAFMultiExp) {
					XNAFMultiExp xnaf = (XNAFMultiExp) foexp.op2;

					OpFilterCond opfiltercond = new OpFilterCond();
					opfiltercond.isNot = exp.negFlag;
					opfiltercond.op = new FilterOp(foexp.op);
					opfiltercond.val1 = new FilterVal(createValType(foexp.op1), foexp.op1);
					opfiltercond.val2 = buildValOrFunc(exp, foexp, xnaf); 
					return opfiltercond;
				}
			} else if (exp.opexp1 instanceof QueryInExp) {
				QueryInExp inexp = (QueryInExp) exp.opexp1;
				InFilterCond incond = new InFilterCond();
				incond.isNot = exp.negFlag;
				incond.val1 = new FilterVal(ValType.SYMBOL, new StringExp(inexp.fieldName));
				
				ListExp listexp = inexp.listExp;
				for(Exp tmp: listexp.valueL) {
					FilterVal fval = new FilterVal(createValType(tmp), tmp); 
					incond.list.add(fval);
				}
				return incond;
			}
		}
		return null;
	}


	private FilterCond handleVarReference(IdentExp iexp) {
		String varName = iexp.name();
		List<DValue> list = varEvaluator.lookupVar(varName);
		if (list.size() == 0) {
			DeliaExceptionHelper.throwError("bad-var-value", "Var '%s' used in filter is empty. Not allowed", varName);
		} else if (list.size() > 1) {
			DeliaExceptionHelper.throwError("bad-var-value", "Var '%s' used in filter has %d values. Only one value is allowed", varName, list.size());
		} else {
			DValue dval = list.get(0);
			if (dval == null) {
				DeliaExceptionHelper.throwError("bad-var-value", "Var '%s' used in filter has null value. Not allowed", varName);
			} 
			return singleFilterFromDVal(dval, varName);
		}
		return null;
	}

	private FilterCond singleFilterFromDVal(DValue dval, String varName) {
		return FilterValHelper.singleFilterFromDVal(dval, varName);
	}

	private boolean isOtherAndOrExp(FilterOpFullExp exp) {
		if (exp.opexp1 instanceof FilterOpFullExp) {
			return true;
		}
		return false;
	}

	private boolean isAndOrExp(Exp opexp1) {
		if (opexp1 instanceof FilterExp) {
			if (((FilterExp) opexp1).cond instanceof FilterOpFullExp) {
				return true;
			}
		}
		return false;
	}

	private FilterCond doAndOrCond(FilterOpFullExp exp) {
		OpAndOrFilter filter = new OpAndOrFilter();
		FilterExp fexp1 = (FilterExp) exp.opexp1;
		FilterExp fexp2 = (FilterExp) exp.opexp2;
		filter.cond1 = doBuild(fexp1.cond);
		filter.cond2 = doBuild(fexp2.cond);
		filter.isAnd = exp.isAnd;
		return filter;
	}
	private FilterCond doOtherAndOrCond(FilterOpFullExp exp) {
		OpAndOrFilter filter = new OpAndOrFilter();
		FilterOpFullExp outerfexp1 = (FilterOpFullExp) exp.opexp1;
		FilterOpFullExp outerfexp2 = (FilterOpFullExp) exp.opexp2;
		FilterOpExp fexp1 = (FilterOpExp) outerfexp1.opexp1;
		FilterOpExp fexp2 = (FilterOpExp) outerfexp2.opexp1;
		filter.cond1 = doBuild2(outerfexp1, fexp1);
		filter.cond2 = doBuild2(outerfexp2, fexp2);
		filter.isAnd = exp.isAnd;
		return filter;
	}


	private FilterCond doBuild2(FilterOpFullExp outerfexp1, Exp cond) {
		if (cond instanceof FilterOpExp) {
			FilterOpExp foexp = (FilterOpExp) cond;
			if (foexp.op1 instanceof XNAFMultiExp) {
				XNAFMultiExp xnaf = (XNAFMultiExp) foexp.op1;
				OpFilterCond opfiltercond = new OpFilterCond();
				opfiltercond.isNot = outerfexp1.negFlag;
				opfiltercond.op = new FilterOp(foexp.op);
				opfiltercond.val1 = buildValOrFunc(outerfexp1, foexp, xnaf); 
				opfiltercond.val2 = new FilterVal(createValType(foexp.op2), foexp.op2);
				return opfiltercond;
			} else if (foexp.op2 instanceof XNAFMultiExp) {
				XNAFMultiExp xnaf = (XNAFMultiExp) foexp.op2;

				OpFilterCond opfiltercond = new OpFilterCond();
				opfiltercond.isNot = outerfexp1.negFlag;
				opfiltercond.op = new FilterOp(foexp.op);
				opfiltercond.val1 = new FilterVal(createValType(foexp.op1), foexp.op1);
				opfiltercond.val2 = buildValOrFunc(outerfexp1, foexp, xnaf); 
				return opfiltercond;
			}
		}
		return null; //TODO: fix
	}

	private FilterVal buildValOrFunc(FilterOpFullExp exp, FilterOpExp foexp, XNAFMultiExp xnaf) {
		if (xnaf.qfeL.size() == 1) {
			XNAFSingleExp el = xnaf.qfeL.get(0);
			return new FilterVal(ValType.SYMBOL, el);
		} else {
			XNAFSingleExp el = (XNAFSingleExp) xnaf.qfeL.get(0);
//			XNAFNameExp el = (XNAFNameExp) xnaf.qfeL.get(0);
			XNAFSingleExp el2 = xnaf.qfeL.get(1); //TODO handle more than 2 later
			//Note. addr.y is a field but will become a FUNCTION here
			TypePair pair = DValueHelper.findField(fromType, el.strValue());
			TypePair pair2 = (pair == null) ? null : DValueHelper.findField(pair.type, el2.strValue());
			if (pair != null && pair2 != null) {
				FilterVal fval = new FilterVal(ValType.SYMBOLCHAIN, el); //has .addr
				fval.symchain = new SymbolChain(fromType); //has .y
				fval.symchain.list.add(el2.strValue());
				return fval;
			} else {
				FilterVal fval = new FilterVal(ValType.FUNCTION, el);
				fval.filterFn = new FilterFunc();
				fval.filterFn.fnName = el2.funcName; //TODO: handle args later
				return fval;
			}
		}
	}

	private ValType createValType(Exp op2) {
		return FilterValHelper.createValType(op2);
	}
}