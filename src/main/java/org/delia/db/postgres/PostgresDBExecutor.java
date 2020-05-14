package org.delia.db.postgres;

import java.util.Map;

import org.delia.core.FactoryService;
import org.delia.db.DBAccessContext;
import org.delia.db.DBExecutor;
import org.delia.db.InsertContext;
import org.delia.db.QueryContext;
import org.delia.db.QuerySpec;
import org.delia.db.SchemaContext;
import org.delia.db.TableExistenceService;
import org.delia.db.TableExistenceServiceImpl;
import org.delia.db.h2.H2DBConnection;
import org.delia.db.hls.HLSQueryStatement;
import org.delia.runner.FetchRunner;
import org.delia.runner.FetchRunnerImpl;
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
	public void createTable(String tableName, SchemaContext ctx) {
		dbInterface.createTable(tableName, dbctx);
	}

	@Override
	public void deleteTable(String tableName, SchemaContext ctx) {
		dbInterface.deleteTable(tableName, dbctx);
	}

	@Override
	public void renameTable(String tableName, String newTableName, SchemaContext ctx) {
		dbInterface.renameTable(tableName, newTableName, dbctx);
	}

	@Override
	public void createField(String typeName, String field, SchemaContext ctx) {
		dbInterface.createField(typeName, field, dbctx);
	}

	@Override
	public void deleteField(String typeName, String field, SchemaContext ctx) {
		dbInterface.deleteField(typeName, field, dbctx);
	}
	@Override
	public void renameField(String typeName, String fieldName, String newName, SchemaContext ctx) {
		dbInterface.renameField(typeName, fieldName, newName, dbctx);
	}
	@Override
	public void alterFieldType(String typeName, String fieldName, String newFieldType, SchemaContext ctx) {
		dbInterface.alterFieldType(typeName, fieldName, newFieldType, dbctx);
	}
	@Override
	public void alterField(String typeName, String fieldName, String deltaFlags, SchemaContext ctx) {
		dbInterface.alterField(typeName, fieldName, deltaFlags, dbctx);
	}

	@Override
	public FetchRunner createFetchRunner(FactoryService factorySvc) {
		return new FetchRunnerImpl(factorySvc, this, dbctx.registry, dbctx.varEvaluator);
	}
	@Override
	public QueryResponse executeHLSQuery(HLSQueryStatement hls, String sql, QueryContext qtx) {
		return dbInterface.executeHLSQuery(hls, sql, qtx, dbctx);
	}
	@Override
	public TableExistenceService createTableExistService() {
		return new TableExistenceServiceImpl(dbInterface, dbctx);
	}
}
