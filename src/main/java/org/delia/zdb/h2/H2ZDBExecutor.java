package org.delia.zdb.h2;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import org.delia.assoc.DatIdMap;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.DBAccessContext;
import org.delia.db.DBErrorConverter;
import org.delia.db.DBType;
import org.delia.db.DBValidationException;
import org.delia.db.InsertContext;
import org.delia.db.QueryContext;
import org.delia.db.QueryDetails;
import org.delia.db.QuerySpec;
import org.delia.db.ResultSetToDValConverter;
import org.delia.db.SpanHelper;
import org.delia.db.SqlExecuteContext;
import org.delia.db.ValueHelper;
import org.delia.db.hls.HLSQuerySpan;
import org.delia.db.hls.HLSQueryStatement;
import org.delia.db.hls.HLSSelectHelper;
import org.delia.db.hls.ResultTypeInfo;
import org.delia.db.sql.SimpleSqlNameFormatter;
import org.delia.db.sql.SqlNameFormatter;
import org.delia.db.sql.prepared.RawStatementGenerator;
import org.delia.db.sql.prepared.SelectFuncHelper;
import org.delia.db.sql.prepared.SqlStatement;
import org.delia.db.sql.prepared.SqlStatementGroup;
import org.delia.db.sql.table.FieldGenFactory;
import org.delia.log.Log;
import org.delia.queryresponse.LetSpan;
import org.delia.runner.DoNothingVarEvaluator;
import org.delia.runner.FetchRunner;
import org.delia.runner.QueryResponse;
import org.delia.runner.VarEvaluator;
import org.delia.runner.ZFetchRunnerImpl;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.type.Shape;
import org.delia.util.DeliaExceptionHelper;
import org.delia.zdb.ZDBConnection;
import org.delia.zdb.ZDBExecuteContext;
import org.delia.zdb.ZDBExecutor;
import org.delia.zdb.ZDBInterfaceFactory;
import org.delia.zdb.ZDelete;
import org.delia.zdb.ZInsert;
import org.delia.zdb.ZQuery;
import org.delia.zdb.ZTableCreator;
import org.delia.zdb.ZUpdate;
import org.delia.zdb.ZUpsert;

public class H2ZDBExecutor extends ServiceBase implements ZDBExecutor {

		private Log sqlLog;
		private H2ZDBInterfaceFactory dbInterface;
		private H2ZDBConnection conn;
		private DTypeRegistry registry;
		private boolean init1HasBeenDone;
		private boolean init2HasBeenDone;
		private DatIdMap datIdMap;
		private VarEvaluator varEvaluator;
		private DBType dbType;
		private DBErrorConverter errorConverter;
		protected ZTableCreator tableCreator;
		private ResultSetToDValConverter resultSetConverter;
		private ZInsert zinsert;
		private ZQuery zquery;
		private ZUpdate zupdate;
		private ZUpsert zupsert;
		private ZDelete zdelete;

		public H2ZDBExecutor(FactoryService factorySvc, Log sqlLog, H2ZDBInterfaceFactory dbInterface, H2ZDBConnection conn) {
			super(factorySvc);
			this.sqlLog = sqlLog;
			this.dbInterface = dbInterface;
			this.conn = conn;
			this.dbType = DBType.H2;
			this.errorConverter = dbInterface.getErrorConverter();
			this.resultSetConverter = new ResultSetToDValConverter(factorySvc, new ValueHelper(factorySvc));
			resultSetConverter.init(factorySvc);
		}

		@Override
		public ZDBConnection getDBConnection() {
			return conn;
		}
		
		@Override
		public void close() {
			conn.close();
		}

		@Override
		public void init1(DTypeRegistry registry) {
			this.init1HasBeenDone = true;
			this.registry = registry;
			this.zinsert = new ZInsert(factorySvc, registry);
			this.zquery = new ZQuery(factorySvc, registry);
			this.zupdate = new ZUpdate(factorySvc, registry);
			this.zupsert = new ZUpsert(factorySvc, registry);
			this.zdelete = new ZDelete(factorySvc, registry);
		}
		
		private ZTableCreator createPartialTableCreator() {
			SqlNameFormatter nameFormatter = new SimpleSqlNameFormatter();
			FieldGenFactory fieldGenFactory = new FieldGenFactory(factorySvc);
			return new ZTableCreator(factorySvc, registry, fieldGenFactory, nameFormatter, null, this);
		}
		

		@Override
		public void init2(DatIdMap datIdMap, VarEvaluator varEvaluator) {
			this.init2HasBeenDone = true;
			this.datIdMap = datIdMap;
			this.varEvaluator = varEvaluator;

			SqlNameFormatter nameFormatter = new SimpleSqlNameFormatter();
			FieldGenFactory fieldGenFactory = new FieldGenFactory(factorySvc);
			this.tableCreator = new ZTableCreator(factorySvc, registry, fieldGenFactory, nameFormatter, datIdMap, this);
		}

		@Override
		public FetchRunner createFetchRunner() {
			return new ZFetchRunnerImpl(factorySvc, this, registry, varEvaluator);
		}

		@Override
		public DValue rawInsert(DValue dval, InsertContext ctx) {
			failIfNotInit1();
			ZTableCreator partialTableCreator = createPartialTableCreator();
			
			if (ctx.extractGeneratedKeys) {
				return doInsert(dval, ctx, partialTableCreator);
			} else {
				doInsert(dval, ctx, partialTableCreator);
				return null;
			}
		}
		
		private void failIfNotInit1() {
			if (! init1HasBeenDone) {
				DeliaExceptionHelper.throwError("zinit1-not-done", "init1 not done");
			}
		}
		private void failIfNotInit2() {
			if (! init2HasBeenDone) {
				DeliaExceptionHelper.throwError("zinit2-not-done", "init2 not done");
			}
		}

		private DValue doInsert(DValue dval, InsertContext ctx, ZTableCreator tmpTableCreator) {
			SqlStatementGroup stgroup = zinsert.generate(dval, ctx, tmpTableCreator, this);

			logStatementGroup(stgroup);
			DType keyType = ctx.genKeytype;
			int nTotal = 0;
			ZDBExecuteContext dbctxMain = null; //assume only one. TODO fix
			try {
				ZDBExecuteContext dbctx = createContext();
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
					genVal = resultSetConverter.extractGeneratedKey(ctx, sqlctx);
				} catch (SQLException e) {
					DeliaExceptionHelper.throwError("extract-generated-key-failed", e.getMessage());
				}
			}
			return genVal;
		}
		
		private void convertAndRethrow(DBValidationException e) {
			ZTableCreator tmp = tableCreator == null ? createPartialTableCreator() : tableCreator;
			errorConverter.convertAndRethrow(e, tmp.alreadyCreatedL);
		}

		@Override
		public QueryResponse rawQuery(QuerySpec spec, QueryContext qtx) {
			failIfNotInit1(); 
			List<LetSpan> spanL = new ArrayList<>();
			QueryDetails details = new QueryDetails();
			ZTableCreator partialTableCreator = createPartialTableCreator();
			SqlStatement statement = zquery.generate(spec, qtx, partialTableCreator, spanL, details);

			logSql(statement);
			ZDBExecuteContext dbctx = createContext();
			ResultSet rs = conn.execQueryStatement(statement, dbctx);

			QueryResponse qresp = new QueryResponse();
			SpanHelper spanHelper = spanL == null ? null : new SpanHelper(spanL);
			SelectFuncHelper sfhelper = new SelectFuncHelper(factorySvc, registry, spanHelper);
			DType selectResultType = sfhelper.getSelectResultType(spec);
			if (selectResultType.isScalarShape()) {
				ResultTypeInfo rti = new ResultTypeInfo();
				rti.logicalType = selectResultType;
				rti.physicalType = selectResultType;
				qresp.dvalList = buildScalarResult(rs, rti, details);
//				fixupForExist(spec, qresp.dvalList, sfhelper, dbctx);
				qresp.ok = true;
			} else {
				String typeName = spec.queryExp.getTypeName();
				DStructType dtype = (DStructType) registry.findTypeOrSchemaVersionType(typeName);
				qresp.dvalList = buildDValueList(rs, dtype, details, null);
				qresp.ok = true;
			}
			return qresp;
		}

		@Override
		public boolean rawTableDetect(String tableName) {
			failIfNotInit1(); 
			RawStatementGenerator sqlgen = new RawStatementGenerator(factorySvc, dbType);
			String sql = sqlgen.generateTableDetect(tableName.toUpperCase()); //h2 tbls are UPPERCASE
			SqlStatement statement = createSqlStatement(sql); 
			return execResultBoolean(statement);
		}
		private SqlStatement createSqlStatement(String sql) {
			SqlStatement statement = new SqlStatement();
			statement.sql = sql;
			return statement;
		}
		private ZDBExecuteContext createContext() {
			ZDBExecuteContext dbctx = new ZDBExecuteContext();
			dbctx.logToUse = log;
			return dbctx;
		}

		private boolean execResultBoolean(SqlStatement statement) {
			logSql(statement);

			ZDBExecuteContext dbctx = createContext();
			ResultSet rs = conn.execQueryStatement(statement, dbctx);

			boolean tblExists = false;
			try {
				if (rs != null && rs.next()) {
					Boolean b = rs.getBoolean(1);
					tblExists = b;
				}
			} catch (SQLException e) {
				convertAndRethrowException(e);
			}        

			return tblExists;
		}
		
		private void convertAndRethrowException(SQLException e) {
			errorConverter.convertAndRethrowException(e);
		}

		protected void logSql(SqlStatement statement) {
			StringJoiner joiner = new StringJoiner(",");
			for(DValue dval: statement.paramL) {
				if (dval.getType().isShape(Shape.STRING)) {
					joiner.add(String.format("'%s'", dval.asString()));
				} else {
					joiner.add(dval == null ? "null" : dval.asString());
				}
			}

			String s = String.format("%s  -- (%s)", statement.sql, joiner.toString());
			logSql(s);
		}
		protected void logSql(String sql) {
			sqlLog.log("SQL: " + sql);
		}
		protected void logStatementGroup(SqlStatementGroup stgroup) {
//			sqlLog.log("SQL: " + stgroup.flatten());
			for(SqlStatement stat: stgroup.statementL) {
				this.logSql(stat);
			}
		}


		@Override
		public boolean rawFieldDetect(String tableName, String fieldName) {
			failIfNotInit1(); 
			RawStatementGenerator sqlgen = new RawStatementGenerator(factorySvc, dbType);
			String sql = sqlgen.generateFieldDetect(tableName, fieldName);
			SqlStatement statement = new SqlStatement();
			statement.sql = sql;
			return execResultBoolean(statement);
		}

		@Override
		public void rawCreateTable(String tableName) {
			failIfNotInit1(); 
			ZTableCreator partialTableCreator = this.createPartialTableCreator();
			DStructType dtype = registry.findTypeOrSchemaVersionType(tableName);
			String sql = partialTableCreator.generateCreateTable(tableName, dtype);
			execSqlStatement(sql);
		}

		@Override
		public DValue executeInsert(DValue dval, InsertContext ctx) {
			failIfNotInit2(); 
			
			if (ctx.extractGeneratedKeys) {
				return doInsert(dval, ctx, tableCreator);
			} else {
				doInsert(dval, ctx, tableCreator);
				return null;
			}
		}

		@Override
		public int executeUpdate(QuerySpec spec, DValue dvalPartial, Map<String, String> assocCrudMap) {
			SqlStatementGroup stgroup = zupdate.generate(spec, dvalPartial, assocCrudMap, varEvaluator, tableCreator, this);
			if (stgroup.statementL.isEmpty()) {
				return 0; //nothing to update
			}

			logStatementGroup(stgroup);
			int updateCount = 0;
			List<Integer > updateCountL = new ArrayList<>();
			try {
				ZDBExecuteContext dbctx = createContext();
				for(SqlStatement statement: stgroup.statementL) {
					int n = conn.executeCommandStatement(statement, dbctx);
					updateCountL.add(n);
				}
				updateCount = findUpdateCount("update", updateCountL, stgroup);
			} catch (DBValidationException e) {
				convertAndRethrow(e);
			}
			return updateCount;
		}
		protected int findUpdateCount(String target, List<Integer> updateCountL, SqlStatementGroup stgroup) {
			int minPos = Integer.MAX_VALUE;
			int foundResult = 0;
			
			int index = 0;
			for(SqlStatement stat: stgroup.statementL) {
				int pos = stat.sql.toLowerCase().indexOf(target);
				if (pos >= 0 && pos < minPos) {
					minPos = pos;
					foundResult = updateCountL.get(index);
				}
				index++;
			}
			return foundResult;
		}

		@Override
		public int executeUpsert(QuerySpec spec, DValue dvalFull, Map<String, String> assocCrudMap,
				boolean noUpdateFlag) {
			
			SqlStatementGroup stgroup = zupsert.generate(spec, dvalFull, assocCrudMap, noUpdateFlag, varEvaluator, tableCreator, this);
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
				ZDBExecuteContext dbctx = createContext();
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
		public void executeDelete(QuerySpec spec) {
			SqlStatementGroup stgroup = zdelete.generate(spec, varEvaluator, tableCreator, this);
			if (stgroup.statementL.isEmpty()) {
				return; //nothing to delete
			}

			logStatementGroup(stgroup);
			try {
				ZDBExecuteContext dbctx = createContext();
				for(SqlStatement statement: stgroup.statementL) {
					conn.execStatement(statement, dbctx);
				}
			} catch (DBValidationException e) {
				convertAndRethrow(e);
			}
		}

		@Override
		public QueryResponse executeHLSQuery(HLSQueryStatement hls, String sql, QueryContext qtx) {
			failIfNotInit2(); 
			SqlStatement statement = createSqlStatement(sql);
			for(HLSQuerySpan hlspan: hls.hlspanL) {
				statement.paramL.addAll(hlspan.paramL);
			}
			logSql(statement);
			
			ZDBExecuteContext dbctx = createContext();
			ResultSet rs = conn.execQueryStatement(statement, dbctx);
			//TODO: do we need to catch and interpret execptions here??

			QueryDetails details = hls.details;

			QueryResponse qresp = new QueryResponse();
			HLSSelectHelper selectHelper = new HLSSelectHelper(factorySvc, registry);
			ResultTypeInfo selectResultType = selectHelper.getSelectResultType(hls);
			if (selectResultType.isScalarShape()) {
				qresp.dvalList = buildScalarResult(rs, selectResultType, details);
//				fixupForExist(spec, qresp.dvalList, sfhelper, dbctx);
				qresp.ok = true;
			} else {
				String typeName = hls.querySpec.queryExp.getTypeName();
				DStructType dtype = (DStructType) registry.findTypeOrSchemaVersionType(typeName);
				qresp.dvalList = buildDValueList(rs, dtype, details, hls);
				qresp.ok = true;
			}
			return qresp;
		}
		protected List<DValue> buildScalarResult(ResultSet rs, ResultTypeInfo selectResultType, QueryDetails details) {
			DBAccessContext dbctx = new DBAccessContext(registry, new DoNothingVarEvaluator());
			return resultSetConverter.buildScalarResult(rs, selectResultType, details, dbctx);
		}
		protected List<DValue> buildDValueList(ResultSet rs, DStructType dtype, QueryDetails details, HLSQueryStatement hls) {
			DBAccessContext dbctx = new DBAccessContext(registry, new DoNothingVarEvaluator());
			return resultSetConverter.buildDValueList(rs, dtype, details, dbctx, hls);
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
			ZDBExecuteContext dbctx = createContext();
			conn.execStatement(statement, dbctx);
		}

		@Override
		public void deleteTable(String tableName) {
			failIfNotInit1(); 
			String sql = String.format("DROP TABLE IF EXISTS %s;", tableName);
			execSqlStatement(sql);
		}

		@Override
		public void renameTable(String tableName, String newTableName) {
			// TODO Auto-generated method stub

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
			// TODO Auto-generated method stub

		}

		@Override
		public void alterFieldType(String typeName, String fieldName, String newFieldType) {
			// TODO Auto-generated method stub

		}

		@Override
		public void alterField(String typeName, String fieldName, String deltaFlags) {
			// TODO Auto-generated method stub

		}

		@Override
		public ZDBInterfaceFactory getDbInterface() {
			return dbInterface;
		}
		@Override
		public DatIdMap getDatIdMap() {
			return datIdMap;
		}
	}