//package org.delia.db.postgres;
//
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.util.List;
//import java.util.Map;
//
//import org.delia.core.FactoryService;
//import org.delia.db.DBAccessContext;
//import org.delia.db.DBExecutor;
//import org.delia.db.DBInterfaceBase;
//import org.delia.db.DBInterfaceInternal;
//import org.delia.db.DBType;
//import org.delia.db.DBValidationException;
//import org.delia.db.InsertContext;
//import org.delia.db.QueryContext;
//import org.delia.db.QueryDetails;
//import org.delia.db.QuerySpec;
//import org.delia.db.RawDBExecutor;
//import org.delia.db.SchemaContext;
//import org.delia.db.SpanHelper;
//import org.delia.db.SqlExecuteContext;
//import org.delia.db.h2.DBListingType;
//import org.delia.db.h2.H2DBConnection;
//import org.delia.db.hls.HLSQuerySpan;
//import org.delia.db.hls.HLSQueryStatement;
//import org.delia.db.hls.HLSSelectHelper;
//import org.delia.db.hls.ResultTypeInfo;
//import org.delia.db.sql.ConnectionFactory;
//import org.delia.db.sql.fragment.DeleteFragmentParser;
//import org.delia.db.sql.fragment.DeleteStatementFragment;
//import org.delia.db.sql.fragment.FragmentParserService;
//import org.delia.db.sql.fragment.InsertFragmentParser;
//import org.delia.db.sql.fragment.InsertStatementFragment;
//import org.delia.db.sql.fragment.SelectFragmentParser;
//import org.delia.db.sql.fragment.SelectStatementFragment;
//import org.delia.db.sql.fragment.UpdateFragmentParser;
//import org.delia.db.sql.fragment.UpdateStatementFragment;
//import org.delia.db.sql.fragment.UpsertStatementFragment;
//import org.delia.db.sql.fragment.WhereFragmentGenerator;
//import org.delia.db.sql.prepared.PreparedStatementGenerator;
//import org.delia.db.sql.prepared.SelectFuncHelper;
//import org.delia.db.sql.prepared.SqlStatement;
//import org.delia.db.sql.prepared.SqlStatementGroup;
//import org.delia.db.sql.table.TableCreator;
//import org.delia.log.Log;
//import org.delia.queryresponse.LetSpan;
//import org.delia.runner.QueryResponse;
//import org.delia.type.DStructType;
//import org.delia.type.DType;
//import org.delia.type.DValue;
//import org.delia.type.TypeReplaceSpec;
//import org.delia.util.DeliaExceptionHelper;
//
//
///**
// * Represents db access to a single Postgres database
// * @author Ian Rae
// *
// */
//public class PostgresDBInterface extends DBInterfaceBase implements DBInterfaceInternal {
//
//	public PostgresDBInterface(FactoryService factorySvc, ConnectionFactory connFactory) {
//		super(DBType.POSTGRES, factorySvc, connFactory, new PostgresSqlHelperFactory(factorySvc));
//		this.sqlHelperFactory.init(this);
//		this.errorConverter = this.sqlHelperFactory.createErrorConverter();
//		this.connFactory.setErrorConverter(errorConverter);
//	}
//	
//	@Override
//	public DBExecutor createExector(DBAccessContext ctx) {
//		H2DBConnection conn;
//		//we can use the h2 connection class
//		conn = new H2DBConnection(factorySvc, connFactory, errorConverter);
//		PostgresDBExecutor dbexecutor = new PostgresDBExecutor(this, ctx, conn);
//		ctx.connObject = conn;
//		conn.openDB();
//		return dbexecutor;
//	}
//
//	@Override
//	public RawDBExecutor createRawExector(DBAccessContext dbctx) {
//		H2DBConnection conn;
//		//we can use the h2 connection class
//		conn = new H2DBConnection(factorySvc, connFactory, errorConverter);
//		PostgresRawDBExecutor dbexecutor = new PostgresRawDBExecutor(this, dbctx, conn);
//		dbctx.connObject = conn;
//		conn.openDB();
//		return dbexecutor;
//	}
//
//
//	
//	@Override
//	public DValue executeInsert(DValue dval, InsertContext ctx, DBAccessContext dbctx) {
//		//TODO: we shouldn't keep tableCreator.alreadyCreatedL around. it becomes out of date 
//		//after schema migrations. should only use it during initial table creation.
//		createTableCreator(dbctx);
//		SqlStatementGroup stgroup;
//		SqlExecuteContext sqlctx = new SqlExecuteContext(dbctx);
//		
//		//			log.log("FRAG PARSER INSERT....................");
//		FragmentParserService fpSvc = null; //TODO fix new FragmentParserService(factorySvc, dbctx.registry, dbctx.varEvaluator, tableCreator.alreadyCreatedL, this, dbctx, sqlHelperFactory, null, null);
//		InsertFragmentParser parser = new InsertFragmentParser(factorySvc, fpSvc, null); //TODO fix null
//		String typeName = dval.getType().getName();
//		InsertStatementFragment selectFrag = parser.parseInsert(typeName, dval);
//		stgroup = parser.renderInsertGroup(selectFrag);
//		
//		logStatementGroup(stgroup);
//		H2DBConnection conn = (H2DBConnection) dbctx.connObject;
//		try {
//			sqlctx.getGeneratedKeys = ctx.extractGeneratedKeys;
//			List<Integer > updateCountL = conn.execInsertStatementGroup(stgroup, sqlctx);
//		} catch (DBValidationException e) {
//			convertAndRethrow(e, dbctx);
//		}
//
//		DValue genVal = null;
//		if (ctx.extractGeneratedKeys && !sqlctx.genKeysL.isEmpty()) {
//			try {
//				genVal = extractGeneratedKey(ctx, sqlctx);
//			} catch (SQLException e) {
//				DeliaExceptionHelper.throwError("extract-generated-key-failed", e.getMessage());
//			}
//		}
//		return genVal;
//	}
//
//	@Override
//	public QueryResponse executeQuery(QuerySpec spec, QueryContext qtx, DBAccessContext dbctx) {
//		List<LetSpan> spanL = buildSpans(spec, qtx);
//		failIfMultiSpan(spec, qtx, spanL);
//		QueryDetails details = new QueryDetails();
//		SqlStatement statement;
//		
//		//			log.log("FRAG PARSEr....................");
//		createTableCreator(dbctx);
//		WhereFragmentGenerator whereGen = new PostgresWhereFragmentGenerator(factorySvc, dbctx.registry, dbctx.varEvaluator);
//		FragmentParserService fpSvc = null; //TODO fixnew FragmentParserService(factorySvc, dbctx.registry, dbctx.varEvaluator, tableCreator.alreadyCreatedL, this, dbctx, sqlHelperFactory, whereGen, spanL);
//		SelectFragmentParser parser = new PostgresFragmentParser(factorySvc, fpSvc);
//		whereGen.tableFragmentMaker = parser;
//		SelectStatementFragment selectFrag = parser.parseSelect(spec, details);
//		parser.renderSelect(selectFrag);
//		statement = selectFrag.statement;
//		
//		logSql(statement);
//		H2DBConnection conn = (H2DBConnection) dbctx.connObject;
//		ResultSet rs = conn.execQueryStatement(statement, dbctx);
//		//TODO: do we need to catch and interpret execptions here??
//
//		QueryResponse qresp = new QueryResponse();
//
//		SpanHelper spanHelper = spanL == null ? null : new SpanHelper(spanL);
//		SelectFuncHelper sfhelper = sqlHelperFactory.createSelectFuncHelper(dbctx, spanHelper);
//		DType selectResultType = sfhelper.getSelectResultType(spec);
//		if (selectResultType.isScalarShape()) {
//			ResultTypeInfo rti = new ResultTypeInfo();
//			rti.logicalType = selectResultType;
//			rti.physicalType = selectResultType;
//			qresp.dvalList = buildScalarResult(rs, rti, details, dbctx);
//			fixupForExist(spec, qresp.dvalList, sfhelper, dbctx);
//			qresp.ok = true;
//		} else {
//			String typeName = spec.queryExp.getTypeName();
//			DStructType dtype = (DStructType) dbctx.registry.findTypeOrSchemaVersionType(typeName);
//			qresp.dvalList = buildDValueList(rs, dtype, details, dbctx, null);
//			qresp.ok = true;
//		}
//		return qresp;
//	}
//	
//	private void fixupForExist(QuerySpec spec, List<DValue> dvalList, SelectFuncHelper sfhelper, DBAccessContext dbctx) {
//		if (sfhelper.isExistsPresent(spec)) {
//			valueHelper.fixupForExist(dvalList, dbctx);
//			DValue dval = dvalList.get(0);
//		}
//	}
//
//	@Override
//	public boolean doesTableExist(String tableName, DBAccessContext dbctx) {
//		H2DBConnection conn = (H2DBConnection) dbctx.connObject;
//		PreparedStatementGenerator sqlgen = createPrepSqlGen(dbctx);
//		return conn.newExecTableDetect(tableName, sqlgen, dbctx.disableSqlLogging);
//	}
//
//	@Override
//	public boolean doesFieldExist(String tableName, String fieldName, DBAccessContext dbctx) {
//		H2DBConnection conn = (H2DBConnection) dbctx.connObject;
//		PreparedStatementGenerator sqlgen = createPrepSqlGen(dbctx);
//		return conn.execFieldDetect(tableName, fieldName, sqlgen, dbctx.disableSqlLogging);
//	}
//	
//
//	@Override
//	public void executeDelete(QuerySpec spec, DBAccessContext dbctx) {
//		SqlStatement statement;
//		
//		//			log.log("FRAG PARSER DELETE....................");
//		createTableCreator(dbctx);
//		WhereFragmentGenerator whereGen = new WhereFragmentGenerator(factorySvc, dbctx.registry, dbctx.varEvaluator);
//		FragmentParserService fpSvc = null;//TODO new FragmentParserService(factorySvc, dbctx.registry, dbctx.varEvaluator, tableCreator.alreadyCreatedL, this, dbctx, sqlHelperFactory, whereGen, null);
//		DeleteFragmentParser parser = new DeleteFragmentParser(factorySvc, fpSvc);
//		whereGen.tableFragmentMaker = parser;
//		QueryDetails details = new QueryDetails();
//		DeleteStatementFragment selectFrag = parser.parseDelete(spec, details);
//		parser.renderDelete(selectFrag);
//		statement = selectFrag.statement;
//			
//		logSql(statement);
//		createTableCreator(dbctx);
//		H2DBConnection conn = (H2DBConnection) dbctx.connObject;
//		try {
//			SqlExecuteContext sqlctx = new SqlExecuteContext(dbctx);
//			boolean b = conn.execStatement(statement, sqlctx); 
//			//TODO: what to do if b is false?
//		} catch (DBValidationException e) {
//			convertAndRethrow(e, dbctx);
//		}
//	}
//	
//	@Override
//	public int executeUpdate(QuerySpec spec, DValue dval, Map<String, String> assocCrudMap, DBAccessContext dbctx) {
//		SqlStatementGroup stgroup;
//		createTableCreator(dbctx);
//		
//		//			log.log("FRAG PARSER UPDATE....................");
//		createTableCreator(dbctx);
//		WhereFragmentGenerator whereGen = new WhereFragmentGenerator(factorySvc, dbctx.registry, dbctx.varEvaluator);
//		FragmentParserService fpSvc = null; //TODOnew FragmentParserService(factorySvc, dbctx.registry, dbctx.varEvaluator, tableCreator.alreadyCreatedL, this, dbctx, sqlHelperFactory, whereGen, null);
//		PostgresAssocTablerReplacer assocTblReplacer = new PostgresAssocTablerReplacer(factorySvc, fpSvc);
//		UpdateFragmentParser parser = new UpdateFragmentParser(factorySvc, fpSvc, assocTblReplacer);
//		whereGen.tableFragmentMaker = parser;
//		parser.useAliases(false);
//		QueryDetails details = new QueryDetails();
//		UpdateStatementFragment selectFrag = parser.parseUpdate(spec, details, dval, assocCrudMap);
//		stgroup = parser.renderUpdateGroup(selectFrag);
//		
//		if (stgroup.statementL.isEmpty()) {
//			return 0; //nothing to update
//		}
//		
//		logStatementGroup(stgroup);
//		H2DBConnection conn = (H2DBConnection) dbctx.connObject;
//		int updateCount = 0;
//		try {
//			SqlExecuteContext sqlctx = new SqlExecuteContext(dbctx);
//			List<Integer > updateCountL = conn.execUpdateStatementGroup(stgroup, sqlctx);
//			updateCount = findUpdateCount("update", updateCountL, stgroup);
//		} catch (DBValidationException e) {
//			convertAndRethrow(e, dbctx);
//		}
//		
//		return updateCount;
//	}
//	@Override
//	public int executeUpsert(QuerySpec spec, DValue dval, Map<String, String> assocCrudMap, boolean noUpdateFlag, DBAccessContext dbctx) {
//		SqlStatementGroup stgroup;
//		createTableCreator(dbctx);
//		
//		//			log.log("FRAG PARSER UPSERT....................");
//		createTableCreator(dbctx);
//		WhereFragmentGenerator whereGen = new WhereFragmentGenerator(factorySvc, dbctx.registry, dbctx.varEvaluator);
//		FragmentParserService fpSvc = null;//TODO new FragmentParserService(factorySvc, dbctx.registry, dbctx.varEvaluator, tableCreator.alreadyCreatedL, this, dbctx, sqlHelperFactory, whereGen, null);
//		PostgresAssocTablerReplacer assocTblReplacer = new PostgresAssocTablerReplacer(factorySvc, fpSvc);
//		PostgresUpsertFragmentParser parser = new PostgresUpsertFragmentParser(factorySvc, fpSvc, assocTblReplacer);
//		whereGen.tableFragmentMaker = parser;
//		QueryDetails details = new QueryDetails();
//		UpsertStatementFragment selectFrag = parser.parseUpsert(spec, details, dval, assocCrudMap, noUpdateFlag);
//		stgroup = parser.renderUpsertGroup(selectFrag);
//		//			s = selectFrag.statement;
//			
//		if (stgroup.statementL.isEmpty()) {
//			return 0; //nothing to update
//		}
//		
//		logStatementGroup(stgroup);
//		H2DBConnection conn = (H2DBConnection) dbctx.connObject;
//		int updateCount = 0;
//		try {
//			SqlExecuteContext sqlctx = new SqlExecuteContext(dbctx);
//			
//			List<Integer > updateCountL = conn.execUpdateStatementGroup(stgroup, sqlctx);
//			updateCount = findUpdateCount("insert into", updateCountL, stgroup);
//		} catch (DBValidationException e) {
//			convertAndRethrow(e, dbctx);
//		}
//		
//		return updateCount;
//	}
//	
//	protected int executeSQL(String sql, DBAccessContext ctx) {
//		logSql(sql);
//		int updateCount = 0;
//		try {
//			H2DBConnection conn = (H2DBConnection) ctx.connObject;
//			updateCount = conn.executeRawSql(sql);
//		} catch (DBValidationException e) {
//			convertAndRethrow(e, ctx);
//		}
//		return updateCount;
//	}
//
//	@Override
//	public void createTable(String tableName, DBAccessContext dbctx, SchemaContext ctx) {
//		DStructType dtype = dbctx.registry.findTypeOrSchemaVersionType(tableName);
//		String sql;
//		createTableCreator(dbctx);
//		sql = tableCreator.generateCreateTable(tableName, dtype);
//		executeSQL(sql, dbctx);
//	}	@Override
//	public void deleteTable(String tableName, DBAccessContext dbctx, SchemaContext ctx) {
//		String sql = String.format("DROP TABLE IF EXISTS %s;", tableName);
//		executeSQL(sql, dbctx);
//	}
//	@Override
//	public void renameTable(String tableName, String newTableName, DBAccessContext dbctx, SchemaContext ctx) {
//		String sql = String.format("ALTER TABLE %s RENAME TO %s", tableName, newTableName);
//		executeSQL(sql, dbctx);
//	}
//	@Override
//	public void createField(String typeName, String fieldName, DBAccessContext dbctx, SchemaContext ctx) {
//		TableCreator creator = this.sqlHelperFactory.createTableCreator(dbctx, ctx.datIdMap);
//		String sql = creator.generateCreateField(typeName, null, fieldName);
//		executeSQL(sql, dbctx);
//	}
//
//	@Override
//	public void deleteField(String typeName, String field, int datId, DBAccessContext dbctx, SchemaContext ctx) {
//		String sql = String.format("ALTER TABLE %s DROP COLUMN %s", typeName, field);
//		executeSQL(sql, dbctx);
//	}
//
//	// ---
//	private void convertAndRethrow(DBValidationException e, DBAccessContext ctx) {
//		createTableCreator(ctx);
//		errorConverter.convertAndRethrow(e, tableCreator.alreadyCreatedL);
//	}
//
//	@Override
//	public void renameField(String typeName, String fieldName, String newName, DBAccessContext dbctx, SchemaContext ctx) {
//		TableCreator creator = this.sqlHelperFactory.createTableCreator(dbctx, ctx.datIdMap);
//		String sql = creator.generateRenameField(typeName, fieldName, newName);
//		executeSQL(sql, dbctx);
//	}
//
//	@Override
//	public void alterFieldType(String typeName, String fieldName, String newFieldType, DBAccessContext dbctx, SchemaContext ctx) {
//		TableCreator creator = this.sqlHelperFactory.createTableCreator(dbctx, ctx.datIdMap);
//		String sql = creator.generateAlterFieldType(typeName, fieldName, newFieldType);
//		executeSQL(sql, dbctx);
//	}
//
//	@Override
//	public void alterField(String tableName, String fieldName, String deltaFlags,
//			DBAccessContext dbctx, SchemaContext ctx) {
//		TableCreator creator = this.sqlHelperFactory.createTableCreator(dbctx, ctx.datIdMap);
//		String constraintName = String.format("%s_%s_key", creator.tblName(tableName), creator.tblName(fieldName));		
//		String sql = creator.generateAlterField(tableName, fieldName, deltaFlags, constraintName);
//		executeSQL(sql, dbctx);
//	}
//	
//	//---------------- DBInterfaceInternal -------------------
//	@Override
//	public String getConnectionSummary() {
//		return connFactory.getConnectionSummary();
//	}
//
//	@Override
//	public void enablePrintStackTrace(boolean b) {
//		this.errorConverter.setPrintStackTraceEnabled(b);
//	}
//
//	@Override
//	public void enumerateAllTables(Log logToUse) {
//		DBAccessContext dbctx = new DBAccessContext(null, null);
//		try(PostgresDBExecutor exec = (PostgresDBExecutor) this.createExector(dbctx)) {
//			PreparedStatementGenerator sqlgen = createPrepSqlGen(dbctx);
//			exec.getConn().enumerateDBSchema(sqlgen, logToUse, DBListingType.ALL_TABLES);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//	@Override
//	public void enumerateAllConstraints(Log logToUse) {
//		DBAccessContext dbctx = new DBAccessContext(null, null);
//		try(PostgresDBExecutor exec = (PostgresDBExecutor) this.createExector(dbctx)) {
//			PreparedStatementGenerator sqlgen = createPrepSqlGen(dbctx);
//			exec.getConn().enumerateDBSchema(sqlgen, logToUse, DBListingType.ALL_CONSTRAINTS);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//	}
//	@Override
//	public void performTypeReplacement(TypeReplaceSpec spec) {
//		//nothing to do
//	}
//
//
//	@Override
//	public QueryResponse executeHLSQuery(HLSQueryStatement hls, String sql, QueryContext qtx, DBAccessContext dbctx) {
//		SqlStatement statement = new SqlStatement();
//		statement.sql = sql;
//		for(HLSQuerySpan hlspan: hls.hlspanL) {
//			statement.paramL.addAll(hlspan.paramL);
//		}
//		logSql(statement);
//		
//		H2DBConnection conn = (H2DBConnection) dbctx.connObject;
//		ResultSet rs = conn.execQueryStatement(statement, dbctx);
//		//TODO: do we need to catch and interpret execptions here??
//
//		QueryDetails details = hls.details;
//
//		QueryResponse qresp = new QueryResponse();
//		HLSSelectHelper selectHelper = new HLSSelectHelper(factorySvc, dbctx.registry);
//		ResultTypeInfo selectResultType = selectHelper.getSelectResultType(hls);
//		if (selectResultType.isScalarShape()) {
//			qresp.dvalList = buildScalarResult(rs, selectResultType, details, dbctx);
////			fixupForExist(spec, qresp.dvalList, sfhelper, dbctx);
//			qresp.ok = true;
//		} else {
//			String typeName = hls.querySpec.queryExp.getTypeName();
//			DStructType dtype = (DStructType) dbctx.registry.findTypeOrSchemaVersionType(typeName);
//			qresp.dvalList = buildDValueList(rs, dtype, details, dbctx, hls);
//			qresp.ok = true;
//		}
//		return qresp;
//	}
//}