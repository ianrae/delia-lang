package org.delia.db.sqlgen;

import org.delia.assoc.DatIdMap;
import org.delia.db.SqlStatement;

public interface SqlGeneratorFactory {

	SqlSelectStatement createSelect(DatIdMap datIdMap);

	SqlInsertStatement createInsert();

	SqlUpdateStatement createUpdate();

	SqlDeleteStatement createDelete();

	SqlMergeIntoStatement createMergeInto();
	SqlMergeAllIntoStatement createMergeAllInto();
	SqlMergeUsingStatement createMergeUsing();
	void useDeleteIn(SqlDeleteStatement delStmt);

	SqlConstraintStatement generateConstraint();
}