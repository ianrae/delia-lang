package org.delia.zdb;

import java.sql.ResultSet;

import org.delia.db.SqlStatement;
import org.delia.type.DType;

//low level access to db. execute statements
//get back void, int, resultSet
//Short-term object. need to call close()
public interface DBConnection {
	public void openDB();
	public void close();
	
	public ResultSet execQueryStatement(SqlStatement statement, DBExecuteContext dbctx);
	public void execStatement(SqlStatement statement, DBExecuteContext sqlctx);
	public int executeCommandStatement(SqlStatement statement, DBExecuteContext sqlctx);
	public int executeCommandStatementGenKey(SqlStatement statement, DType keyType, DBExecuteContext sqlctx);
	
	public void enumerateDBSchema(String sql, String title, DBExecuteContext dbctx);
	public String findConstraint(String sql, String tableName, String fieldName, String constraintType, boolean useFieldName);
}