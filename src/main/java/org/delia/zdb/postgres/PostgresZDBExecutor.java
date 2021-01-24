package org.delia.zdb.postgres;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.delia.assoc.DatIdMap;
import org.delia.core.FactoryService;
import org.delia.db.DBAccessContext;
import org.delia.db.DBType;
import org.delia.db.DBValidationException;
import org.delia.db.InsertContext;
import org.delia.db.QueryContext;
import org.delia.db.QueryDetails;
import org.delia.db.RawStatementGenerator;
import org.delia.db.SqlExecuteContext;
import org.delia.db.SqlStatement;
import org.delia.db.SqlStatementGroup;
import org.delia.db.ValueHelper;
import org.delia.db.h2.DBListingType;
import org.delia.db.hls.ResultTypeInfo;
import org.delia.db.postgres.PostgresFieldgenFactory;
import org.delia.db.sql.SqlNameFormatter;
import org.delia.db.sql.table.FieldGenFactory;
import org.delia.hld.HLDQueryStatement;
import org.delia.hld.cud.HLDDeleteStatement;
import org.delia.hld.cud.HLDInsertStatement;
import org.delia.hld.cud.HLDUpdateStatement;
import org.delia.hld.cud.HLDUpsertStatement;
import org.delia.hld.results.HLDResultSetConverter;
import org.delia.hld.results.HLDSelectHelper;
import org.delia.log.Log;
import org.delia.runner.DoNothingVarEvaluator;
import org.delia.runner.FetchRunner;
import org.delia.runner.QueryResponse;
import org.delia.runner.VarEvaluator;
import org.delia.runner.ZFetchRunnerImpl;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.util.DeliaExceptionHelper;
import org.delia.zdb.DBConnection;
import org.delia.zdb.DBExecuteContext;
import org.delia.zdb.DBExecutor;
import org.delia.zdb.DBInterfaceFactory;
import org.delia.zdb.TableCreator;
import org.delia.zdb.h2.H2DeliaSessionCache.CacheData;
import org.delia.zdb.h2.DBExecutorBase;

public class PostgresZDBExecutor extends DBExecutorBase implements DBExecutor {

	private PostgresZDBInterfaceFactory dbInterface;
	private PostgresZDBConnection conn;
//	private PostgresZInsert zinsert;
//	private PostgresZQuery zquery;
//	private PostgresZUpdate zupdate;
//	private PostgresZUpsert zupsert;
//	private PostgresZDelete zdelete;
	private PostgresDeliaSessionCache cache;
	private CacheData cacheData;

	public PostgresZDBExecutor(FactoryService factorySvc, Log sqlLog, PostgresZDBInterfaceFactory dbInterface, 
			PostgresZDBConnection conn, PostgresDeliaSessionCache sessionCache) {
		super(factorySvc, sqlLog, dbInterface.getErrorConverter());
		this.dbInterface = dbInterface;
		this.conn = conn;
		this.dbType = DBType.POSTGRES;
		this.cache = sessionCache;
	}

	@Override
	public DBConnection getDBConnection() {
		return conn;
	}

	@Override
	public void close() {
		conn.close();
	}
	@Override
	public Log getLog() {
		return sqlLog;
	}

	@Override
	public void init1(DTypeRegistry registry) {
		super.init1(registry);
//		this.zinsert = new PostgresZInsert(factorySvc, registry);
//		this.zquery = new PostgresZQuery(factorySvc, registry);
//		this.zupdate = new PostgresZUpdate(factorySvc, registry);
//		this.zupsert = new PostgresZUpsert(factorySvc, registry, dbInterface);
//		this.zdelete = new PostgresZDelete(factorySvc, registry);
		this.cacheData = cache.findOrCreate(registry); //registry persists across a DeliaSession
	}

	private TableCreator createPartialTableCreator() {
		return super.createPartialTableCreator(this);
	}
	
	@Override
	protected TableCreator createZTableCreator(FieldGenFactory fieldGenFactory, SqlNameFormatter nameFormatter, DatIdMap datIdMap, DBExecutor zexec) {
		return  new PostgresZTableCreator(factorySvc, registry, fieldGenFactory, nameFormatter, datIdMap, zexec);
	}
	@Override
	protected FieldGenFactory createFieldGenFactory() {
		return new PostgresFieldgenFactory(factorySvc);
	}

	@Override
	public void init2(DatIdMap datIdMap, VarEvaluator varEvaluator) {
		super.init2(datIdMap, varEvaluator, this);
	}

	@Override
	public FetchRunner createFetchRunner() {
		return new ZFetchRunnerImpl(factorySvc, this, registry, varEvaluator);
	}

	@Override
	public DValue rawInsert(SqlStatement stm, InsertContext ctx) {
		SqlStatementGroup stgroup = new SqlStatementGroup(stm);
		if (ctx.extractGeneratedKeys) {
			return doInsert(stgroup, ctx);
		} else {
			doInsert(stgroup, ctx);
			return null;
		}
	}

	private DValue doInsert(SqlStatementGroup stgroup, InsertContext ctx) {
		logStatementGroup(stgroup);
		DType keyType = ctx.genKeytype;
		int nTotal = 0;
		DBExecuteContext dbctxMain = null; //only one statement is allowed to generate keys
		try {
			DBExecuteContext dbctx = createContext();
			for(SqlStatement statement: stgroup.statementL) {
				int n = conn.executeCommandStatementGenKey(statement, keyType, dbctx);
				nTotal += n;
				dbctxMain = dbctx;
			}
		} catch (DBValidationException e) {
			convertAndRethrow(e);
		}

		DValue genVal = null;
		if (ctx.extractGeneratedKeys && !dbctxMain.genKeysL.isEmpty()) {
			try {
				SqlExecuteContext sqlctx = new SqlExecuteContext(registry, null);
				sqlctx.genKeysL = dbctxMain.genKeysL;
				HLDResultSetConverter hldRSCconverter = new HLDResultSetConverter(factorySvc, new ValueHelper(factorySvc), registry);
				genVal = hldRSCconverter.extractGeneratedKey(ctx, sqlctx);
			} catch (SQLException e) {
				DeliaExceptionHelper.throwError("extract-generated-key-failed", e.getMessage());
			}
		}
		return genVal;
	}

	private void convertAndRethrow(DBValidationException e) {
		super.convertAndRethrow(e, this);
	}

	@Override
	public boolean rawTableDetect(String tableName) {
		failIfNotInit1(); 
		RawStatementGenerator sqlgen = new RawStatementGenerator(factorySvc, dbType);
		String sql = sqlgen.generateTableDetect(tableName); //postgres tbls are lowercase
		SqlStatement statement = createSqlStatement(sql); 
		return execResultBoolean(conn, statement);
	}


	@Override
	public boolean rawFieldDetect(String tableName, String fieldName) {
		failIfNotInit1(); 
		RawStatementGenerator sqlgen = new RawStatementGenerator(factorySvc, dbType);
		String sql = sqlgen.generateFieldDetect(tableName, fieldName);
		SqlStatement statement = new SqlStatement(null);
		statement.sql = sql;
		return execResultBoolean(conn, statement);
	}

	@Override
	public void rawCreateTable(String tableName) {
		failIfNotInit1(); 
		TableCreator partialTableCreator = this.createPartialTableCreator();
		DStructType dtype = registry.findTypeOrSchemaVersionType(tableName);
		String sql = partialTableCreator.generateCreateTable(tableName, dtype);
		execSqlStatement(sql);
	}

	@Override
	public DValue executeInsert(HLDInsertStatement hld, SqlStatementGroup stmgrp, InsertContext ctx) {
		failIfNotInit2(); 

		if (ctx.extractGeneratedKeys) {
			return doInsert(stmgrp, ctx);
		} else {
			doInsert(stmgrp, ctx);
			return null;
		}
	}

	@Override
	public int executeUpdate(HLDUpdateStatement hld, SqlStatementGroup stgroup) {
		if (stgroup.statementL.isEmpty()) {
			return 0; //nothing to update
		}

		logStatementGroup(stgroup);
		int updateCount = 0;
		List<Integer > updateCountL = new ArrayList<>();
		try {
			DBExecuteContext dbctx = createContext();
			for(SqlStatement statement: stgroup.statementL) {
				//ignore empty statements. 
				if (statement.owner == hld.hldupdate && hld.hldupdate.isEmpty()) {
					continue;
				}
				
				int n = conn.executeCommandStatement(statement, dbctx);
				updateCountL.add(n);
			}
			updateCount = findUpdateCount("update", updateCountL, stgroup);
		} catch (DBValidationException e) {
			convertAndRethrow(e);
		}
		return updateCount;
	}

	@Override
	public int executeUpsert(HLDUpsertStatement hld, SqlStatementGroup stgroup, boolean noUpdateFlag) {
		if (stgroup == null) {
			return 0; //noupdate flag thing
		}
		if (stgroup.statementL.isEmpty()) {
			return 0; //nothing to update
		}

		logStatementGroup(stgroup);
		int updateCount = 0;
		List<Integer > updateCountL = new ArrayList<>();
		try {
			DBExecuteContext dbctx = createContext();
			for(SqlStatement statement: stgroup.statementL) {
				int n = conn.executeCommandStatement(statement, dbctx);
				updateCountL.add(n);
			}
			updateCount = findUpdateCount("merge", updateCountL, stgroup);
		} catch (DBValidationException e) {
			convertAndRethrow(e);
		}
		return updateCount;
	}

	@Override
	public void executeDelete(HLDDeleteStatement hld, SqlStatementGroup stgroup) {
		if (stgroup.statementL.isEmpty()) {
			return; //nothing to delete
		}

		logStatementGroup(stgroup);
		try {
			DBExecuteContext dbctx = createContext();
			for(SqlStatement statement: stgroup.statementL) {
				conn.execStatement(statement, dbctx);
			}
		} catch (DBValidationException e) {
			convertAndRethrow(e);
		}
	}


	@Override
	public QueryResponse executeHLDQuery(HLDQueryStatement hld, SqlStatementGroup stmgrp, QueryContext qtx) {
		failIfNotInit2(); 
		SqlStatement statement = stmgrp.statementL.get(0);
		logSql(statement);

		DBExecuteContext dbctx = createContext();
		ResultSet rs = conn.execQueryStatement(statement, dbctx);   // *** call the DB ***
		//TODO: do we need to catch and interpret exceptions here??

		QueryResponse qresp = new QueryResponse();
		HLDSelectHelper selectHelper = new HLDSelectHelper(factorySvc, registry);
		ResultTypeInfo selectResultType = selectHelper.getSelectResultType(hld);
		DBAccessContext dbactx = new DBAccessContext(registry, new DoNothingVarEvaluator());
		HLDResultSetConverter hldRSCconverter = new HLDResultSetConverter(factorySvc, new ValueHelper(factorySvc), registry);
		if (selectResultType.isScalarShape()) {
			QueryDetails details = new QueryDetails(); //TODO delete later
			qresp.dvalList = hldRSCconverter.buildScalarResult(rs, selectResultType, details, dbactx);
			qresp.ok = true;
		} else {
			qresp.dvalList = hldRSCconverter.buildDValueList(rs, dbactx, hld);
			qresp.ok = true;
		}
		return qresp;
	}

	@Override
	public boolean doesTableExist(String tableName) {
		failIfNotInit2(); 
		return rawTableDetect(tableName);
	}

	@Override
	public boolean doesFieldExist(String tableName, String fieldName) {
		failIfNotInit2(); 
		return rawFieldDetect(tableName, fieldName);
	}

	@Override
	public void createTable(String tableName) {
		failIfNotInit2(); 
		DStructType dtype = registry.findTypeOrSchemaVersionType(tableName);
		String sql = tableCreator.generateCreateTable(tableName, dtype);
		execSqlStatement(sql);
	}
	private void execSqlStatement(String sql) {
		logSql(sql);
		SqlStatement statement = createSqlStatement(sql); 
		DBExecuteContext dbctx = createContext();
		conn.execStatement(statement, dbctx);
	}

	@Override
	public void deleteTable(String tableName) {
		//failIfNotInit1(); 
		String sql = String.format("DROP TABLE IF EXISTS %s;", tableName);
		execSqlStatement(sql);
	}

	@Override
	public void renameTable(String tableName, String newTableName) {
		failIfNotInit1(); 
		String sql = String.format("ALTER TABLE %s RENAME TO %s", tableName, newTableName);
		execSqlStatement(sql);
	}

	@Override
	public void createField(String typeName, String field) {
		failIfNotInit2(); 
		DStructType dtype = registry.findTypeOrSchemaVersionType(typeName);
		String sql = tableCreator.generateCreateField(typeName, dtype, field);
		execSqlStatement(sql);
	}

	@Override
	public void deleteField(String typeName, String field, int datId) {
		failIfNotInit2(); 
		DStructType dtype = registry.findTypeOrSchemaVersionType(typeName);
		String sql = tableCreator.generateDeleteField(typeName, dtype, field, datId);
		execSqlStatement(sql);
	}

	@Override
	public void renameField(String typeName, String fieldName, String newName) {
		failIfNotInit2(); 
		String sql = tableCreator.generateRenameField(typeName, fieldName, newName);
		execSqlStatement(sql);
	}

	@Override
	public void alterFieldType(String typeName, String fieldName, String newFieldType) {
		failIfNotInit2(); 
		String sql = tableCreator.generateAlterFieldType(typeName, fieldName, newFieldType);
		execSqlStatement(sql);
	}

	@Override
	public void alterField(String typeName, String fieldName, String deltaFlags) {
		failIfNotInit2(); 
		String constraintName = null;
		if (deltaFlags.contains("-U")) {
			RawStatementGenerator sqlgen = new RawStatementGenerator(factorySvc, dbType);
			String sql = sqlgen.generateSchemaListing(DBListingType.ALL_CONSTRAINTS);
			constraintName = conn.findConstraint(sql, typeName, fieldName, "UNIQUE", false);
		} else if (deltaFlags.contains("+U")) {
			constraintName = generateUniqueConstraintName();
		}

		String sql = tableCreator.generateAlterField(typeName, fieldName, deltaFlags, constraintName);
		execSqlStatement(sql);
	}

	@Override
	public DBInterfaceFactory getDbInterface() {
		return dbInterface;
	}
	@Override
	public DatIdMap getDatIdMap() {
		return datIdMap;
	}
}