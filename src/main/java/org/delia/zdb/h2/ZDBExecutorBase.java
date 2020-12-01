package org.delia.zdb.h2;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Random;
import java.util.StringJoiner;

import org.delia.assoc.DatIdMap;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.DBAccessContext;
import org.delia.db.DBErrorConverter;
import org.delia.db.DBType;
import org.delia.db.DBValidationException;
import org.delia.db.QueryDetails;
import org.delia.db.ResultSetConverter;
import org.delia.db.ResultSetToDValConverter;
import org.delia.db.ValueHelper;
import org.delia.db.hls.HLSQueryStatement;
import org.delia.db.hls.ResultTypeInfo;
import org.delia.db.sql.SimpleSqlNameFormatter;
import org.delia.db.sql.SqlNameFormatter;
import org.delia.db.sql.prepared.SqlStatement;
import org.delia.db.sql.prepared.SqlStatementGroup;
import org.delia.db.sql.table.FieldGenFactory;
import org.delia.log.Log;
import org.delia.runner.DoNothingVarEvaluator;
import org.delia.runner.VarEvaluator;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.type.Shape;
import org.delia.util.DeliaExceptionHelper;
import org.delia.zdb.ZDBConnection;
import org.delia.zdb.ZDBExecuteContext;
import org.delia.zdb.ZDBExecutor;
import org.delia.zdb.ZTableCreator;

public abstract class ZDBExecutorBase extends ServiceBase {

	protected Log sqlLog;
	protected DTypeRegistry registry;
	protected boolean init1HasBeenDone;
	protected boolean init2HasBeenDone;
	protected DatIdMap datIdMap;
	protected VarEvaluator varEvaluator;
	protected DBType dbType;
	protected DBErrorConverter errorConverter;
	protected ZTableCreator tableCreator;
//	protected ResultSetToDValConverter resultSetConverter;
	protected ResultSetConverter resultSetConverter;
	protected Random random = new Random();

	public ZDBExecutorBase(FactoryService factorySvc, Log sqlLog, DBErrorConverter errorConverter) {
		super(factorySvc);
		this.sqlLog = sqlLog;
		this.errorConverter = errorConverter;
//		this.resultSetConverter = new ResultSetToDValConverter(factorySvc, new ValueHelper(factorySvc));
		this.resultSetConverter = new ResultSetConverter(factorySvc, new ValueHelper(factorySvc));
		resultSetConverter.init(factorySvc);
	}

	public void init1(DTypeRegistry registry) {
		this.init1HasBeenDone = true;
		this.registry = registry;
	}

	protected ZTableCreator createPartialTableCreator(ZDBExecutor zexec) {
		SqlNameFormatter nameFormatter = new SimpleSqlNameFormatter();
		FieldGenFactory fieldGenFactory = createFieldGenFactory();
		return createZTableCreator(fieldGenFactory, nameFormatter, null, zexec);
	}

	public void init2(DatIdMap datIdMap, VarEvaluator varEvaluator, ZDBExecutor zexec) {
		this.init2HasBeenDone = true;
		this.datIdMap = datIdMap;
		this.varEvaluator = varEvaluator;

		SqlNameFormatter nameFormatter = new SimpleSqlNameFormatter();
		FieldGenFactory fieldGenFactory = createFieldGenFactory();
		this.tableCreator = createZTableCreator(fieldGenFactory, nameFormatter, datIdMap, zexec);
	}
	
	protected ZTableCreator createZTableCreator(FieldGenFactory fieldGenFactory, SqlNameFormatter nameFormatter, DatIdMap datIdMap, ZDBExecutor zexec) {
		return  new ZTableCreator(factorySvc, registry, fieldGenFactory, nameFormatter, datIdMap, zexec);
	}
	
	protected FieldGenFactory createFieldGenFactory() {
		return new FieldGenFactory(factorySvc);
	}

	protected void failIfNotInit1() {
		if (! init1HasBeenDone) {
			DeliaExceptionHelper.throwError("zinit1-not-done", "init1 not done");
		}
	}
	protected void failIfNotInit2() {
		if (! init2HasBeenDone) {
			DeliaExceptionHelper.throwError("zinit2-not-done", "init2 not done");
		}
	}

	protected void convertAndRethrow(DBValidationException e, ZDBExecutor zexec) {
		ZTableCreator tmp = tableCreator == null ? createPartialTableCreator(zexec) : tableCreator;
		errorConverter.convertAndRethrow(e, tmp.alreadyCreatedL);
	}

	protected SqlStatement createSqlStatement(String sql) {
		SqlStatement statement = new SqlStatement();
		statement.sql = sql;
		return statement;
	}
	protected ZDBExecuteContext createContext() {
		ZDBExecuteContext dbctx = new ZDBExecuteContext();
		dbctx.logToUse = log;
		return dbctx;
	}

	protected boolean execResultBoolean(ZDBConnection conn, SqlStatement statement) {
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

	protected void convertAndRethrowException(SQLException e) {
		errorConverter.convertAndRethrowException(e);
	}

	protected void logSql(SqlStatement statement) {
		StringJoiner joiner = new StringJoiner(",");
		for(DValue dval: statement.paramL) {
			joiner.add(String.format("'%s'", dval.asString()));
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

	protected List<DValue> buildScalarResult(ResultSet rs, ResultTypeInfo selectResultType, QueryDetails details) {
		DBAccessContext dbctx = new DBAccessContext(registry, new DoNothingVarEvaluator());
		return resultSetConverter.buildScalarResult(rs, selectResultType, details, dbctx);
	}
	protected List<DValue> buildDValueList(ResultSet rs, DStructType dtype, QueryDetails details, HLSQueryStatement hls) {
		DBAccessContext dbctx = new DBAccessContext(registry, new DoNothingVarEvaluator());
		return resultSetConverter.buildDValueList(rs, dtype, details, dbctx, hls);
	}

	protected void execSqlStatement(ZDBConnection conn, String sql) {
		logSql(sql);
		SqlStatement statement = createSqlStatement(sql); 
		ZDBExecuteContext dbctx = createContext();
		conn.execStatement(statement, dbctx);
	}

	protected String generateUniqueConstraintName() {
		int n = random.nextInt(Integer.MAX_VALUE - 10);
		return String.format("DConstraint_%d", n);
	}

}