package org.delia.zdb;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.delia.db.SqlStatement;
import org.delia.type.DType;

public class DBConnectionObserverAdapter implements DBConnection {
	public List<SqlStatement> statements = new ArrayList<>();
	private DBConnection inner;
	private boolean enableObserver;

	public DBConnectionObserverAdapter(DBConnection inner) {
		this.inner = inner;
	}
	@Override
	public void openDB() {
		inner.openDB();
	}

	@Override
	public void close() {
		inner.close();
	}

	@Override
	public ResultSet execQueryStatement(SqlStatement statement, DBExecuteContext dbctx) {
		add(statement);
		return inner.execQueryStatement(statement, dbctx);
	}

	private void add(SqlStatement statement) {
		if (this.enableObserver) {
			statements.add(statement);
		}
	}
	@Override
	public void execStatement(SqlStatement statement, DBExecuteContext sqlctx) {
		add(statement);
		inner.execStatement(statement, sqlctx);
	}

	@Override
	public int executeCommandStatement(SqlStatement statement, DBExecuteContext sqlctx) {
		add(statement);
		return inner.executeCommandStatement(statement, sqlctx);
	}

	@Override
	public int executeCommandStatementGenKey(SqlStatement statement, DType keyType, DBExecuteContext sqlctx) {
		add(statement);
		return inner.executeCommandStatementGenKey(statement, keyType, sqlctx);
	}

	@Override
	public void enumerateDBSchema(String sql, String title, DBExecuteContext dbctx) {
		SqlStatement stm = new SqlStatement();
		stm.sql = sql;
		add(stm);
		inner.enumerateDBSchema(sql, title, dbctx);
	}

	@Override
	public String findConstraint(String sql, String tableName, String fieldName, String constraintType,
			boolean useFieldName) {
		SqlStatement stm = new SqlStatement();
		stm.sql = sql;
		add(stm);
		return inner.findConstraint(sql, tableName, fieldName, constraintType, useFieldName);
	}
	public boolean isEnableObserver() {
		return enableObserver;
	}
	public void setEnableObserver(boolean enableObserver) {
		this.enableObserver = enableObserver;
	}
	public void setStatementList(List<SqlStatement> statementList) {
		this.statements = statementList;
	}

}
