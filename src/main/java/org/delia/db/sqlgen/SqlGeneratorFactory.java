package org.delia.db.sqlgen;

public class SqlGeneratorFactory {
	public SqlMergeIntoStatement createMergeInto() {
		return new SqlMergeIntoStatement(new SqlTableNameClause(), new SqlValueListClause());
	}
}
