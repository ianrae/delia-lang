package org.delia.db;

import java.util.Map;

import org.delia.core.FactoryService;
import org.delia.db.hls.HLSQueryStatement;
import org.delia.runner.FetchRunner;
import org.delia.runner.QueryResponse;
import org.delia.type.DValue;

/**
 * All database statements should be executed through here.
 * We manage jdbc connection inside this class.
 * @author Ian Rae
 *
 */
public interface DBExecutor extends AutoCloseable {
	
	DBAccessContext getDBAccessContext();
	DValue executeInsert(DValue dval, InsertContext ctx);
	int executeUpdate(QuerySpec spec, DValue dvalPartial, Map<String, String> assocCrudMap); 
	int executeUpsert(QuerySpec spec, DValue dvalFull, Map<String, String> assocCrudMap, boolean noUpdateFlag); 
	QueryResponse executeQuery(QuerySpec spec, QueryContext qtx);
	void executeDelete(QuerySpec spec);
	
	//hls layer
	QueryResponse executeHLSQuery(HLSQueryStatement hls, String sql, QueryContext qtx);

	boolean execTableDetect(String tableName);
	boolean execFieldDetect(String tableName, String fieldName);
	
	FetchRunner createFetchRunner(FactoryService factorySvc);
	TableExistenceService createTableExistService();

	//schema actions
	void createTable(String tableName, SchemaContext ctx);
	void deleteTable(String tableName, SchemaContext ctx);
	void renameTable(String tableName, String newTableName, SchemaContext ctx);
	void createField(String typeName, String field, SchemaContext ctx);
	void deleteField(String typeName, String field, int datId, SchemaContext ctx);
	void renameField(String typeName, String field, String newName, SchemaContext ctx);
	void alterFieldType(String typeName, String fieldName, String newFieldType, SchemaContext ctx);
	void alterField(String typeName, String fieldName, String deltaFlags, SchemaContext ctx);
}
