package org.delia.db;

import java.util.Map;

import org.delia.core.FactoryService;
import org.delia.db.hls.HLSQueryStatement;
import org.delia.db.memdb.MemDBExecutor;
import org.delia.db.memdb.MemDBInterface;
import org.delia.runner.QueryResponse;
import org.delia.type.DValue;
import org.delia.type.TypeReplaceSpec;

public class InstrumentedDBInterface implements DBInterface {
	public DBInterface actualInterface;
	public int insertCount; //TODO: atomic thread-safe int later
	public int updateCount;
	public int upsertCount;
	public int deleteCount;
	public int queryCount;
	
	public InstrumentedDBInterface(DBInterface actual) {
		this.actualInterface = actual;
	}

	@Override
	public DBCapabilties getCapabilities() {
		return actualInterface.getCapabilities();
	}
	
	@Override
	public void init(FactoryService factorySvc) {
		actualInterface.init(factorySvc);
	}

	@Override
	public DValue executeInsert(DValue dval, InsertContext ctx, DBAccessContext dbctx) {
		this.insertCount++;
		return actualInterface.executeInsert(dval, ctx, dbctx);
	}

	@Override
	public int executeUpdate(QuerySpec spec, DValue dvalPartial, Map<String, String> assocCrudMap, DBAccessContext dbctx) {
		this.updateCount++;
		return actualInterface.executeUpdate(spec, dvalPartial, assocCrudMap, dbctx);
	}
	@Override
	public int executeUpsert(QuerySpec spec, DValue dvalFull, Map<String, String> assocCrudMap, boolean noUpdateFlag, DBAccessContext dbctx) {
		this.upsertCount++;
		return actualInterface.executeUpsert(spec, dvalFull, assocCrudMap, noUpdateFlag, dbctx);
	}

	@Override
	public QueryResponse executeQuery(QuerySpec spec, QueryContext qtx, DBAccessContext dbctx) {
		this.queryCount++;
		return actualInterface.executeQuery(spec, qtx, dbctx);
	}

	@Override
	public void executeDelete(QuerySpec spec, DBAccessContext dbctx) {
		this.deleteCount++;
		actualInterface.executeDelete(spec, dbctx);
	}

	@Override
	public boolean doesTableExist(String tableName, DBAccessContext dbctx) {
		return actualInterface.doesTableExist(tableName, dbctx);
	}

	@Override
	public void createTable(String tableName, DBAccessContext dbctx, SchemaContext ctx) {
		actualInterface.createTable(tableName, dbctx, ctx);
	}

	@Override
	public void deleteTable(String tableName, DBAccessContext dbctx, SchemaContext ctx) {
		actualInterface.deleteTable(tableName, dbctx, ctx);
	}

	@Override
	public void renameTable(String tableName, String newTableName, DBAccessContext dbctx, SchemaContext ctx) {
		actualInterface.renameTable(tableName, newTableName, dbctx, ctx);
	}

	@Override
	public boolean isSQLLoggingEnabled() {
		return actualInterface.isSQLLoggingEnabled();
	}
	@Override
	public void enableSQLLogging(boolean b) {
		actualInterface.enableSQLLogging(b);
	}

	@Override
	public void createField(String typeName, String field, DBAccessContext dbctx, SchemaContext ctx) {
		actualInterface.createField(typeName, field, dbctx, ctx);
	}

	@Override
	public void deleteField(String typeName, String field, DBAccessContext dbctx, SchemaContext ctx) {
		actualInterface.deleteField(typeName, field, dbctx, ctx);
	}

	@Override
	public DBExecutor createExector(DBAccessContext ctx) {
		DBExecutor executor = actualInterface.createExector(ctx);
		if (actualInterface instanceof MemDBInterface) {
			MemDBExecutor memexecutor = (MemDBExecutor) executor;
			memexecutor.forceDBInterface(this);
		}
		return executor;
	}

	@Override
	public DBType getDBType() {
		return actualInterface.getDBType();
	}

	@Override
	public boolean doesFieldExist(String tableName, String fieldName, DBAccessContext dbctx) {
		return actualInterface.doesFieldExist(tableName, fieldName, dbctx);
	}

	@Override
	public void renameField(String typeName, String fieldName, String newName, DBAccessContext dbctx, SchemaContext ctx) {
		actualInterface.renameField(typeName, fieldName, newName, dbctx, ctx);
	}

	@Override
	public void alterFieldType(String typeName, String fieldName, String newFieldType, DBAccessContext dbctx, SchemaContext ctx) {
		actualInterface.alterFieldType(typeName, fieldName, newFieldType, dbctx, ctx);
	}

	@Override
	public void alterField(String typeName, String fieldName, String deltaFlags, DBAccessContext dbctx, SchemaContext ctx) {
		actualInterface.alterField(typeName, fieldName, deltaFlags, dbctx, ctx);
	}

	@Override
	public void performTypeReplacement(TypeReplaceSpec spec) {
		actualInterface.performTypeReplacement(spec);
	}

	@Override
	public QueryResponse executeHLSQuery(HLSQueryStatement hls, String sql, QueryContext qtx, DBAccessContext dbctx) {
		return actualInterface.executeHLSQuery(hls, sql, qtx, dbctx);
	}
}
