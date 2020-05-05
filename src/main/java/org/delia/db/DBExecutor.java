package org.delia.db;

import java.util.Map;

import org.delia.core.FactoryService;
import org.delia.runner.FetchRunner;
import org.delia.runner.QueryResponse;
import org.delia.runner.VarEvaluator;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;

/**
 * All database statements should be executed through here.
 * We manage jdbc connection inside this class.
 * @author Ian Rae
 *
 */
public interface DBExecutor extends AutoCloseable {
	
	DValue executeInsert(DValue dval, InsertContext ctx);
	int executeUpdate(QuerySpec spec, DValue dvalPartial, Map<String, String> assocCrudMap); 
	int executeUpsert(QuerySpec spec, DValue dvalFull, Map<String, String> assocCrudMap, boolean noUpdateFlag); 
	QueryResponse executeQuery(QuerySpec spec, QueryContext qtx);
	void executeDelete(QuerySpec spec);

	boolean execTableDetect(String tableName);
	boolean execFieldDetect(String tableName, String fieldName);
	
	FetchRunner createFetchRunner(FactoryService factorySvc);
	

	//schema actions
	void createTable(String tableName);
	void deleteTable(String tableName);
	void renameTable(String tableName, String newTableName);
	void createField(String typeName, String field);
	void deleteField(String typeName, String field);
	void renameField(String typeName, String field, String newName);
	void alterFieldType(String typeName, String fieldName, String newFieldType);
	void alterField(String typeName, String fieldName, String deltaFlags);
}
