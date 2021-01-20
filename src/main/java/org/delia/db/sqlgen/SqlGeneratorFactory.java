package org.delia.db.sqlgen;

public class SqlGeneratorFactory {
	
	public SqlInsertStatement createInsert() {
		return new SqlInsertStatement(new SqlTableNameClause(), new SqlFieldListClause(), new SqlValueListClause());
	}
	public SqlMergeIntoStatement createMergeInto() {
		return new SqlMergeIntoStatement(new SqlTableNameClause(), new SqlValueListClause());
	}
	public SqlMergeUsingStatement createMergeUsing() {
		return new SqlMergeUsingStatement(new SqlTableNameClause(), new SqlFieldListClause(), new SqlValueListClause());
	}
}
