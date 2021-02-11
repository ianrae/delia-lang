package org.delia.db.transaction;

import java.sql.ResultSet;

import org.delia.db.SqlStatement;
import org.delia.db.ValueHelper;
import org.delia.type.DType;
import org.delia.zdb.DBConnection;
import org.delia.zdb.DBConnectionInternal;
import org.delia.zdb.DBExecuteContext;

public class TransactionAwareDBConnection implements DBConnection {

	private DBConnection conn;

	public TransactionAwareDBConnection(DBConnection conn) {
		this.conn = conn;
	}
	
	@Override
	public void openDB() {
		conn.openDB();
	}

	@Override
	public void close() {
		//do nothing
	}
	
	public void actuallyClose() {
		conn.close();
	}
	public DBConnectionInternal getConnInternal() {
		return (DBConnectionInternal) conn;
	}

	@Override
	public ResultSet execQueryStatement(SqlStatement statement, DBExecuteContext dbctx) {
		return conn.execQueryStatement(statement, dbctx);
	}

	@Override
	public void execStatement(SqlStatement statement, DBExecuteContext sqlctx) {
		conn.execStatement(statement, sqlctx);
	}

	@Override
	public int executeCommandStatement(SqlStatement statement, DBExecuteContext sqlctx) {
		return conn.executeCommandStatement(statement, sqlctx);
	}

	@Override
	public int executeCommandStatementGenKey(SqlStatement statement, DType keyType, DBExecuteContext sqlctx) {
		return conn.executeCommandStatementGenKey(statement, keyType, sqlctx);
	}

	@Override
	public void enumerateDBSchema(String sql, String title, DBExecuteContext dbctx) {
		conn.enumerateDBSchema(sql, title, dbctx);
	}

	@Override
	public String findConstraint(String sql, String tableName, String fieldName, String constraintType,
			boolean useFieldName) {
		return conn.findConstraint(sql, tableName, fieldName, constraintType, useFieldName);
	}

	@Override
	public ValueHelper createValueHelper() {
		return conn.createValueHelper();
	}

}
