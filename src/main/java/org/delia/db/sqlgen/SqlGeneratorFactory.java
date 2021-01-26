package org.delia.db.sqlgen;

import org.delia.assoc.DatIdMap;

public interface SqlGeneratorFactory {

	SqlSelectStatement createSelect(DatIdMap datIdMap);

	SqlInsertStatement createInsert();

	SqlUpdateStatement createUpdate();

	SqlDeleteStatement createDelete();

	SqlMergeIntoStatement createMergeInto();
	SqlMergeAllIntoStatement createMergeAllInto();
	SqlMergeUsingStatement createMergeUsing();
	void useDeleteIn(SqlDeleteStatement delStmt);

}