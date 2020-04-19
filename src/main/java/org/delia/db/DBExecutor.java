package org.delia.db;

import org.delia.runner.QueryResponse;
import org.delia.type.DValue;

/**
 * All database statements should be executed through here.
 * We manage jdbc connection inside this class.
 * @author Ian Rae
 *
 */
public interface DBExecutor {
	
	DValue executeInsert(DValue dval, InsertContext ctx);
	int executeUpdate(QuerySpec spec, DValue dvalPartial); 
	QueryResponse executeQuery(QuerySpec spec, QueryContext qtx);
	void executeDelete(QuerySpec spec);

	boolean execTableDetect(String tableName);
	boolean execFieldDetect(String tableName, String fieldName);
	void close();

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
