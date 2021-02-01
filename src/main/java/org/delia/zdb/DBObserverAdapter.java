package org.delia.zdb;

import java.util.ArrayList;
import java.util.List;

import org.delia.assoc.DatIdMap;
import org.delia.db.InsertContext;
import org.delia.db.QueryContext;
import org.delia.db.SqlStatement;
import org.delia.db.SqlStatementGroup;
import org.delia.db.schema.SchemaChangeAction;
import org.delia.hld.HLDFactory;
import org.delia.hld.HLDQueryStatement;
import org.delia.hld.cud.HLDDeleteStatement;
import org.delia.hld.cud.HLDInsertStatement;
import org.delia.hld.cud.HLDUpdateStatement;
import org.delia.hld.cud.HLDUpsertStatement;
import org.delia.log.Log;
import org.delia.runner.FetchRunner;
import org.delia.runner.QueryResponse;
import org.delia.runner.VarEvaluator;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;

public class DBObserverAdapter implements DBExecutor {
	
	private List<SqlStatement> statements = new ArrayList<>();
	private DBExecutor inner;
	private boolean ignoreSimpleSvcSql = true; //ignore delia's internal db queries
	
	public DBObserverAdapter(DBExecutor inner, boolean ignoreSimpleSvcSql) {
		this.inner = inner;
		this.ignoreSimpleSvcSql = ignoreSimpleSvcSql;
	}
	
	public List<SqlStatement> getStatementList() {
		return statements;
	}

	@Override
	public void close() throws Exception {
		inner.close();
	}

	@Override
	public DBConnection getDBConnection() {
		return inner.getDBConnection();
	}

	@Override
	public Log getLog() {
		return inner.getLog();
	}

	@Override
	public void init1(DTypeRegistry registry) {
		inner.init1(registry);
	}

	@Override
	public void init2(DatIdMap datIdMap, VarEvaluator varEvaluator) {
		inner.init2(datIdMap, varEvaluator);
	}

	@Override
	public FetchRunner createFetchRunner() {
		return inner.createFetchRunner();
	}

	@Override
	public DatIdMap getDatIdMap() {
		return inner.getDatIdMap();
	}

	@Override
	public DValue rawInsert(SqlStatement stm, InsertContext ctx) {
		if (!ignoreSimpleSvcSql) {
			statements.add(stm);
		}
		return inner.rawInsert(stm, ctx);
	}

	@Override
	public boolean rawTableDetect(String tableName) {
		return inner.rawTableDetect(tableName);
	}

	@Override
	public boolean rawFieldDetect(String tableName, String fieldName) {
		return inner.rawFieldDetect(tableName, fieldName);
	}

	@Override
	public void rawCreateTable(String tableName) {
		//TODO: should we add this to statements somehow?
		inner.rawCreateTable(tableName);
	}

	@Override
	public QueryResponse executeHLDQuery(HLDQueryStatement hld, SqlStatementGroup stmgrp, QueryContext qtx) {
		if (!qtx.isSimpleSvc || !ignoreSimpleSvcSql) {
			add(stmgrp);
		}
		return inner.executeHLDQuery(hld, stmgrp, qtx);
	}

	private void add(SqlStatementGroup stmgrp) {
		for(SqlStatement stm: stmgrp.statementL) {
			statements.add(stm);
		}
	}

	@Override
	public DValue executeInsert(HLDInsertStatement hld, SqlStatementGroup stmgrp, InsertContext ctx) {
		add(stmgrp);
		return inner.executeInsert(hld, stmgrp, ctx);
	}

	@Override
	public int executeUpdate(HLDUpdateStatement hld, SqlStatementGroup stmgrp) {
		add(stmgrp);
		return inner.executeUpdate(hld, stmgrp);
	}

	@Override
	public int executeUpsert(HLDUpsertStatement hld, SqlStatementGroup stmgrp, boolean noUpdateFlag) {
		add(stmgrp);
		return inner.executeUpsert(hld, stmgrp, noUpdateFlag);
	}

	@Override
	public void executeDelete(HLDDeleteStatement hld, SqlStatementGroup stmgrp) {
		add(stmgrp);
		inner.executeDelete(hld, stmgrp);
	}

	@Override
	public boolean doesTableExist(String tableName) {
		return inner.doesTableExist(tableName);
	}

	@Override
	public boolean doesFieldExist(String tableName, String fieldName) {
		return inner.doesFieldExist(tableName, fieldName);
	}

	@Override
	public void createTable(String tableName) {
		inner.createTable(tableName);
	}

	@Override
	public void deleteTable(String tableName) {
		inner.deleteTable(tableName);
	}

	@Override
	public void renameTable(String tableName, String newTableName) {
		inner.renameTable(tableName, newTableName);
	}

	@Override
	public void createField(String typeName, String field, int sizeof) {
		inner.createField(typeName, field, sizeof);
	}

	@Override
	public void deleteField(String typeName, String field, int datId) {
		inner.deleteField(typeName, field, datId);
	}

	@Override
	public void renameField(String typeName, String fieldName, String newName) {
		inner.renameField(typeName, fieldName, newName);
	}

	@Override
	public void alterFieldType(String typeName, String fieldName, String newFieldType, int sizeof) {
		inner.alterFieldType(typeName, fieldName, newFieldType, sizeof);
	}

	@Override
	public void alterField(String typeName, String fieldName, String deltaFlags) {
		inner.alterField(typeName, fieldName, deltaFlags);
	}

	@Override
	public DBInterfaceFactory getDbInterface() {
		return inner.getDbInterface();
	}

	public void setInner(DBExecutor inner) {
		this.inner = inner;
	}

	public boolean isIgnoreSimpleSvcSql() {
		return ignoreSimpleSvcSql;
	}

	public void setIgnoreSimpleSvcSql(boolean ignoreSimpleSvcSql) {
		this.ignoreSimpleSvcSql = ignoreSimpleSvcSql;
	}

	@Override
	public HLDFactory getHLDFactory() {
		return inner.getHLDFactory();
	}

	@Override
	public void performSchemaChangeAction(SchemaChangeAction action) {
		inner.performSchemaChangeAction(action);
	}
}
