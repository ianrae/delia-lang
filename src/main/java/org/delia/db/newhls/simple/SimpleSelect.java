package org.delia.db.newhls.simple;

import java.util.ArrayList;
import java.util.List;

import org.delia.db.newhls.HLDAliasBuilderAdapter;
import org.delia.db.newhls.HLDQuery;
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
	public HLDQuery hld; //for aliases
	
	@Override
	public void assignAliases(HLDAliasBuilderAdapter aliasBuilder) {
		aliasBuilder.assignAliases(hld);
		for(SqlColumn column: fieldL) {
			column.alias = hld.fromAlias;
		}
		tblFrag.alias = hld.fromAlias;
	}
	
}