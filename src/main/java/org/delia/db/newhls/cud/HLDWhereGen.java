package org.delia.db.newhls.cud;

import java.util.List;

import org.delia.db.QuerySpec;
import org.delia.db.newhls.HLDAliasManager;
import org.delia.db.sql.fragment.SqlFragment;
import org.delia.db.sql.prepared.SqlStatement;
import org.delia.type.DStructType;

public interface HLDWhereGen {

	List<SqlFragment> createWhere(QuerySpec spec, DStructType structType, SqlStatement statement, HLDAliasManager aliasMgr);

}
