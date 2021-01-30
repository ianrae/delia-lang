package org.delia.hld.simple;

import java.util.ArrayList;
import java.util.List;

import org.delia.hld.HLDAliasBuilderAdapter;
import org.delia.hld.SqlColumn;
import org.delia.hld.cond.FilterCond;
import org.delia.hld.cud.HLDUpdate;

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
		this.outputAliases = false; //postgres doesn't like alias on UPDATE aliasBuilder.isOutputAliases();
		aliasBuilder.assignAliases(hld);
		for(SqlColumn column: fieldL) {
			column.alias = assign(hld.getMainAlias());
		}
		tblFrag.alias = assign(hld.getMainAlias());
	}
}