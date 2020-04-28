package org.delia.db.sql.fragment;

import org.delia.type.DStructType;

public interface TableFragmentMaker {

	TableFragment createTable(DStructType structType, StatementFragmentBase selectFrag);
	TableFragment createAssocTable(StatementFragmentBase selectFrag, String tableName);

}