package org.delia.zdb;

import org.delia.assoc.DatIdMap;
import org.delia.db.InsertContext;
import org.delia.db.QueryContext;
import org.delia.db.SqlStatement;
import org.delia.db.SqlStatementGroup;
import org.delia.db.schema.SchemaChangeAction;
import org.delia.db.schema.modify.SchemaChangeOperation;
import org.delia.hld.HLDFactory;
import org.delia.hld.HLDQueryStatement;
import org.delia.hld.cud.HLDDeleteStatement;
import org.delia.hld.cud.HLDInsertStatement;
import org.delia.hld.cud.HLDUpdateStatement;
import org.delia.hld.cud.HLDUpsertStatement;
import org.delia.log.Log;
import org.delia.runner.FetchRunner;
import org.delia.runner.QueryResponse;
import org.delia.runner.VarEvaluator;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;

public interface DBExecutor extends AutoCloseable {
	DBConnection getDBConnection(); //for raw db access
	Log getLog();

	//executor holds session data regarding db
	void init1(DTypeRegistry registry);
	void init2(DatIdMap datIdMap, VarEvaluator varEvaluator);
	String getDefaultSchema();
	void setDefaultSchema(String schema);
	FetchRunner createFetchRunner();
	DatIdMap getDatIdMap();

	//these can be called after init1
	DValue rawInsert(SqlStatement stm, InsertContext ctx);
	boolean rawTableDetect(String tableName);
	boolean rawFieldDetect(String tableName, String fieldName);
	void rawCreateTable(String tableName);

	//these can ONLY be called after init2
	QueryResponse executeHLDQuery(HLDQueryStatement hld, SqlStatementGroup stmgrp, QueryContext qtx);
	DValue executeInsert(HLDInsertStatement hld, SqlStatementGroup stmgrp, InsertContext ctx);
	int executeUpdate(HLDUpdateStatement hld, SqlStatementGroup stmgrp); 
	int executeUpsert(HLDUpsertStatement hld, SqlStatementGroup stmgrp, boolean noUpdateFlag); 
	void executeDelete(HLDDeleteStatement hld, SqlStatementGroup stmgrp);

	//schema actions (only be called after init2)
	boolean doesTableExist(String tableName);
	boolean doesFieldExist(String tableName, String fieldName);
	void createTable(String tableName);
	void deleteTable(String tableName);
	void renameTable(String tableName, String newTableName);
	void createField(String typeName, String field, int sizeof);
	void deleteField(String typeName, String field, int datId);
	void renameField(String typeName, String fieldName, String newName);
	void alterFieldType(String typeName, String fieldName, String newFieldType, int sizeof);
	void alterField(String typeName, String fieldName, String deltaFlags);
	void performSchemaChangeAction(SchemaChangeAction action);
	void executeSchemaChangeOperation(SchemaChangeOperation op);

	DBInterfaceFactory getDbInterface();
	HLDFactory getHLDFactory();
}