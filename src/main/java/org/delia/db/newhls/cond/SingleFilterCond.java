package org.delia.db.newhls.cond;

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
}