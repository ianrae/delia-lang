package org.delia.db;

import java.util.Map;

import org.delia.core.FactoryService;
import org.delia.db.hls.HLSQueryStatement;
import org.delia.runner.QueryResponse;
import org.delia.type.DValue;
import org.delia.type.TypeReplaceSpec;

/**
 * The main interface to the database.
 * Contains methods for CRUD and schema changes.
 * @author Ian Rae
 *
 */
public interface DBInterface {
	DBType getDBType();
	DBCapabilties getCapabilities();
	
	void init(FactoryService factorySvc);
	DBExecutor createExector(DBAccessContext ctx);
	
	DValue executeInsert(DValue dval, InsertContext ctx, DBAccessContext dbctx);
	int executeUpdate(QuerySpec spec, DValue dvalPartial, Map<String, String> assocCrudMap, DBAccessContext dbctx); 
	int executeUpsert(QuerySpec spec, DValue dvalFull, Map<String, String> assocCrudMap, boolean noUpdateFlag, DBAccessContext dbctx); 
	QueryResponse executeQuery(QuerySpec spec, QueryContext qtx, DBAccessContext dbctx);
	void executeDelete(QuerySpec spec, DBAccessContext dbctx);
	
	QueryResponse executeHLSQuery(HLSQueryStatement hls, String sql, QueryContext qtx, DBAccessContext dbctx);

	boolean isSQLLoggingEnabled();
	void enableSQLLogging(boolean b);

	//schema actions
	boolean doesTableExist(String tableName, DBAccessContext dbctx);
	boolean doesFieldExist(String tableName, String fieldName, DBAccessContext dbctx);
	void createTable(String tableName, DBAccessContext dbctx, SchemaContext ctx);
	void deleteTable(String tableName, DBAccessContext dbctx, SchemaContext ctx);
	void renameTable(String tableName, String newTableName, DBAccessContext dbctx, SchemaContext ctx);
	void createField(String typeName, String field, DBAccessContext dbctx, SchemaContext ctx);
	void deleteField(String typeName, String field, int datId, DBAccessContext dbctx, SchemaContext ctx);
	void renameField(String typeName, String fieldName, String newName, DBAccessContext dbctx, SchemaContext ctx);
	void alterFieldType(String typeName, String fieldName, String newFieldType, DBAccessContext dbctx, SchemaContext ctx);
	void alterField(String typeName, String fieldName, String deltaFlags, DBAccessContext dbctx, SchemaContext ctx);
	void performTypeReplacement(TypeReplaceSpec spec);
}