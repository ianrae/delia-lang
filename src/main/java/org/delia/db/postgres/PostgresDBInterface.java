package org.delia.db.postgres;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.delia.core.FactoryService;
import org.delia.db.DBAccessContext;
import org.delia.db.DBExecutor;
import org.delia.db.DBInterfaceBase;
import org.delia.db.DBInterfaceInternal;
import org.delia.db.DBType;
import org.delia.db.DBValidationException;
import org.delia.db.InsertContext;
import org.delia.db.QueryContext;
import org.delia.db.QueryDetails;
import org.delia.db.QuerySpec;
import org.delia.db.SqlExecuteContext;
import org.delia.db.h2.DBListingType;
import org.delia.db.h2.H2DBConnection;
import org.delia.db.sql.ConnectionFactory;
import org.delia.db.sql.prepared.FKSqlGenerator;
import org.delia.db.sql.prepared.InsertStatementGenerator;
import org.delia.db.sql.prepared.PreparedStatementGenerator;
import org.delia.db.sql.prepared.SelectFuncHelper;
import org.delia.db.sql.prepared.SqlStatement;
import org.delia.db.sql.table.TableCreator;
import org.delia.log.Log;
import org.delia.runner.QueryResponse;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DValue;
import org.delia.util.DeliaExceptionHelper;


/**
 * Represents db access to a single Postgres database
 * @author Ian Rae
 *
 */
public class PostgresDBInterface extends DBInterfaceBase implements DBInterfaceInternal {

	public PostgresDBInterface(FactoryService factorySvc, ConnectionFactory connFactory) {
		super(DBType.POSTGRES, factorySvc, connFactory, new PostgresSqlHelperFactory(factorySvc));
		this.errorConverter = this.sqlHelperFactory.createErrorConverter();
		this.connFactory.setErrorConverter(errorConverter);
	}
	
	@Override
	public DBExecutor createExector(DBAccessContext ctx) {
		H2DBConnection conn;
		//we can use the h2 connection class
		conn = new H2DBConnection(factorySvc, connFactory, errorConverter);
		PostgresDBExecutor dbexecutor = new PostgresDBExecutor(this, ctx, conn);
		ctx.connObject = conn;
		conn.openDB();
		return dbexecutor;
	}
	
	@Override
	public DValue executeInsert(DValue dval, InsertContext ctx, DBAccessContext dbctx) {
		createTableCreator(dbctx);

		SqlExecuteContext sqlctx = new SqlExecuteContext(dbctx);
		InsertStatementGenerator sqlgen = createPrepInsertSqlGen(dbctx);
		SqlStatement statement = sqlgen.generateInsert(dval, tableCreator.alreadyCreatedL);
		logSql(statement);
		H2DBConnection conn = (H2DBConnection) dbctx.connObject;
		try {
			sqlctx.getGeneratedKeys = ctx.extractGeneratedKeys;
			int n = conn.executeInsertStatement(statement, sqlctx); 
		} catch (DBValidationException e) {
			convertAndRethrow(e, dbctx);
		}

		DValue genVal = null;
		if (ctx.extractGeneratedKeys && sqlctx.genKeys != null) {
			try {
				genVal = extractGeneratedKey(ctx, sqlctx);
			} catch (SQLException e) {
				DeliaExceptionHelper.throwError("extract-generated-key-failed", e.getMessage());
			}
		}
		return genVal;
	}
//	static int countdownHack = 2;

	@Override
	public QueryResponse executeQuery(QuerySpec spec, QueryContext qtx, DBAccessContext dbctx) {
		QueryDetails details = new QueryDetails();
		SqlStatement statement;
		if (qtx.loadFKs) {
			createTableCreator(dbctx);
			FKSqlGenerator smartgen = createFKSqlGen(tableCreator.alreadyCreatedL, dbctx);
			statement = smartgen.generateFKsQuery(spec, details);
		} else {
			PreparedStatementGenerator sqlgen = createPrepSqlGen(dbctx);
			statement = sqlgen.generateQuery(spec);
		}
		
		logSql(statement);
		H2DBConnection conn = (H2DBConnection) dbctx.connObject;
		ResultSet rs = conn.execQueryStatement(statement, dbctx);
		//TODO: do we need to catch and interpret execptions here??

		QueryResponse qresp = new QueryResponse();
		
		SelectFuncHelper sfhelper = sqlHelperFactory.createSelectFuncHelper(dbctx);
		DType selectResultType = sfhelper.getSelectResultType(spec);
		if (selectResultType.isScalarShape()) {
			qresp.dvalList = buildScalarResult(rs, selectResultType, details, dbctx);
			fixupForExist(spec, qresp.dvalList, sfhelper, dbctx);
			qresp.ok = true;
		} else {
			String typeName = spec.queryExp.getTypeName();
			DStructType dtype = (DStructType) dbctx.registry.findTypeOrSchemaVersionType(typeName);
			qresp.dvalList = buildDValueList(rs, dtype, details, dbctx);
			qresp.ok = true;
		}
		return qresp;
	}
	
	private void fixupForExist(QuerySpec spec, List<DValue> dvalList, SelectFuncHelper sfhelper, DBAccessContext dbctx) {
		if (sfhelper.isExistsPresent(spec)) {
			valueHelper.fixupForExist(dvalList, dbctx);
			DValue dval = dvalList.get(0);
		}
	}

	@Override
	public boolean doesTableExist(String tableName, DBAccessContext dbctx) {
		H2DBConnection conn = (H2DBConnection) dbctx.connObject;
		PreparedStatementGenerator sqlgen = createPrepSqlGen(dbctx);
		return conn.newExecTableDetect(tableName, sqlgen, dbctx.disableSqlLogging);
	}

	@Override
	public boolean doesFieldExist(String tableName, String fieldName, DBAccessContext dbctx) {
		H2DBConnection conn = (H2DBConnection) dbctx.connObject;
		PreparedStatementGenerator sqlgen = createPrepSqlGen(dbctx);
		return conn.execFieldDetect(tableName, fieldName, sqlgen, dbctx.disableSqlLogging);
	}
	

	@Override
	public void executeDelete(QuerySpec spec, DBAccessContext dbctx) {
		PreparedStatementGenerator sqlgen = createPrepSqlGen(dbctx);
		SqlStatement statement = sqlgen.generateDelete(spec);
		logSql(statement);
		createTableCreator(dbctx);
		H2DBConnection conn = (H2DBConnection) dbctx.connObject;
		try {
			SqlExecuteContext sqlctx = new SqlExecuteContext(dbctx);
			boolean b = conn.execStatement(statement, sqlctx); 
			//TODO: what to do if b is false?
		} catch (DBValidationException e) {
			convertAndRethrow(e, dbctx);
		}
	}
	
	@Override
	public int executeUpdate(QuerySpec spec, DValue dval, DBAccessContext dbctx) {
		createTableCreator(dbctx);
		PreparedStatementGenerator sqlgen = createPrepSqlGen(dbctx);
		SqlStatement statement = sqlgen.generateUpdate(dval, tableCreator.alreadyCreatedL, spec);
		if (statement.sql.isEmpty()) {
			return 0; //nothing to update
		}
		
		logSql(statement);
		H2DBConnection conn = (H2DBConnection) dbctx.connObject;
		int updateCount = 0;
		try {
			SqlExecuteContext sqlctx = new SqlExecuteContext(dbctx);
			updateCount = conn.execUpdateStatement(statement, sqlctx); 
		} catch (DBValidationException e) {
			convertAndRethrow(e, dbctx);
		}
		
		return updateCount;
	}
	
	protected int executeSQL(String sql, DBAccessContext ctx) {
		logSql(sql);
		int updateCount = 0;
		try {
			H2DBConnection conn = (H2DBConnection) ctx.connObject;
			updateCount = conn.executeRawSql(sql);
		} catch (DBValidationException e) {
			convertAndRethrow(e, ctx);
		}
		return updateCount;
	}

	@Override
	public void createTable(String tableName, DBAccessContext dbctx) {
		DStructType dtype = dbctx.registry.findTypeOrSchemaVersionType(tableName);
		String sql;
		createTableCreator(dbctx);
		sql = tableCreator.generateCreateTable(tableName, dtype);
		executeSQL(sql, dbctx);
	}	@Override
	public void deleteTable(String tableName, DBAccessContext dbctx) {
		String sql = String.format("DROP TABLE IF EXISTS %s;", tableName);
		executeSQL(sql, dbctx);
	}
	@Override
	public void renameTable(String tableName, String newTableName, DBAccessContext dbctx) {
		String sql = String.format("ALTER TABLE %s RENAME TO %s", tableName, newTableName);
		executeSQL(sql, dbctx);
	}
	@Override
	public void createField(String typeName, String fieldName, DBAccessContext dbctx) {
		TableCreator creator = this.sqlHelperFactory.createTableCreator(dbctx);
		String sql = creator.generateCreateField(typeName, null, fieldName);
		executeSQL(sql, dbctx);
	}

	@Override
	public void deleteField(String typeName, String field, DBAccessContext dbctx) {
		String sql = String.format("ALTER TABLE %s DROP COLUMN %s", typeName, field);
		executeSQL(sql, dbctx);
	}

	// ---
	private void convertAndRethrow(DBValidationException e, DBAccessContext ctx) {
		createTableCreator(ctx);
		errorConverter.convertAndRethrow(e, tableCreator.alreadyCreatedL);
	}

	@Override
	public void renameField(String typeName, String fieldName, String newName, DBAccessContext dbctx) {
		TableCreator creator = this.sqlHelperFactory.createTableCreator(dbctx);
		String sql = creator.generateRenameField(typeName, fieldName, newName);
		executeSQL(sql, dbctx);
	}

	@Override
	public void alterFieldType(String typeName, String fieldName, String newFieldType, DBAccessContext dbctx) {
		TableCreator creator = this.sqlHelperFactory.createTableCreator(dbctx);
		String sql = creator.generateAlterFieldType(typeName, fieldName, newFieldType);
		executeSQL(sql, dbctx);
	}

	@Override
	public void alterField(String tableName, String fieldName, String deltaFlags,
			DBAccessContext dbctx) {
		TableCreator creator = this.sqlHelperFactory.createTableCreator(dbctx);
		String constraintName = String.format("%s_%s_key", creator.tblName(tableName), creator.tblName(fieldName));		
		String sql = creator.generateAlterField(tableName, fieldName, deltaFlags, constraintName);
		executeSQL(sql, dbctx);
	}
	
	//---------------- DBInterfaceInternal -------------------
	@Override
	public String getConnectionSummary() {
		return connFactory.getConnectionSummary();
	}

	@Override
	public void enablePrintStackTrace(boolean b) {
		this.errorConverter.setPrintStackTraceEnabled(b);
	}

	@Override
	public void enumerateAllTables(Log logToUse) {
		DBAccessContext dbctx = new DBAccessContext(null, null);
		PostgresDBExecutor exec = (PostgresDBExecutor) this.createExector(dbctx);
		
		PreparedStatementGenerator sqlgen = createPrepSqlGen(dbctx);
		exec.getConn().enumerateDBSchema(sqlgen, logToUse, DBListingType.ALL_TABLES);
		exec.close();
	}
	@Override
	public void enumerateAllConstraints(Log logToUse) {
		DBAccessContext dbctx = new DBAccessContext(null, null);
		PostgresDBExecutor exec = (PostgresDBExecutor) this.createExector(dbctx);
		
		PreparedStatementGenerator sqlgen = createPrepSqlGen(dbctx);
		exec.getConn().enumerateDBSchema(sqlgen, logToUse, DBListingType.ALL_CONSTRAINTS);
		exec.close();
	}
	
	
}