package org.delia.zdb.core;

import java.sql.ResultSet;

import org.delia.db.sql.prepared.SqlStatement;
import org.delia.type.DType;
import org.delia.type.DValue;

//low level access to db. execute statements
//get back void, int, resultSet
//Short-term object. need to call close()
public interface ZDBConnection {
	public void openDB();
	public void close();
	
	public ResultSet execQueryStatement(SqlStatement statement, ZDBExecuteContext dbctx);
	public void execStatement(SqlStatement statement, ZDBExecuteContext sqlctx);
	public int executeCommandStatement(SqlStatement statement, ZDBExecuteContext sqlctx);
	public DValue executeCommandStatementGenKey(SqlStatement statement, DType keyType, ZDBExecuteContext sqlctx);
	
	public void enumerateDBSchema(String sql, ZDBExecuteContext dbctx);
	public String findConstraint(String sql, String tableName, String fieldName, String constraintType);
}