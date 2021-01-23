package org.delia.zdb;

import org.delia.assoc.DatIdMap;
import org.delia.db.InsertContext;
import org.delia.db.QueryContext;
import org.delia.db.sql.prepared.SqlStatement;
import org.delia.db.sql.prepared.SqlStatementGroup;
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

public class InstrumentedZDBExecutor implements ZDBExecutor {

	private ZDBExecutor zexec;

	public InstrumentedZDBExecutor(ZDBExecutor zexec) {
		this.zexec = zexec;
	}

	@Override
	public void close() throws Exception {
		zexec.close();
	}
	@Override
	public Log getLog() {
		return zexec.getLog();
	}

	@Override
	public ZDBConnection getDBConnection() {
		return zexec.getDBConnection();
	}

	@Override
	public void init1(DTypeRegistry registry) {
		zexec.init1(registry);
	}

	@Override
	public void init2(DatIdMap datIdMap, VarEvaluator varEvaluator) {
		zexec.init2(datIdMap, varEvaluator);
	}

	@Override
	public FetchRunner createFetchRunner() {
		return zexec.createFetchRunner();
	}

	@Override
	public DatIdMap getDatIdMap() {
		return zexec.getDatIdMap();
	}

	@Override
	public boolean rawTableDetect(String tableName) {
		return zexec.rawTableDetect(tableName);
	}

	@Override
	public boolean rawFieldDetect(String tableName, String fieldName) {
		return zexec.rawFieldDetect(tableName, fieldName);
	}

	@Override
	public void rawCreateTable(String tableName) {
		zexec.rawCreateTable(tableName);
	}

	@Override
	public DValue executeInsert(HLDInsertStatement hld, SqlStatementGroup stmgrp, InsertContext ctx) {
		return zexec.executeInsert(hld, stmgrp, ctx);
	}

	@Override
	public int executeUpdate(HLDUpdateStatement hld, SqlStatementGroup stmgrp) {
		return zexec.executeUpdate(hld, stmgrp);
	}

	@Override
	public int executeUpsert(HLDUpsertStatement hld, SqlStatementGroup stmgrp, boolean noUpdateFlag) {
		return zexec.executeUpsert(hld, stmgrp, noUpdateFlag);
	}

	@Override
	public void executeDelete(HLDDeleteStatement hld, SqlStatementGroup stmgrp) {
		zexec.executeDelete(hld, stmgrp);
	}

	@Override
	public QueryResponse executeHLDQuery(HLDQueryStatement hld, SqlStatementGroup stmgrp, QueryContext qtx) {
		return zexec.executeHLDQuery(hld, stmgrp, qtx);
	}

	@Override
	public boolean doesTableExist(String tableName) {
		return zexec.doesTableExist(tableName);
	}

	@Override
	public boolean doesFieldExist(String tableName, String fieldName) {
		return zexec.doesFieldExist(tableName, fieldName);
	}

	@Override
	public void createTable(String tableName) {
		zexec.createTable(tableName);
	}

	@Override
	public void deleteTable(String tableName) {
		zexec.deleteTable(tableName);		
	}

	@Override
	public void renameTable(String tableName, String newTableName) {
		zexec.renameTable(tableName, newTableName);
	}

	@Override
	public void createField(String typeName, String field) {
		zexec.createField(typeName, field);
	}

	@Override
	public void deleteField(String typeName, String field, int datId) {
		zexec.deleteField(typeName, field, datId);
	}

	@Override
	public void renameField(String typeName, String fieldName, String newName) {
		zexec.renameField(typeName, fieldName, newName);
	}

	@Override
	public void alterFieldType(String typeName, String fieldName, String newFieldType) {
		zexec.alterFieldType(typeName, fieldName, newFieldType);
	}

	@Override
	public void alterField(String typeName, String fieldName, String deltaFlags) {
		zexec.alterField(typeName, fieldName, deltaFlags);
	}

	@Override
	public ZDBInterfaceFactory getDbInterface() {
		return zexec.getDbInterface();
	}

	@Override
	public DValue rawInsert(SqlStatement stm, InsertContext ctx) {
		return zexec.rawInsert(stm, ctx);
	}
}