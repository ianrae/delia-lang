package org.delia.db.memdb.filter;

import org.delia.compiler.ast.IdentExp;
import org.delia.compiler.ast.IntegerExp;
import org.delia.compiler.ast.LongExp;
import org.delia.compiler.ast.NullExp;
import org.delia.compiler.ast.NumberExp;
import org.delia.compiler.astx.XNAFMultiExp;
import org.delia.core.DateFormatService;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;

public class OpFactory {
	
	private DTypeRegistry registry;
	private DateFormatService fmtSvc;

	public OpFactory(DTypeRegistry registry, DateFormatService fmtSvc) {
		this.registry = registry;
		this.fmtSvc = fmtSvc;
	}

	public OpEvaluator create(String opStr, Object op1, Object op2, DType op1HintType, DType op2HintType, boolean negFlag) {
		OpEvaluator eval = doCreate(opStr, op1, op2, op1HintType, op2HintType);
		if (eval != null) {
			eval.setNegFlag(negFlag);
		}
		return eval;
	}

	private OpEvaluator doCreate(String opStr, Object op1, Object op2, DType op1HintType, DType op2HintType) {
		OP op = OP.createFromString(opStr);
		if (op1 instanceof IdentExp) {
			IdentExp exp = (IdentExp) op1;
			String fieldName = exp.strValue();
			if (op1HintType != null) {
				return creatFromHint(op, fieldName, op1HintType);
			}

			OpEvaluator eval = createFromExp(op, op2, fieldName);
			return eval;
		} else if (op1 instanceof XNAFMultiExp) {
			XNAFMultiExp multiexp = (XNAFMultiExp) op1;
			OpEvaluator inner = createFromExp(op, op2, OpEvaluatorBase.SCALAR_VAL);
			inner.setRightVar(op2);
			OpEvaluator eval = new NAFEvaluator(multiexp, inner, registry);
			return eval;
		} else if (op2 instanceof IdentExp) {
			IdentExp exp = (IdentExp) op2;
			String fieldName = exp.strValue();
			if (op2HintType != null) {
				return creatFromHint(op, fieldName, op2HintType);
			}

			OpEvaluator eval = createFromExp(op, op1, fieldName);
			return eval;
		} else {
			XNAFMultiExp multiexp = (XNAFMultiExp) op2;
			OpEvaluator inner = createFromExp(op, op1, OpEvaluatorBase.SCALAR_VAL);
			inner.setRightVar(op1);
			OpEvaluator eval = new NAFEvaluator(multiexp, inner, registry);
			return eval;
		}
	}
	private OpEvaluator createFromExp(OP op, Object op2, String fieldName) {
		if (op2 instanceof IntegerExp) {
			return new IntOpEvaluator(op, fieldName);
		} else if (op2 instanceof LongExp) {
			return new LongOpEvaluator(op, fieldName);
		} else if (op2 instanceof NumberExp) {
			return new NumberOpEvaluator(op, fieldName);
		} else if (op2 instanceof NullExp) {
			return new NullOpEvaluator(op, fieldName);
		} else {
			return new StringOpEvaluator(op, fieldName);
		}
	}

	private OpEvaluator creatFromHint(OP op, String fieldName, DType hintType) {
		switch(hintType.getShape()) {
		case INTEGER:
			return new IntOpEvaluator(op, fieldName);
		case LONG:
			return new LongOpEvaluator(op, fieldName);
		case NUMBER:
			return new NumberOpEvaluator(op, fieldName);
		case BOOLEAN:
			return new BooleanOpEvaluator(op, fieldName);
		case DATE:
			return new DateOpEvaluator(op, fieldName, fmtSvc);
		case STRUCT:
			return new RelationOpEvaluator(op, fieldName, fmtSvc);
		default:
			return new StringOpEvaluator(op, fieldName);
		}
	}
}