package org.delia.db.newhls.cond;

import org.delia.compiler.ast.BooleanExp;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.FilterExp;
import org.delia.compiler.ast.FilterOpExp;
import org.delia.compiler.ast.FilterOpFullExp;
import org.delia.compiler.ast.IntegerExp;
import org.delia.compiler.ast.LongExp;
import org.delia.compiler.ast.NumberExp;
import org.delia.compiler.ast.QueryExp;
import org.delia.compiler.ast.StringExp;
import org.delia.compiler.astx.XNAFMultiExp;
import org.delia.compiler.astx.XNAFNameExp;
import org.delia.compiler.astx.XNAFSingleExp;
import org.delia.db.newhls.ValType;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.TypePair;
import org.delia.util.DValueHelper;

public class FilterCondBuilder {

	private DTypeRegistry registry;
	private DStructType fromType;

	public FilterCondBuilder(DTypeRegistry registry, DStructType fromType) {
		this.registry = registry;
		this.fromType = fromType;
	}
	
	public FilterCond build(QueryExp queryExp) {
		Exp cond = queryExp.filter.cond;
		return doBuild(cond);
	}
	private FilterCond doBuild(Exp cond) {
		if (cond instanceof BooleanExp) {
			BooleanExp exp = (BooleanExp) cond;
			return new BooleanFilterCond(exp);
		} else if (cond instanceof IntegerExp) {
			IntegerExp exp = (IntegerExp) cond;
			return new IntFilterCond(exp);
		} else if (cond instanceof LongExp) {
			LongExp exp = (LongExp) cond;
			return new LongFilterCond(exp);
		} else if (cond instanceof StringExp) {
			StringExp exp = (StringExp) cond;
			return new StringFilterCond(exp);
		} else if (cond instanceof FilterOpFullExp) {
			FilterOpFullExp exp = (FilterOpFullExp) cond;
			if (isAndOrExp(exp.opexp1)) {
				return doAndOrCond(exp);
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
			}
		}
		return null;
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

	private FilterVal buildValOrFunc(FilterOpFullExp exp, FilterOpExp foexp, XNAFMultiExp xnaf) {
		if (xnaf.qfeL.size() == 1) {
			XNAFSingleExp el = xnaf.qfeL.get(0);
			return new FilterVal(ValType.SYMBOL, el);
		} else {
			XNAFNameExp el = (XNAFNameExp) xnaf.qfeL.get(0);
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
		if (op2 instanceof BooleanExp) {
			return ValType.BOOLEAN;
		} else if (op2 instanceof IntegerExp) {
			return ValType.INT;
		} else if (op2 instanceof LongExp) {
			return ValType.LONG;
		} else if (op2 instanceof NumberExp) {
			return ValType.NUMBER;
		} else if (op2 instanceof StringExp) {
			return ValType.STRING;
		} else {
			return null; //TODO: error
		}
	}
}