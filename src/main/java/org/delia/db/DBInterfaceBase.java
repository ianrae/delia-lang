package org.delia.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Random;
import java.util.StringJoiner;

import org.delia.assoc.DatIdMap;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.hls.HLSQueryStatement;
import org.delia.db.hls.ResultTypeInfo;
import org.delia.db.sql.ConnectionFactory;
import org.delia.db.sql.prepared.PreparedStatementGenerator;
import org.delia.db.sql.prepared.SqlStatement;
import org.delia.db.sql.prepared.SqlStatementGroup;
import org.delia.db.sql.table.TableCreator;
import org.delia.log.Log;
import org.delia.log.LogLevel;
import org.delia.log.SimpleLog;
import org.delia.type.DStructType;
import org.delia.type.DValue;
import org.delia.type.Shape;
import org.delia.util.DeliaExceptionHelper;
import org.delia.zqueryresponse.LetSpan;

/**
 * Represents db access to a single database.
 * A DBInterface is a long-lived object and is generally created once
 * per application (per database).
 * 
 * @author Ian Rae
 *
 */
public abstract class DBInterfaceBase extends ServiceBase implements DBInterface {
	protected DBCapabilties capabilities;
	protected Log sqlLog;
	protected TableCreator tableCreator;
	protected ValueHelper valueHelper;
	protected SqlHelperFactory sqlHelperFactory;
	protected ConnectionFactory connFactory;
	protected DBType dbType;
	protected DBErrorConverter errorConverter;
	protected Random random = new Random();
	protected ResultSetToDValConverter resultSetConverter;

	public DBInterfaceBase(DBType dbType, FactoryService factorySvc, ConnectionFactory connFactory, SqlHelperFactory sqlhelperFactory) {
		super(factorySvc);
		this.dbType = dbType;
		this.capabilities = new DBCapabilties(true, true, true, true);
		this.sqlLog = new SimpleLog();
		this.connFactory = connFactory;
		this.sqlHelperFactory = sqlhelperFactory;
		this.valueHelper = sqlHelperFactory.createValueHelper();
		this.resultSetConverter = new ResultSetToDValConverter(dbType, factorySvc, connFactory, sqlhelperFactory);
	}

	@Override
	public DBCapabilties getCapabilities() {
		return capabilities;
	}

	@Override
	public abstract DBExecutor createExector(DBAccessContext ctx);

	@Override
	public void init(FactoryService factorySvc) {
		this.factorySvc = factorySvc;
		this.log = factorySvc.getLog();
		this.et = factorySvc.getErrorTracker();
		this.resultSetConverter.init(factorySvc);
	}

	/**
	 * we can only have generated one key even if did several inserts because
	 * the additional inserts are just the assoc table.
	 * @param ctx
	 * @param sqlctx
	 * @return
	 * @throws SQLException
	 */
	protected DValue extractGeneratedKey(InsertContext ctx, SqlExecuteContext sqlctx) throws SQLException {
		return resultSetConverter.extractGeneratedKey(ctx, sqlctx);
	}

	protected void logSql(String sql) {
		sqlLog.log("SQL: " + sql);
	}
	protected void logStatementGroup(SqlStatementGroup stgroup) {
//		sqlLog.log("SQL: " + stgroup.flatten());
		for(SqlStatement stat: stgroup.statementL) {
			this.logSql(stat);
		}
	}

	protected List<DValue> buildDValueList(ResultSet rs, DStructType dtype, QueryDetails details, DBAccessContext dbctx, HLSQueryStatement hls) {
		return resultSetConverter.buildDValueList(rs, dtype, details, dbctx, hls);
	}
	
	
	protected List<DValue> buildScalarResult(ResultSet rs, ResultTypeInfo selectResultType, QueryDetails details, DBAccessContext dbctx) {
		return resultSetConverter.buildScalarResult(rs, selectResultType, details, dbctx);
	}
	

	protected PreparedStatementGenerator createPrepSqlGen(DBAccessContext dbctx) {
		return sqlHelperFactory.createPrepSqlGen(createExistService(dbctx), dbctx);
	}
//	private InsertStatementGenerator createPrepInsertSqlGen(DBAccessContext dbctx) {
//		TableExistenceService existSvc = createExistService(dbctx);
//		return sqlHelperFactory.createPrepInsertSqlGen(dbctx, existSvc);
//	}
	protected TableExistenceService createExistService(DBAccessContext dbctx) {
		TableExistenceService existSvc = new TableExistenceServiceImpl(this, dbctx);
		return existSvc;
	}
	
	protected synchronized TableCreator createTableCreator(DBAccessContext dbctx) {
		if (tableCreator == null) {
			DatIdMap datIdMap = null; //TODO is this ok?
			this.tableCreator = sqlHelperFactory.createTableCreator(dbctx, datIdMap);
		}
		return tableCreator;
	}
//	private FKSqlGenerator createFKSqlGen(List<TableInfo> tblinfoL, DBAccessContext dbctx) {
//		return sqlHelperFactory.createFKSqlGen(tblinfoL, dbctx, createExistService(dbctx));
//	}
	
	@Override
	public boolean isSQLLoggingEnabled() {
		return !LogLevel.OFF.equals(sqlLog.getLevel());
	}
	
	@Override
	public void enableSQLLogging(boolean b) {
		if (b) {
			sqlLog.setLevel(LogLevel.INFO);
		} else {
			sqlLog.setLevel(LogLevel.OFF);
		}
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
	@Override
	public DBType getDBType() {
		return dbType;
	}
	
	protected String generateUniqueConstraintName() {
		int n = random.nextInt(Integer.MAX_VALUE - 10);
		return String.format("DConstraint_%d", n);
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
	protected void failIfMultiSpan(QuerySpec spec, QueryContext qtx, List<LetSpan> spanL) {
		if (qtx.letSpanEngine == null) return;
		
		if (spanL.size() > 1) {
			String msg = "Query of '%s' contains %d spans. Only one span supported in current version";
			DeliaExceptionHelper.throwError("db-multiple-spans-not-supported", msg, spec.queryExp.typeName, spanL.size());
		}
	}
	protected List<LetSpan> buildSpans(QuerySpec spec, QueryContext qtx) {
		if (qtx.letSpanEngine == null) return null;
		
		List<LetSpan> spanL = qtx.letSpanEngine.buildAllSpans(spec.queryExp);
		return spanL;
	}


}