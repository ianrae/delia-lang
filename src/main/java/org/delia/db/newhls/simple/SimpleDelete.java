package org.delia.db.newhls.simple;

import org.delia.db.newhls.HLDAliasBuilderAdapter;
import org.delia.db.newhls.cond.FilterCond;
import org.delia.db.newhls.cud.HLDDelete;

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