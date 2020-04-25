package org.delia.db.sql.fragment;

import org.delia.type.DStructType;

public interface TableFragmentMaker {

	TableFragment createTable(DStructType structType, SelectStatementFragment selectFrag);
	TableFragment createAssocTable(SelectStatementFragment selectFrag, String tableName);

}