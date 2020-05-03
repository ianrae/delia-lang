package org.delia.db.h2;

import java.sql.ResultSet;
import java.util.Map;

import org.delia.db.DBAccessContext;
import org.delia.db.DBExecutor;
import org.delia.db.InsertContext;
import org.delia.db.QueryContext;
import org.delia.db.QuerySpec;
import org.delia.runner.QueryResponse;
import org.delia.type.DValue;

public class H2DBExecutor implements DBExecutor {

	private H2DBInterface dbInterface;
	private DBAccessContext dbctx;

	public H2DBExecutor(H2DBInterface dbInterface, DBAccessContext ctx, H2DBConnection conn) {
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
	public int executeUpsert(QuerySpec spec, DValue dvalFull, Map<String, String> assocCrudMap, boolean noUpdateFlag) {
		return dbInterface.executeUpsert(spec, dvalFull, assocCrudMap, noUpdateFlag, dbctx);
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
		//information_schema is case-sensitive
		DBAccessContext tmp = dbctx.clone();
		tmp.disableSqlLogging = true;
		return dbInterface.doesTableExist(tableName.toUpperCase(), tmp);
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
	public boolean execFieldDetect(String tableName, String fieldName) {
		return dbInterface.doesFieldExist(tableName, fieldName, dbctx);
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
	
	//internal
	public void executeRawSql(String sql) {
		H2DBConnection conn = (H2DBConnection) dbctx.connObject;
		conn.executeRawSql(sql);
	}
	public ResultSet executeRawQuery(String sql) {
		H2DBConnection conn = (H2DBConnection) dbctx.connObject;
		return conn.execRawQuery(sql);
	}
}
