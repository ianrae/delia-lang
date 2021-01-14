package org.delia.db.newhls.simple;

import org.delia.db.newhls.HLDAliasBuilderAdapter;
import org.delia.db.newhls.SqlColumn;

public abstract class SimpleBase {
	public SqlColumn tblFrag;

	public abstract void assignAliases(HLDAliasBuilderAdapter aliasBuilder);
}