package org.delia.db.newhls;

import org.delia.db.newhls.cond.FilterFunc;

/**
 * A function such as orderBy,count,first,min,etc
 * @author ian
 *
 */
public class QueryFnSpec {
	public StructField structField; //who the func is being applied to. fieldName & fieldType can be null
	public FilterFunc filterFn;

	@Override
	public String toString() {
		String s = String.format("%s %s", structField == null ? "" : structField.toString(), filterFn.toString());
		return s;
	}
}