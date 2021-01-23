package org.delia.hld.simple;

import org.delia.hld.HLDAliasBuilderAdapter;
import org.delia.hld.cond.FilterCond;
import org.delia.hld.cud.HLDDelete;

/**
 * A simple DELETE statement.
 * @author ian
 *
 */
public class SimpleDelete extends SimpleBase {
	public FilterCond filter;
	HLDDelete hld; //for aliases
	
	@Override
	public void assignAliases(HLDAliasBuilderAdapter aliasBuilder) {
		aliasBuilder.assignAliases(hld);
		tblFrag.alias = hld.getMainAlias();
	}
	
}