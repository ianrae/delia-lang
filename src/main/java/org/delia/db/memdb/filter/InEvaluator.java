package org.delia.db.memdb.filter;

import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.FilterOpFullExp;
import org.delia.compiler.ast.ListExp;
import org.delia.compiler.ast.QueryInExp;
import org.delia.type.DStructType;
import org.delia.type.DValue;

public class InEvaluator implements OpEvaluator {
	private FilterOpFullExp fullExp;
	private DStructType dtype;
	private QueryInExp inExp;
	private String keyField;
	
	public InEvaluator(FilterOpFullExp fullexp, DStructType dtype) {
		this.fullExp = fullexp;
		this.dtype = dtype;
		
		this.inExp = (QueryInExp) fullexp.opexp1;
		this.keyField = inExp.fieldName;
		if (this.keyField == null) {
			//err!!
//			wasError = true;
		}
		
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
		if (keyField == null) {
//			wasError = true;
			//err!!
			return false;
		} else {
			DValue dval = (DValue) left;
			DValue key = dval.asStruct().getField(keyField);
			if (key == null) {
//				wasError = true;
				//err!!
				return false;
			}

			if (isIn(key, inExp.listExp)) {
				return true;
			}
			return false;
		}
	}
	
	
	@Override
	public void setRightVar(Object rightVar) {
//		this.rightVar = rightVar;
	}

	@Override
	public void setNegFlag(boolean negFlag) {
		//not used
	}
	
	//moved from InSelector
	//TODO: do we also need to move varEvaluator??
	public boolean isIn(DValue key, ListExp listExp) {
		for(Exp exp: listExp.valueL) {
			if (doIsEqualTo(key, exp.strValue())) {
				return true;
			}
		}
		return false;
	}
	private boolean doIsEqualTo(DValue dval, Object target) {
		String tmp = dval.asString();
		if(tmp != null && tmp.equals(target)) {
			return true;
		}
		return false;
	}
	
}