package org.delia.db.memdb;

import java.util.Map;

import org.delia.core.FactoryService;
import org.delia.db.DBAccessContext;
import org.delia.db.DBExecutor;
import org.delia.db.DBInterface;
import org.delia.db.InsertContext;
import org.delia.db.QueryContext;
import org.delia.db.QuerySpec;
import org.delia.db.SchemaContext;
import org.delia.db.TableExistenceService;
import org.delia.db.TableExistenceServiceImpl;
import org.delia.db.hls.HLSQueryStatement;
import org.delia.runner.FetchRunner;
import org.delia.runner.FetchRunnerImpl;
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
	public DBAccessContext getDBAccessContext() {
		return dbctx;
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
		return dbInterface.doesTableExist(tableName, dbctx);
	}

	//close is from AutoClosable
	@Override
	public void close() {
//		conn.close(); no conn with mem db
	}

	@Override
	public void createTable(String tableName, SchemaContext ctx) {
		dbInterface.createTable(tableName, dbctx, ctx);
	}

	@Override
	public void deleteTable(String tableName, SchemaContext ctx) {
		dbInterface.deleteTable(tableName, dbctx, ctx);
	}

	@Override
	public void renameTable(String tableName, String newTableName, SchemaContext ctx) {
		dbInterface.renameTable(tableName, newTableName, dbctx, ctx);
	}

	@Override
	public void createField(String typeName, String field, SchemaContext ctx) {
		dbInterface.createField(typeName, field, dbctx, ctx);
	}

	@Override
	public void deleteField(String typeName, String field, int datId, SchemaContext ctx) {
		dbInterface.deleteField(typeName, field, datId, dbctx, ctx);
	}
	@Override
	public boolean execFieldDetect(String tableName, String fieldName) {
		return dbInterface.doesFieldExist(tableName, fieldName, dbctx);
	}
	@Override
	public void renameField(String typeName, String field, String newName, SchemaContext ctx) {
		dbInterface.renameField(typeName, field, newName, dbctx, ctx);
	}
	@Override
	public void alterFieldType(String typeName, String fieldName, String newFieldType, SchemaContext ctx) {
		dbInterface.alterFieldType(typeName, fieldName, newFieldType, dbctx, ctx);
	}
	@Override
	public void alterField(String typeName, String fieldName, String deltaFlags, SchemaContext ctx) {
		dbInterface.alterField(typeName, fieldName, deltaFlags, dbctx, ctx);
	}

	@Override
	public FetchRunner createFetchRunner(FactoryService factorySvc) {
		return null; //TODOfixnew FetchRunnerImpl(factorySvc, this, dbctx.registry, dbctx.varEvaluator);
	}
	@Override
	public QueryResponse executeHLSQuery(HLSQueryStatement hls, String sql, QueryContext qtx) {
		return dbInterface.executeHLSQuery(hls, sql, qtx, dbctx);
	}
	@Override
	public TableExistenceService createTableExistService() {
		return null; //TODO fixnew TableExistenceServiceImpl(dbInterface, dbctx);
	}
}
