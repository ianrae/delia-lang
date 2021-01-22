package org.delia.db.hld.simple;

import java.util.ArrayList;
import java.util.List;

import org.delia.db.hld.HLDAliasBuilderAdapter;
import org.delia.db.hld.SqlColumn;
import org.delia.db.hld.cud.HLDUpdate;
import org.delia.db.newhls.cond.FilterCond;

/**
 * A simple UPDATE statement.
 * @author ian
 *
 */
public class SimpleUpdate extends SimpleBase {
	public List<SqlColumn> fieldL = new ArrayList<>();
	public FilterCond filter;
	public HLDUpdate hld; //for aliases
	
	@Override
	public void assignAliases(HLDAliasBuilderAdapter aliasBuilder) {
		aliasBuilder.assignAliases(hld);
		for(SqlColumn column: fieldL) {
			column.alias = hld.getMainAlias();
		}
		tblFrag.alias = hld.getMainAlias();
	}

}