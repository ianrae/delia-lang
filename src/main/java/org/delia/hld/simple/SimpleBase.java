package org.delia.hld.simple;

import org.delia.hld.HLDAliasBuilderAdapter;
import org.delia.hld.SqlColumn;

public abstract class SimpleBase {
	public SqlColumn tblFrag;

	public abstract void assignAliases(HLDAliasBuilderAdapter aliasBuilder);
}