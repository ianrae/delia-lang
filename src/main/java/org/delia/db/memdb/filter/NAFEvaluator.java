package org.delia.db.memdb.filter;

import org.delia.compiler.astx.XNAFMultiExp;
import org.delia.compiler.astx.XNAFSingleExp;
import org.delia.db.memdb.filter.filterfn.FilterFnRunner;
import org.delia.type.DRelation;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.util.DeliaExceptionHelper;

public class NAFEvaluator implements OpEvaluator {
	private XNAFMultiExp op1;
	private OpEvaluator inner;
	private DTypeRegistry registry;
	private FilterFnRunner filterFnRunner;
	private boolean negFlag;

	public NAFEvaluator(XNAFMultiExp op1, OpEvaluator inner, DTypeRegistry registry) {
		this.op1 = op1;
		this.inner = inner;
		this.registry = registry;
		this.filterFnRunner = new FilterFnRunner(registry);
	}

	@Override
	public boolean match(Object left) {
		boolean b = doMatch(left);
		if (negFlag) {
			return !b;
		} else {
			return b;
		}
	}
	private boolean doMatch(Object left) {
		DValue dval = (DValue) left;
		String fieldName = getFieldName();
		DValue fieldval = dval.asStruct().getField(fieldName);
		
		if (fieldval != null && fieldval.getType().isRelationShape()) {
			DRelation drel = fieldval.asRelation();
			if (!drel.haveFetched()) {
				DeliaExceptionHelper.throwError("implicit-fetch-needed", "Filter containing %s.%s needs in implicit fetch. This is a bug!", dval.getType().getName(), fieldName);
			}
		} else {
			DValue resultVal = filterFnRunner.executeFilterFn(op1, fieldval);
			if (resultVal != null) {
				return inner.match(resultVal);
			}
		}

		return false;
	}

	private String getFieldName() {
		//TODO: add error checking
		XNAFSingleExp first = this.op1.qfeL.get(0);
		return first.funcName;
	}

	@Override
	public void setRightVar(Object rightVar) {
		//unused
	}

	@Override
	public void setNegFlag(boolean negFlag) {
		this.negFlag = negFlag;
	}
}