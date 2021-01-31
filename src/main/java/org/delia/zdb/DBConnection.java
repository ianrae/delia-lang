package org.delia.zdb;

import java.sql.ResultSet;

import org.delia.db.SqlStatement;
import org.delia.db.ValueHelper;
import org.delia.type.DType;

//low level access to db. execute statements
//get back void, int, resultSet
//Short-term object. need to call close()
public interface DBConnection {
	void openDB();
	void close();
	
	ResultSet execQueryStatement(SqlStatement statement, DBExecuteContext dbctx);
	void execStatement(SqlStatement statement, DBExecuteContext sqlctx);
	int executeCommandStatement(SqlStatement statement, DBExecuteContext sqlctx);
	int executeCommandStatementGenKey(SqlStatement statement, DType keyType, DBExecuteContext sqlctx);
	
	void enumerateDBSchema(String sql, String title, DBExecuteContext dbctx);
	String findConstraint(String sql, String tableName, String fieldName, String constraintType, boolean useFieldName);
	ValueHelper createValueHelper();
}