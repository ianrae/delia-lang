package org.delia.db.memdb;

import java.util.Map;

import org.delia.db.DBAccessContext;
import org.delia.db.DBExecutor;
import org.delia.db.DBInterface;
import org.delia.db.InsertContext;
import org.delia.db.QueryContext;
import org.delia.db.QuerySpec;
import org.delia.runner.QueryResponse;
import org.delia.type.DValue;

public class MemDBExecutor implements DBExecutor {

	private DBInterface dbInterface;
	private DBAccessContext dbctx;

	public MemDBExecutor(MemDBInterface memDBInterface, DBAccessContext ctx) {
		this.dbInterface = memDBInterface;
		this.dbctx = ctx;
	}
	public void forceDBInterface(DBInterface newOne) {
		this.dbInterface = newOne;
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
	public QueryResponse executeQuery(QuerySpec spec, QueryContext qtx) {
		return dbInterface.executeQuery(spec, qtx, dbctx);
	}

	@Override
	public void executeDelete(QuerySpec spec) {
		dbInterface.executeDelete(spec, dbctx);
	}

	@Override
	public boolean execTableDetect(String tableName) {
		return dbInterface.doesTableExist(tableName, dbctx);
	}

	@Override
	public void close() {
//		conn.close(); no conn with mem db
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
	public void renameField(String typeName, String field, String newName) {
		dbInterface.renameField(typeName, field, newName, dbctx);
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
