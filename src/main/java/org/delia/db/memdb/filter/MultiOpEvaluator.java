package org.delia.db.memdb.filter;

import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.FilterOpExp;
import org.delia.compiler.ast.FilterOpFullExp;
import org.delia.compiler.ast.IdentExp;
import org.delia.core.DateFormatService;
import org.delia.core.FactoryService;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.util.DValueHelper;

public class MultiOpEvaluator implements OpEvaluator {
//	private Object rightVar;
	private FilterOpFullExp fullExp;
	private DStructType dtype;
	private OpEvaluator eval1;
	private OpEvaluator eval2;
	private DTypeRegistry registry;
	private DateFormatService fmtSvc;
	private FactoryService factorySvc;

	
	public MultiOpEvaluator(FilterOpFullExp fullexp, DStructType dtype, DTypeRegistry registry, DateFormatService fmtSvc, FactoryService factorySvc) {
		this.fullExp = fullexp;
		this.dtype = dtype;
		this.registry = registry;
		this.fmtSvc = fmtSvc;
		this.factorySvc = factorySvc;
		
		FilterOpFullExp f1 = (FilterOpFullExp) fullexp.opexp1;
		FilterOpFullExp f2 = (FilterOpFullExp) fullexp.opexp2;
		this.eval1 = createSingleOpEvaluator((FilterOpExp) f1.opexp1, dtype, f1.negFlag);
		this.eval2 = createSingleOpEvaluator((FilterOpExp) f2.opexp1, dtype, f2.negFlag);
	}

	@Override
	public boolean match(Object left) {
		boolean b = doMatch(left);
		if (fullExp.negFlag) {
			return !b;
		} else {
			return b;
		}
	}
	private boolean doMatch(Object left) {
		boolean b1 = eval1.match(left);
		if (fullExp.isAnd && !b1) {
			return false;
		}
		boolean b2 = eval2.match(left);
		if (fullExp.isAnd) {
			return b1 && b2;
		} else {
			return b1 || b2;
		}
	}
	
	private OpEvaluator createSingleOpEvaluator(FilterOpExp foexp, DStructType dtype, boolean negFlag) {
		//handle int-long issues by determining field type
		Exp xop1 = foexp.getFirstArg();
		Exp xop2 = foexp.getSecondArg();
		
		DType op1HintType = null;
		DType op2HintType = null;
		if (xop1 instanceof IdentExp) {
			String fieldOrVarOrFn = ((IdentExp)xop1).strValue();
			op1HintType = DValueHelper.findFieldType(dtype, fieldOrVarOrFn);
		}
		if (xop2 instanceof IdentExp) {
			String fieldOrVarOrFn = ((IdentExp)xop2).strValue();
			op2HintType = DValueHelper.findFieldType(dtype, fieldOrVarOrFn);
		}
		
		OpFactory factory = new OpFactory(registry, fmtSvc, factorySvc, dtype);
		OpEvaluator evaluator = factory.create(foexp.op, xop1, xop2, op1HintType, op2HintType,negFlag);
//		//TODO: this support id < 10. later support 10 < id too!!
		evaluator.setRightVar(xop2);
		return evaluator;
	}
	

	@Override
	public void setRightVar(Object rightVar) {
//		this.rightVar = rightVar;
	}

	@Override
	public void setNegFlag(boolean negFlag) {
		//not used
	}
}