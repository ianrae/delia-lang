package org.delia.db.postgres;

import java.util.Map;

import org.delia.db.DBAccessContext;
import org.delia.db.DBExecutor;
import org.delia.db.InsertContext;
import org.delia.db.QueryContext;
import org.delia.db.QuerySpec;
import org.delia.db.h2.H2DBConnection;
import org.delia.runner.QueryResponse;
import org.delia.type.DValue;

public class PostgresDBExecutor implements DBExecutor {

	private PostgresDBInterface dbInterface;
	private DBAccessContext dbctx;

	public PostgresDBExecutor(PostgresDBInterface dbInterface, DBAccessContext ctx, H2DBConnection conn) {
		this.dbInterface = dbInterface;
		this.dbctx = ctx;
		dbctx.connObject = conn;
	}
	public H2DBConnection getConn() {
		return (H2DBConnection) dbctx.connObject;
	}


	@Override
	public DValue executeInsert(DValue dval, InsertContext ctx) {
		return dbInterface.executeInsert(dval, ctx, dbctx);
	}

	@Override
	public int executeUpdate(QuerySpec spec, DValue dvalPartial, Map<String, String> assocCrudMap) {
		return dbInterface.executeUpdate(spec, dvalPartial, assocCrudMap, dbctx);
	}
	@Override
	public int executeUpsert(QuerySpec spec, DValue dvalFull, Map<String, String> assocCrudMap) {
		return dbInterface.executeUpsert(spec, dvalFull, assocCrudMap, dbctx);
	}

	@Override
	public QueryResponse executeQuery(QuerySpec spec, QueryContext qtx) {
		return dbInterface.executeQuery(spec, qtx, dbctx);
	}

	@Override
	public void executeDelete(QuerySpec spec) {
		dbInterface.executeDelete(spec, dbctx);
	}

	@Override
	public boolean execTableDetect(String tableName) {
		DBAccessContext tmp = dbctx.clone();
		tmp.disableSqlLogging = true;
		return dbInterface.doesTableExist(tableName, tmp);
	}
	@Override
	public boolean execFieldDetect(String tableName, String fieldName) {
		DBAccessContext tmp = dbctx.clone();
		tmp.disableSqlLogging = true;
		return dbInterface.doesFieldExist(tableName, fieldName, tmp);
	}

	@Override
	public void close() {
		H2DBConnection conn = (H2DBConnection) dbctx.connObject;
		conn.close();
	}

	@Override
	public void createTable(String tableName) {
		dbInterface.createTable(tableName, dbctx);
	}

	@Override
	public void deleteTable(String tableName) {
		dbInterface.deleteTable(tableName, dbctx);
	}

	@Override
	public void renameTable(String tableName, String newTableName) {
		dbInterface.renameTable(tableName, newTableName, dbctx);
	}

	@Override
	public void createField(String typeName, String field) {
		dbInterface.createField(typeName, field, dbctx);
	}

	@Override
	public void deleteField(String typeName, String field) {
		dbInterface.deleteField(typeName, field, dbctx);
	}
	@Override
	public void renameField(String typeName, String fieldName, String newName) {
		dbInterface.renameField(typeName, fieldName, newName, dbctx);
	}
	@Override
	public void alterFieldType(String typeName, String fieldName, String newFieldType) {
		dbInterface.alterFieldType(typeName, fieldName, newFieldType, dbctx);
	}
	@Override
	public void alterField(String typeName, String fieldName, String deltaFlags) {
		dbInterface.alterField(typeName, fieldName, deltaFlags, dbctx);
	}
}
