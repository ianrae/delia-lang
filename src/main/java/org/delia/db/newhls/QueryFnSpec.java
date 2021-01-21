package org.delia.db.newhls;

import org.delia.db.newhls.cond.FilterFunc;
import org.delia.type.DStructType;

/**
 * A function such as orderBy,count,first,min,etc
 * @author ian
 *
 */
public class QueryFnSpec {
	public StructFieldOpt structField; //who the func is being applied to. fieldName & fieldType can be null
	public FilterFunc filterFn;

	public boolean isFn(String fnName) {
		return filterFn.fnName.equals(fnName);
	}
	public String getFnName() {
		return filterFn.fnName;
	}
	
	public boolean isMatch(DStructType structType, String fieldName) {
		if (structType == structField.dtype && fieldName.equals(structField.fieldName)) {
			return true;
		}
		return false;
	}
	
	@Override
	public String toString() {
		String s = String.format("%s %s", structField == null ? "" : structField.toString(), filterFn.toString());
		return s;
	}
}