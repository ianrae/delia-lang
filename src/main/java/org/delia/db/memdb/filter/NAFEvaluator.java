package org.delia.db.memdb.filter;

import org.delia.compiler.astx.XNAFMultiExp;
import org.delia.compiler.astx.XNAFSingleExp;
import org.delia.db.memdb.filter.filterfn.FilterFnRunner;
import org.delia.type.DRelation;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.util.DeliaExceptionHelper;
import org.delia.zdb.mem.ImplicitFetchContext;
import org.delia.zdb.mem.RelationFetchHelper;

public class NAFEvaluator implements OpEvaluator {
	private XNAFMultiExp op1;
	private OpEvaluator inner;
	private DTypeRegistry registry;
	private FilterFnRunner filterFnRunner;
	private boolean negFlag;
	private ImplicitFetchContext implicitCtx;

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
//				DeliaExceptionHelper.throwError("implicit-fetch-needed", "Filter containing %s.%s needs in implicit fetch. This is a bug!", dval.getType().getName(), fieldName);
			DRelation drel = fieldval.asRelation();
			if (!drel.haveFetched()) {
				RelationFetchHelper helper = new RelationFetchHelper(registry, implicitCtx.fetchRunner);
				helper.fetchParentSide(dval, fieldName);
				fieldval = dval.asStruct().getField(fieldName);
				drel = fieldval.asRelation(); //is new drel object
				XNAFSingleExp sexp = op1.qfeL.get(1);
				String subFieldName = sexp.funcName;
				
				//Note. this works for addr.city (one deep) but not any deeper
				//TODO: support deeper, such as addr.country.code
				for(DValue fetchedVal: drel.getFetchedItems()) {
					DValue targetval = fetchedVal.asStruct().getField(subFieldName);
					if (inner.match(targetval)) {
						return true;
					}
				}
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

	public void setImplicitContext(ImplicitFetchContext implicitCtx) {
		this.implicitCtx = implicitCtx;
	}
}