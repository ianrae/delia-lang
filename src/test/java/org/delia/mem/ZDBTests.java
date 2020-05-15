package org.delia.mem;

import static org.junit.Assert.assertEquals;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.delia.assoc.DatIdMap;
import org.delia.db.DBCapabilties;
import org.delia.db.DBType;
import org.delia.db.InsertContext;
import org.delia.db.QueryContext;
import org.delia.db.QuerySpec;
import org.delia.db.hls.HLSQueryStatement;
import org.delia.db.sql.prepared.SqlStatement;
import org.delia.log.Log;
import org.delia.runner.QueryResponse;
import org.delia.runner.VarEvaluator;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.type.TypeReplaceSpec;
import org.junit.Before;
import org.junit.Test;

public class ZDBTests  {
	
	public class ZDBExecuteContext  {
		public Log logToUse; //can be null
		
		//only for executeCommandStatementGenKey
		public List<ResultSet> genKeysL = new ArrayList<>();
	}
	
	//low level access to db. execute statements
	//get back void, int, resultSet
	//Short-term object. need to call close()
	public interface ZDBConnection {
		public void openDB();
		public void close();
		
		public ResultSet execQueryStatement(SqlStatement statement, ZDBExecuteContext dbctx);
		public void execStatement(SqlStatement statement, ZDBExecuteContext sqlctx);
		public int executeCommandStatement(SqlStatement statement, ZDBExecuteContext sqlctx);
		public DValue executeCommandStatementGenKey(SqlStatement statement, DType keyType, ZDBExecuteContext sqlctx);
		
		public void enumerateDBSchema(String sql, ZDBExecuteContext dbctx);
		public String findConstraint(String sql, String tableName, String fieldName, String constraintType);
	}
	
	//holds session data regarding db
	public class ZDBIContext {
		public Object connObject; //for internal use only
		public boolean disableSqlLogging; //for internal use only
	}
	
	public interface ZDBInterfaceFactory {
		DBType getDBType();
		DBCapabilties getCapabilities();
		
		ZDBConnection openConnection();
		
		boolean isSQLLoggingEnabled();
		void enableSQLLogging(boolean b);
	}
	
	public interface ZDBExecutor {
//		public boolean disableSqlLogging; //for internal use only
//		public Object connObject; //for internal use only
//		ZAccessContext getContext();
		ZDBConnection getDBConnection(); //for raw db access

		//executor holds session data regarding db
		void init1(DTypeRegistry registry);
		void init2(DatIdMap datIdMap, VarEvaluator varEvaluator);
		
		//these can be called after init1
		DValue rawInsert(DValue dval, InsertContext ctx);
		QueryResponse rawQuery(QuerySpec spec, QueryContext qtx);
		boolean rawTableDetect(String tableName);
		boolean rawFieldDetect(String tableName, String fieldName);
		void rawCreateTable(String tableName);
		void performTypeReplacement(TypeReplaceSpec spec);
		
		//these can ONLY be called after init2
		DValue executeInsert(DValue dval, InsertContext ctx);
		int executeUpdate(QuerySpec spec, DValue dvalPartial, Map<String, String> assocCrudMap); 
		int executeUpsert(QuerySpec spec, DValue dvalFull, Map<String, String> assocCrudMap, boolean noUpdateFlag); 
		void executeDelete(QuerySpec spec);
		QueryResponse executeHLSQuery(HLSQueryStatement hls, String sql, QueryContext qtx);
		
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
	}
	
	@Test
	public void testTool() {
		assertEquals(1,2);
	}
	
	// --

	@Before
	public void init() {
	}
	
}
