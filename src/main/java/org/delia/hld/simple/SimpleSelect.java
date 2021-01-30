package org.delia.hld.simple;

import java.util.ArrayList;
import java.util.List;

import org.delia.hld.HLDAliasBuilderAdapter;
import org.delia.hld.HLDQuery;
import org.delia.hld.SqlColumn;
import org.delia.hld.cond.FilterCond;

/**
 * A simple SELECT statement (no joins, order by, etc)
 * @author ian
 *
 */
public class SimpleSelect extends SimpleBase {
	public List<SqlColumn> fieldL = new ArrayList<>();
	public FilterCond filter;
	public HLDQuery hld; //for aliases
	
	@Override
	public void assignAliases(HLDAliasBuilderAdapter aliasBuilder) {
		this.outputAliases = true;
		aliasBuilder.assignAliases(hld);
		for(SqlColumn column: fieldL) {
			column.alias = assign(hld.fromAlias);
		}
		tblFrag.alias = assign(hld.fromAlias);
	}
	
}