package org.delia.zdb.h2;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Random;
import java.util.StringJoiner;

import org.delia.assoc.DatIdMap;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.DBErrorConverter;
import org.delia.db.DBType;
import org.delia.db.DBValidationException;
import org.delia.db.SqlStatement;
import org.delia.db.SqlStatementGroup;
import org.delia.db.sql.SimpleSqlNameFormatter;
import org.delia.db.sql.SqlNameFormatter;
import org.delia.db.sql.table.FieldGenFactory;
import org.delia.log.Log;
import org.delia.log.LoggableBlob;
import org.delia.runner.VarEvaluator;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.type.Shape;
import org.delia.util.BlobUtils;
import org.delia.util.DeliaExceptionHelper;
import org.delia.zdb.DBConnection;
import org.delia.zdb.DBExecuteContext;
import org.delia.zdb.DBExecutor;
import org.delia.zdb.TableCreator;

public abstract class DBExecutorBase extends ServiceBase {

	protected Log sqlLog;
	protected DTypeRegistry registry;
	protected boolean init1HasBeenDone;
	protected boolean init2HasBeenDone;
	protected DatIdMap datIdMap;
	protected VarEvaluator varEvaluator;
	protected DBType dbType;
	protected DBErrorConverter errorConverter;
	protected TableCreator tableCreator;
	protected Random random = new Random();

	public DBExecutorBase(FactoryService factorySvc, Log sqlLog, DBErrorConverter errorConverter) {
		super(factorySvc);
		this.sqlLog = sqlLog;
		this.errorConverter = errorConverter;
	}

	public void init1(DTypeRegistry registry) {
		this.init1HasBeenDone = true;
		this.registry = registry;
	}

	protected TableCreator createPartialTableCreator(DBExecutor zexec) {
		SqlNameFormatter nameFormatter = new SimpleSqlNameFormatter();
		FieldGenFactory fieldGenFactory = createFieldGenFactory();
		return createZTableCreator(fieldGenFactory, nameFormatter, null, zexec);
	}

	public void init2(DatIdMap datIdMap, VarEvaluator varEvaluator, DBExecutor zexec) {
		this.init2HasBeenDone = true;
		this.datIdMap = datIdMap;
		this.varEvaluator = varEvaluator;

		SqlNameFormatter nameFormatter = new SimpleSqlNameFormatter();
		FieldGenFactory fieldGenFactory = createFieldGenFactory();
		this.tableCreator = createZTableCreator(fieldGenFactory, nameFormatter, datIdMap, zexec);
	}
	
	protected TableCreator createZTableCreator(FieldGenFactory fieldGenFactory, SqlNameFormatter nameFormatter, DatIdMap datIdMap, DBExecutor zexec) {
		return  new TableCreator(factorySvc, registry, fieldGenFactory, nameFormatter, datIdMap, zexec);
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

	protected void convertAndRethrow(DBValidationException e, DBExecutor zexec) {
		TableCreator tmp = tableCreator == null ? createPartialTableCreator(zexec) : tableCreator;
		errorConverter.convertAndRethrow(e, tmp.alreadyCreatedL);
	}

	protected SqlStatement createSqlStatement(String sql) {
		SqlStatement statement = new SqlStatement(null);
		statement.sql = sql;
		return statement;
	}
	protected DBExecuteContext createContext() {
		DBExecuteContext dbctx = new DBExecuteContext();
		dbctx.logToUse = log;
		return dbctx;
	}

	protected boolean execResultBoolean(DBConnection conn, SqlStatement statement) {
		logSql(statement);

		DBExecuteContext dbctx = createContext();
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
			if (dval == null) {
				joiner.add("null");
			} else if (dval.getType().isShape(Shape.BLOB)) {
				LoggableBlob lb = new LoggableBlob(dval.asString());
				joiner.add(String.format("'%s'", lb.toLoggableHexString()));
			} else {
				joiner.add(String.format("'%s'", dval.asString()));
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

	protected void execSqlStatement(DBConnection conn, String sql) {
		logSql(sql);
		SqlStatement statement = createSqlStatement(sql); 
		DBExecuteContext dbctx = createContext();
		conn.execStatement(statement, dbctx);
	}

	protected String generateUniqueConstraintName() {
		int n = random.nextInt(Integer.MAX_VALUE - 10);
		return String.format("DConstraint_%d", n);
	}
}