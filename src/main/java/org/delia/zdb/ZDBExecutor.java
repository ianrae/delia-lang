package org.delia.zdb;

import org.delia.assoc.DatIdMap;
import org.delia.db.InsertContext;
import org.delia.db.QueryContext;
import org.delia.db.QuerySpec;
import org.delia.db.hld.HLDQueryStatement;
import org.delia.db.hld.cud.HLDDeleteStatement;
import org.delia.db.hld.cud.HLDInsertStatement;
import org.delia.db.hld.cud.HLDUpdateStatement;
import org.delia.db.hld.cud.HLDUpsertStatement;
import org.delia.db.sql.prepared.SqlStatementGroup;
import org.delia.log.Log;
import org.delia.runner.FetchRunner;
import org.delia.runner.QueryResponse;
import org.delia.runner.VarEvaluator;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;

public interface ZDBExecutor extends AutoCloseable {
		ZDBConnection getDBConnection(); //for raw db access
		Log getLog();

		//executor holds session data regarding db
		void init1(DTypeRegistry registry);
		void init2(DatIdMap datIdMap, VarEvaluator varEvaluator);
		FetchRunner createFetchRunner();
		DatIdMap getDatIdMap();
		
		//these can be called after init1
		DValue rawInsert(DValue dval, InsertContext ctx);
//		QueryResponse rawQuery(QuerySpec spec, QueryContext qtx);
		boolean rawTableDetect(String tableName);
		boolean rawFieldDetect(String tableName, String fieldName);
		void rawCreateTable(String tableName);
		
		//these can ONLY be called after init2
//		DValue executeInsert(DValue dval, InsertContext ctx);
//		int executeUpdate(QuerySpec spec, DValue dvalPartial, Map<String, String> assocCrudMap); 
//		int executeUpsert(QuerySpec spec, DValue dvalFull, Map<String, String> assocCrudMap, boolean noUpdateFlag); 
//		void executeDelete(QuerySpec spec);
//		QueryResponse executeHLSQuery(HLSQueryStatement hls, String sql, QueryContext qtx);
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
		void createField(String typeName, String field);
		void deleteField(String typeName, String field, int datId);
		void renameField(String typeName, String fieldName, String newName);
		void alterFieldType(String typeName, String fieldName, String newFieldType);
		void alterField(String typeName, String fieldName, String deltaFlags);

		ZDBInterfaceFactory getDbInterface();
	}