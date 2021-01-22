package org.delia.db.newhls.simple;

import org.delia.db.hld.HLDAliasBuilderAdapter;
import org.delia.db.hld.SqlColumn;

public abstract class SimpleBase {
	public SqlColumn tblFrag;

	public abstract void assignAliases(HLDAliasBuilderAdapter aliasBuilder);
}