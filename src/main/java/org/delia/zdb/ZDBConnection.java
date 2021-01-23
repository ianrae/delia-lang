package org.delia.zdb;

import java.sql.ResultSet;

import org.delia.db.SqlStatement;
import org.delia.type.DType;

//low level access to db. execute statements
//get back void, int, resultSet
//Short-term object. need to call close()
public interface ZDBConnection {
	public void openDB();
	public void close();
	
	public ResultSet execQueryStatement(SqlStatement statement, ZDBExecuteContext dbctx);
	public void execStatement(SqlStatement statement, ZDBExecuteContext sqlctx);
	public int executeCommandStatement(SqlStatement statement, ZDBExecuteContext sqlctx);
	public int executeCommandStatementGenKey(SqlStatement statement, DType keyType, ZDBExecuteContext sqlctx);
	
	public void enumerateDBSchema(String sql, String title, ZDBExecuteContext dbctx);
	public String findConstraint(String sql, String tableName, String fieldName, String constraintType, boolean useFieldName);
}