package org.delia.db.newhls.simple;

import java.util.ArrayList;
import java.util.List;

import org.delia.db.newhls.SqlColumn;
import org.delia.db.newhls.cond.FilterCond;

/**
 * A simple SELECT statement (no joins, order by, etc)
 * @author ian
 *
 */
public class SimpleSelect extends SimpleBase {
	public List<SqlColumn> fieldL = new ArrayList<>();
	public FilterCond filter;
}