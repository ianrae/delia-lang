package org.delia.dbimpl.mem.impl.filter;

import org.delia.db.InternalException;
import org.delia.dbimpl.mem.impl.filter.filterfn.FilterFunctionService;
import org.delia.error.DeliaError;
import org.delia.tok.Tok;
import org.delia.type.DType;
import org.delia.type.DValue;
import org.delia.type.Shape;
import org.delia.util.DValueHelper;

import java.util.List;

public abstract class OpEvaluatorBase implements OpEvaluator {
	public static final String SCALAR_VAL = "__SCALAR__";

	protected String fieldName;
	protected OP op;
	protected Object rightVar;
	protected boolean negFlag;
	protected final FilterFunctionService filterFnSvc;
	protected List<Tok.FunctionTok> funcs;

	public OpEvaluatorBase(OP op, String fieldName, FilterFunctionService filterFnSvc) {
		this.op = op;
		this.fieldName = fieldName;
		this.filterFnSvc = filterFnSvc;
	}


	@Override
	public void setFuncs(List<Tok.FunctionTok> funcs) {
		this.funcs = funcs;
	}

	protected Boolean checkNull(DValue dval, Object right) {
		//for filterfns
		if (fieldName.equals(SCALAR_VAL)) {
			return null;
		}
		
		throwIfNotFieldName(dval);
		
		if (dval.asStruct().getField(fieldName) == null) {
			return (right instanceof Tok.NullTok);
		}
		if (right instanceof Tok.NullTok) {
			return false;
		}
		return null;
	}
	protected Boolean checkRelationNull(DValue dval, Object right) {
//		DRelation drel = dval.asRelation();
		if (dval == null || dval.asRelation() == null) {
			return (right instanceof Tok.NullTok);
		}
		if (right instanceof Tok.NullTok) {
			return false;
		}
		return null;
	}
	
	private void throwIfNotFieldName(DValue dval) {
		if (dval == null || ! dval.getType().isStructShape()) {
			return;
		}
		if (!DValueHelper.fieldExists(dval.getType(), fieldName)) {
			String msg = String.format("Type '%s' doesn't have field '%s'", dval.getType().getName(), fieldName);
			DeliaError err = new DeliaError("memdb-unknown-field", msg);
			throw new InternalException(err);
		}
	}

	protected DValue getFieldValue(DValue dval) {
		if (funcs != null) {
			return filterFnSvc.exec(dval, doGetFieldValue(dval), funcs);
		}
		return doGetFieldValue(dval);
	}
	private DValue doGetFieldValue(DValue dval) {
		if (fieldName.equals(SCALAR_VAL)) {
			return dval; //it's a scalar
		} else {
			throwIfNotFieldName(dval);
			return dval.asStruct().getField(fieldName);
		}
	}

	protected Integer resolveToInt(DValue dval) {
		DValue tmp = getFieldValue(dval);
		DType dtype = tmp.getType();
		if (dtype.isShape(Shape.NUMBER)) {
			Double n = tmp.asNumber();
			return n.intValue();
		} else {
			return tmp.asInt(); //will only work for int and long. others should never occur
		}
	}
//	protected Long resolveToLong(DValue dval) {
//		DValue tmp = getFieldValue(dval);
//		DType dtype = tmp.getType();
//		if (dtype.isShape(Shape.NUMBER)) {
//			Double n = tmp.asNumber();
//			return n.longValue();
//		} else {
//			return tmp.asLong(); //will only work for int and long. others should never occur
//		}
//	}
	
	@Override
	public boolean match(Object left) {
		boolean b = doMatch(left);
		if (negFlag) {
			return !b;
		} else {
			return b;
		}
	}
	protected abstract boolean doMatch(Object left);
	
	@Override
	public void setRightVar(Object rightVar) {
		this.rightVar = rightVar;
	}

	@Override
	public void setNegFlag(boolean negFlag) {
		this.negFlag = negFlag;
	}
}