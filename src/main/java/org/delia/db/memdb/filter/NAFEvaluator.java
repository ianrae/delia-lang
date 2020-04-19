package org.delia.db.memdb.filter;

import org.delia.compiler.astx.XNAFMultiExp;
import org.delia.compiler.astx.XNAFSingleExp;
import org.delia.db.memdb.filter.filterfn.FilterFnRunner;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;

public class NAFEvaluator implements OpEvaluator {
	private XNAFMultiExp op1;
	private OpEvaluator inner;
	private DTypeRegistry registry;
	private FilterFnRunner filterFnRunner;

	public NAFEvaluator(XNAFMultiExp op1, OpEvaluator inner, DTypeRegistry registry) {
		this.op1 = op1;
		this.inner = inner;
		this.registry = registry;
		this.filterFnRunner = new FilterFnRunner(registry);
	}

	@Override
	public boolean match(Object left) {
		DValue dval = (DValue) left;
		String fieldName = getFieldName();
		DValue fieldval = dval.asStruct().getField(fieldName);

		DValue resultVal = filterFnRunner.executeFilterFn(op1, fieldval);
		if (resultVal != null) {
			return inner.match(resultVal);
		}
		return true;
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
		//unused
	}
}