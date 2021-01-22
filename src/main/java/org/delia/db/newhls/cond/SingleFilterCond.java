package org.delia.db.newhls.cond;

import org.delia.db.hld.HLDQuery;
import org.delia.db.hld.ValType;

/**
 * If filter is single value then is query on pk
 * for [true], [16], [x], [myfunc(13)]
 * @author ian
 *
 */
public abstract class SingleFilterCond implements FilterCond {
	public FilterVal val1;

	@Override
	public String toString() {
		return val1.toString();
	}

	public abstract String renderSql();
	
	public boolean isAllQuery() {
		return val1.valType.equals(ValType.BOOLEAN);
	}
	public boolean isPKQuery() {
		return !val1.valType.equals(ValType.BOOLEAN);
	}
	
}