package org.delia.zdb;

import static org.junit.Assert.assertEquals;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import org.delia.api.Delia;
import org.delia.api.DeliaSession;
import org.delia.assoc.DatIdMap;
import org.delia.bdd.NewBDDBase;
import org.delia.builder.ConnectionBuilder;
import org.delia.builder.ConnectionInfo;
import org.delia.builder.DeliaBuilder;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.dao.DeliaDao;
import org.delia.db.DBAccessContext;
import org.delia.db.DBCapabilties;
import org.delia.db.DBErrorConverter;
import org.delia.db.DBInterface;
import org.delia.db.DBType;
import org.delia.db.InsertContext;
import org.delia.db.QueryContext;
import org.delia.db.QueryDetails;
import org.delia.db.QuerySpec;
import org.delia.db.ResultSetHelper;
import org.delia.db.ResultSetToDValConverter;
import org.delia.db.TableExistenceService;
import org.delia.db.TableExistenceServiceImpl;
import org.delia.db.ValueHelper;
import org.delia.db.h2.DBListingType;
import org.delia.db.h2.H2DBConnection;
import org.delia.db.h2.H2DBExecutor;
import org.delia.db.h2.H2ErrorConverter;
import org.delia.db.hls.HLSQuerySpan;
import org.delia.db.hls.HLSQueryStatement;
import org.delia.db.hls.HLSSelectHelper;
import org.delia.db.hls.ResultTypeInfo;
import org.delia.db.memdb.MemDBInterface;
import org.delia.db.sql.ConnectionFactory;
import org.delia.db.sql.SimpleSqlNameFormatter;
import org.delia.db.sql.SqlNameFormatter;
import org.delia.db.sql.prepared.RawStatementGenerator;
import org.delia.db.sql.prepared.SqlStatement;
import org.delia.db.sql.table.FieldGenFactory;
import org.delia.db.sql.table.TableCreator;
import org.delia.log.Log;
import org.delia.log.LogLevel;
import org.delia.log.SimpleLog;
import org.delia.runner.DoNothingVarEvaluator;
import org.delia.runner.FetchRunner;
import org.delia.runner.QueryResponse;
import org.delia.runner.VarEvaluator;
import org.delia.type.BuiltInTypes;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.type.Shape;
import org.delia.type.TypeReplaceSpec;
import org.delia.typebuilder.InternalTypeCreator;
import org.delia.valuebuilder.ScalarValueBuilder;
import org.delia.valuebuilder.StructValueBuilder;
import org.delia.zdb.core.ZDBConnection;
import org.delia.zdb.core.ZDBExecuteContext;
import org.delia.zdb.core.ZDBExecutor;
import org.delia.zdb.core.ZDBInterfaceFactory;
import org.delia.zdb.core.ZTableCreator;
import org.delia.zdb.core.mem.MemZDBExecutor;
import org.delia.zdb.core.mem.MemZDBInterfaceFactory;
import org.junit.Before;
import org.junit.Test;

public class ZDBTests  extends NewBDDBase {

	public static class H2ZDBConnection extends ServiceBase implements ZDBConnection {
		protected Connection conn;
		protected ConnectionFactory connectionFactory;
		private ValueHelper valueHelper;
		private DBErrorConverter errorConverter;

		public H2ZDBConnection(FactoryService factorySvc, ConnectionFactory connectionFactory, DBErrorConverter errorConverter) {
			super(factorySvc);
			this.connectionFactory = connectionFactory;
			this.valueHelper = new ValueHelper(factorySvc);
			this.errorConverter = errorConverter;
		}

		@Override
		public void openDB() {
			if (conn != null) {
				return; //already open
			}

			conn = connectionFactory.createConnection();
		}

		@Override
		public void close() {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		@Override
		public ResultSet execQueryStatement(SqlStatement statement, ZDBExecuteContext dbctx) {
			ResultSet rs = null;
			try {
				PreparedStatement stm = createPrepStatement(statement);
				rs = stm.executeQuery();
				return rs;
			} catch (SQLException e) {
				convertAndRethrowException(e);
			}
			return rs;
		}
		private PreparedStatement createPrepStatement(SqlStatement statement) throws SQLException {
			return valueHelper.createPrepStatement(statement, conn);
		}
		private void convertAndRethrowException(SQLException e) {
			errorConverter.convertAndRethrowException(e);
		}

		@Override
		public void execStatement(SqlStatement statement, ZDBExecuteContext sqlctx) {
			boolean b = false;
			try {
				PreparedStatement stm = createPrepStatement(statement);
				b = stm.execute();
			} catch (SQLException e) {
				convertAndRethrowException(e);
			}
		}

		@Override
		public int executeCommandStatement(SqlStatement statement, ZDBExecuteContext sqlctx) {
			boolean b = false;
			int affectedRows = 0;
			try {
				PreparedStatement stm = createPrepStatement(statement);
				affectedRows = stm.executeUpdate();
			} catch (SQLException e) {
				convertAndRethrowException(e);
			}
			log.logDebug("b:%b %d", b, affectedRows);
			return affectedRows;
		}

		@Override
		public int executeCommandStatementGenKey(SqlStatement statement, DType keyType, ZDBExecuteContext sqlctx) {
			boolean b = false;
			int affectedRows = 0;
			try {
				PreparedStatement stm = valueHelper.createPrepStatementWithGenKey(statement, conn);
				affectedRows = stm.executeUpdate();
				if (affectedRows > 0) {
					sqlctx.genKeysL.add(stm.getGeneratedKeys());
				}
			} catch (SQLException e) {
				convertAndRethrowException(e);
			}
			log.logDebug("b:%b %d", b, affectedRows);
			return affectedRows;
		}

		@Override
		public void enumerateDBSchema(String sql, String title, ZDBExecuteContext dbctx) {
			ResultSet rs = null;
			try {
				//String sql = String.format("SELECT count(*) from %s;", tableName);
				dbctx.logToUse.log("SQL: %s", sql);
				Statement stm = conn.createStatement();
				rs = stm.executeQuery(sql);
				if (rs != null) {
					dbctx.logToUse.log("--- list of %s ---", title);
					int n = ResultSetHelper.getColumnCount(rs);
					while(rs.next()) {
						StringJoiner joiner = new StringJoiner(",");
						for(int i = 0; i < n; i++) {
							Object obj = rs.getObject(i+1);
							joiner.add(obj == null ? "null" : obj.toString());
						}
						log.log(joiner.toString());
					}
					dbctx.logToUse.log("--- end list of %s ---", title);
				}        
			} catch (SQLException e) {
				convertAndRethrowException(e);
			}
		}

		@Override
		public String findConstraint(String sql, String tableName, String fieldName, String constraintType) {
			ResultSet rs = null;
			try {
				log.log("SQL: %s", sql);
				Statement stm = conn.createStatement();
				rs = stm.executeQuery(sql);
				if (rs != null) {
					int n = ResultSetHelper.getColumnCount(rs);
					//so we want to enumerate and capture
					int iConstrainName = 3;
					int iConstrainType = 4;
					int iTable = 7;
					int iColumn = 10;

					while(rs.next()) {
						String cname = getRsValue(rs, iConstrainName);
						String ctype= getRsValue(rs, iConstrainType);
						String tbl = getRsValue(rs, iTable);
						String field = getRsValue(rs, iColumn);

						//for now assume only one
						if (tableName.equalsIgnoreCase(tbl) && fieldName.equalsIgnoreCase(field)) {
							if (constraintType.equalsIgnoreCase(ctype)) {
								return cname;
							}
						}
					}
				}        
			} catch (SQLException e) {
				convertAndRethrowException(e);
			}

			return null;
		}
		private String getRsValue(ResultSet rs, int index) throws SQLException {
			Object obj = rs.getObject(index);
			return obj == null ? "" : obj.toString();
		}
	}


	public static class H2ZDBInterfaceFactory extends ServiceBase implements ZDBInterfaceFactory {
		private DBCapabilties capabilities;
		private SimpleLog sqlLog;
		private ConnectionFactory connFactory;
		private DBErrorConverter errorConverter;

		public H2ZDBInterfaceFactory(FactoryService factorySvc, ConnectionFactory connFactory) {
			super(factorySvc);
			this.capabilities = new DBCapabilties(true, true, true, true);
			this.sqlLog = new SimpleLog();
			this.connFactory = connFactory;
			this.errorConverter = new H2ErrorConverter();
			this.connFactory.setErrorConverter(errorConverter);
		}

		@Override
		public DBType getDBType() {
			return DBType.H2;
		}

		@Override
		public DBCapabilties getCapabilities() {
			return capabilities;
		}

		@Override
		public ZDBConnection openConnection() {
			H2ZDBConnection conn;
			conn = new H2ZDBConnection(factorySvc, connFactory, errorConverter);
			return conn;
		}

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

		public DBErrorConverter getErrorConverter() {
			return errorConverter;
		}
	}

	public static class H2ZDBExecutor extends ServiceBase implements ZDBExecutor {

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

		public H2ZDBExecutor(FactoryService factorySvc, Log sqlLog, H2ZDBInterfaceFactory dbInterface, H2ZDBConnection conn) {
			super(factorySvc);
			this.sqlLog = sqlLog;
			this.dbInterface = dbInterface;
			this.conn = conn;
			this.dbType = DBType.H2;
			this.errorConverter = dbInterface.getErrorConverter();
		}

		@Override
		public ZDBConnection getDBConnection() {
			return conn;
		}

		@Override
		public void init1(DTypeRegistry registry) {
			this.init1HasBeenDone = true;
			this.registry = registry;
		}

		@Override
		public void init2(DatIdMap datIdMap, VarEvaluator varEvaluator) {
			this.init2HasBeenDone = true;
			this.datIdMap = datIdMap;
			this.varEvaluator = varEvaluator;
		}

		@Override
		public FetchRunner createFetchRunner() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public DValue rawInsert(DValue dval, InsertContext ctx) {

			if (ctx.extractGeneratedKeys) {
				int n = conn.executeCommandStatementGenKey(statement, keyType, sqlctx);
			} else {
				int n = conn.executeCommandStatement(statement, sqlctx);
				return null;
			}
		}

		@Override
		public QueryResponse rawQuery(QuerySpec spec, QueryContext qtx) {
			ResultSet rs = conn.execQueryStatement(statement, dbctx);
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean rawTableDetect(String tableName) {
			RawStatementGenerator sqlgen = new RawStatementGenerator(factorySvc, dbType);
			String sql = sqlgen.generateTableDetect(tableName);
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


		@Override
		public boolean rawFieldDetect(String tableName, String fieldName) {
			RawStatementGenerator sqlgen = new RawStatementGenerator(factorySvc, dbType);
			String sql = sqlgen.generateFieldDetect(tableName, fieldName);
			SqlStatement statement = new SqlStatement();
			statement.sql = sql;
			return execResultBoolean(statement);
		}

		@Override
		public void rawCreateTable(String tableName) {
			createTable(tableName);
		}

		@Override
		public void performTypeReplacement(TypeReplaceSpec spec) {
			//nothing to do
		}

		@Override
		public DValue executeInsert(DValue dval, InsertContext ctx) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int executeUpdate(QuerySpec spec, DValue dvalPartial, Map<String, String> assocCrudMap) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public int executeUpsert(QuerySpec spec, DValue dvalFull, Map<String, String> assocCrudMap,
				boolean noUpdateFlag) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public void executeDelete(QuerySpec spec) {
			// TODO Auto-generated method stub

		}

		@Override
		public QueryResponse executeHLSQuery(HLSQueryStatement hls, String sql, QueryContext qtx) {
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
				DStructType dtype = (DStructType) dbctx.registry.findTypeOrSchemaVersionType(typeName);
				qresp.dvalList = buildDValueList(rs, dtype, details, dbctx, hls);
				qresp.ok = true;
			}
			return qresp;
		}
		protected List<DValue> buildScalarResult(ResultSet rs, ResultTypeInfo selectResultType, QueryDetails details) {
			DBAccessContext dbctx = new DBAccessContext(registry, new DoNothingVarEvaluator());
			ResultSetToDValConverter resultSetConverter;
			resultSetConverter = new ResultSetToDValConverter(factorySvc, new ValueHelper(factorySvc));
			resultSetConverter.init(factorySvc);
			
			return resultSetConverter.buildScalarResult(rs, selectResultType, details, dbctx);
		}
		

		@Override
		public boolean doesTableExist(String tableName) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean doesFieldExist(String tableName, String fieldName) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void createTable(String tableName) {
			DStructType dtype = registry.findTypeOrSchemaVersionType(tableName);
			String sql;
			if (tableCreator == null) {
				SqlNameFormatter nameFormatter = new SimpleSqlNameFormatter();
				FieldGenFactory fieldGenFactory = new FieldGenFactory(factorySvc);
				this.tableCreator = new ZTableCreator(factorySvc, registry, fieldGenFactory, nameFormatter, datIdMap);
			}
			
			sql = tableCreator.generateCreateTable(tableName, dtype);
			SqlStatement statement = createSqlStatement(sql); 
			ZDBExecuteContext dbctx = createContext();
			conn.execStatement(statement, dbctx);
		}

		@Override
		public void deleteTable(String tableName) {
			// TODO Auto-generated method stub

		}

		@Override
		public void renameTable(String tableName, String newTableName) {
			// TODO Auto-generated method stub

		}

		@Override
		public void createField(String typeName, String field) {
			// TODO Auto-generated method stub

		}

		@Override
		public void deleteField(String typeName, String field, int datId) {
			// TODO Auto-generated method stub

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

	}

	@Test
	public void test() {
		MemZDBInterfaceFactory dbFactory = new MemZDBInterfaceFactory(factorySvc);
		MemZDBExecutor dbexec = new MemZDBExecutor(factorySvc, dbFactory);
		dbexec.init1(registry);

		InternalTypeCreator typeCreator = new InternalTypeCreator();
		String typeName = "DELIA_ASSOC";
		DStructType datType = typeCreator.createDATType(registry, typeName);
		assertEquals(false, dbexec.doesTableExist(datType.getName()));
		dbexec.rawCreateTable(typeName);
		assertEquals(true, dbexec.doesTableExist(datType.getName()));

		DValue dval = createDatTableObj(datType, "dat1");

		InsertContext ictx = new InsertContext();
		ictx.extractGeneratedKeys = true;
		ictx.genKeytype = registry.getType(BuiltInTypes.INTEGER_SHAPE);
		DValue newDatIdValue = dbexec.executeInsert(dval, ictx);
		assertEquals(1, newDatIdValue.asInt());
	}

	// --
	private DeliaDao dao;
	private Delia delia;
	private FactoryService factorySvc;
	private DeliaSession session;
	private DTypeRegistry registry;

	@Before
	public void init() {
		this.dao = createDao();
		this.delia = dao.getDelia();
		this.factorySvc = delia.getFactoryService();

		this.session = delia.beginSession("");
		this.registry = session.getExecutionContext().registry;
	}

	private DeliaDao createDao() {
		ConnectionInfo info = ConnectionBuilder.dbType(DBType.MEM).build();
		Delia delia = DeliaBuilder.withConnection(info).build();
		return new DeliaDao(delia);
	}

	@Override
	public DBInterface createForTest() {
		return new MemDBInterface();
	}

	private DValue createDatTableObj(DStructType type, String datTableName) {
		StructValueBuilder structBuilder = new StructValueBuilder(type);

		ScalarValueBuilder builder = factorySvc.createScalarValueBuilder(registry);
		DValue dval = builder.buildString(datTableName);
		structBuilder.addField("tblName", dval);

		boolean b = structBuilder.finish();
		if (! b) {
			return null;
		}
		dval = structBuilder.getDValue();
		return dval;
	}

}
