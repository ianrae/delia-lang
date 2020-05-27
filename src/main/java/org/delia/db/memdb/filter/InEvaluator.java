package org.delia.db.memdb.filter;

import java.util.List;

import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.FilterOpFullExp;
import org.delia.compiler.ast.ListExp;
import org.delia.compiler.ast.QueryInExp;
import org.delia.runner.FilterEvaluator;
import org.delia.type.DRelation;
import org.delia.type.DStructType;
import org.delia.type.DValue;

public class InEvaluator implements OpEvaluator {
	private FilterOpFullExp fullExp;
	private DStructType dtype;
	private QueryInExp inExp;
	private String keyField;
	private FilterEvaluator filterEvaluator;
	
	public InEvaluator(FilterOpFullExp fullexp, DStructType dtype, FilterEvaluator filterEvaluator) {
		this.fullExp = fullexp;
		this.dtype = dtype;
		this.filterEvaluator = filterEvaluator;
		
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
				//try as var
				List<DValue> varValueL = filterEvaluator.lookupVar(keyField);
				if (varValueL != null) {
					for(DValue vv: varValueL) {
						if (isIn(vv, inExp.listExp)) {
							return true;
						}
					}
					return false;
				}
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
		String tmp;
		if (dval.getType().isRelationShape()) {
			DRelation drel = dval.asRelation();
			tmp = drel.getForeignKey().asString(); //TODO later support multiple keys
		} else {
			tmp = dval.asString();
		}
		if(tmp != null && tmp.equals(target)) {
			return true;
		}
		return false;
	}
	
}