package org.delia.hld.simple;

import org.delia.hld.HLDAliasBuilderAdapter;
import org.delia.hld.SqlColumn;

public abstract class SimpleBase {
	public SqlColumn tblFrag;
	protected boolean outputAliases;

	public abstract void assignAliases(HLDAliasBuilderAdapter aliasBuilder);
	
	protected String assign(String alias) {
		return outputAliases ? alias : null;
	}

}