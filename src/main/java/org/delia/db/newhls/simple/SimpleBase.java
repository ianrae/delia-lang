package org.delia.db.newhls.simple;

import org.delia.db.newhls.HLDAliasBuilder;
import org.delia.db.newhls.SqlColumn;

public abstract class SimpleBase {
	public SqlColumn tblFrag;

	public abstract void assignAliases(HLDAliasBuilder aliasBuilder);
}