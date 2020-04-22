package org.delia.db;

import org.delia.core.FactoryService;
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
	int executeUpdate(QuerySpec spec, DValue dvalPartial, DBAccessContext dbctx); 
	QueryResponse executeQuery(QuerySpec spec, QueryContext qtx, DBAccessContext dbctx);
	void executeDelete(QuerySpec spec, DBAccessContext dbctx);

	boolean isSQLLoggingEnabled();
	void enableSQLLogging(boolean b);

	//schema actions
	boolean doesTableExist(String tableName, DBAccessContext dbctx);
	boolean doesFieldExist(String tableName, String fieldName, DBAccessContext dbctx);
	void createTable(String tableName, DBAccessContext dbctx);
	void deleteTable(String tableName, DBAccessContext dbctx);
	void renameTable(String tableName, String newTableName, DBAccessContext dbctx);
	void createField(String typeName, String field, DBAccessContext dbctx);
	void deleteField(String typeName, String field, DBAccessContext dbctx);
	void renameField(String typeName, String fieldName, String newName, DBAccessContext dbctx);
	void alterFieldType(String typeName, String fieldName, String newFieldType, DBAccessContext dbctx);
	void alterField(String typeName, String fieldName, String deltaFlags, DBAccessContext dbctx);
	void performTypeReplacement(TypeReplaceSpec spec);
}