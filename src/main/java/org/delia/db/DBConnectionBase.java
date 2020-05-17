//package org.delia.db;
//
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.sql.Statement;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.StringJoiner;
//
//import org.delia.core.FactoryService;
//import org.delia.core.ServiceBase;
//import org.delia.db.h2.DBListingType;
//import org.delia.db.sql.ConnectionFactory;
//import org.delia.db.sql.prepared.PreparedStatementGenerator;
//import org.delia.db.sql.prepared.SqlStatement;
//import org.delia.db.sql.prepared.SqlStatementGroup;
//import org.delia.log.Log;
//
//public class DBConnectionBase extends ServiceBase {
//	protected Connection conn;
//	protected ConnectionFactory connectionFactory;
//	private ValueHelper valueHelper;
//	private DBErrorConverter errorConverter;
//
//	public DBConnectionBase(FactoryService factorySvc, ConnectionFactory connectionFactory, DBErrorConverter errorConverter) {
//		super(factorySvc);
//		this.connectionFactory = connectionFactory;
//		this.valueHelper = new ValueHelper(factorySvc);
//		this.errorConverter = errorConverter;
//	}
//
//	public void openDB() {
//		if (conn != null) {
//			return; //already open
//		}
//
//		conn = connectionFactory.createConnection();
//	}
//	
//	
//	public boolean execStatement(SqlStatement statement, SqlExecuteContext sqlctx) {
//		boolean b = false;
//		try {
//			PreparedStatement stm = createPrepStatement(statement, sqlctx);
//			b = stm.execute();
//		} catch (SQLException e) {
//			convertAndRethrowException(e);
//		}
//		return b;
//	}    
//	
//	
//	public List<Integer> execInsertStatementGroup(SqlStatementGroup stgroup, SqlExecuteContext sqlctx) {
//		//TODO: add batching later
//		List<Integer>insertCountL = new ArrayList<>();
//		for(SqlStatement statement: stgroup.statementL) {
//			int updateCount = executeInsertStatement(statement, sqlctx);
//			insertCountL.add(updateCount);
//		}
//		return insertCountL;
//	}    
//	public int executeInsertStatement(SqlStatement statement, SqlExecuteContext sqlctx) {
//		if (sqlctx.getGeneratedKeys) {
//			return executeInsertAndGenKeysx(statement, sqlctx);
//		}
//		boolean b = false;
//		int affectedRows = 0;
//		try {
//			PreparedStatement stm = createPrepStatement(statement, sqlctx);
//			affectedRows = stm.executeUpdate();
//		} catch (SQLException e) {
//			convertAndRethrowException(e);
//		}
//		log.logDebug("b:%b %d", b, affectedRows);
//		return affectedRows;
//	}
//	private int executeInsertAndGenKeysx(SqlStatement statement, SqlExecuteContext sqlctx) {
//		boolean b = false;
//		int affectedRows = 0;
//		try {
//			PreparedStatement stm = valueHelper.createPrepStatementWithGenKey(statement, conn);
//			affectedRows = stm.executeUpdate();
//			if (affectedRows > 0) {
//				sqlctx.genKeysL.add(stm.getGeneratedKeys());
//			}
//		} catch (SQLException e) {
//			convertAndRethrowException(e);
//		}
//		log.logDebug("b:%b %d", b, affectedRows);
//		return affectedRows;
//	}
//	
//	public int execUpdateStatement(SqlStatement statement, SqlExecuteContext sqlctx) {
//		int updateCount = 0;
//		try {
//			PreparedStatement stm = createPrepStatement(statement, sqlctx);
//			updateCount = stm.executeUpdate();
//		} catch (SQLException e) {
//			convertAndRethrowException(e);
//		}
//		return updateCount;
//	}    
//	public List<Integer> execUpdateStatementGroup(SqlStatementGroup stgroup, SqlExecuteContext sqlctx) {
//		//TODO: add batching later
//		List<Integer>updateCountL = new ArrayList<>();
//		for(SqlStatement statement: stgroup.statementL) {
//			int updateCount = execUpdateStatement(statement, sqlctx);
//			updateCountL.add(updateCount);
//		}
//		return updateCountL;
//	}    
//
//	public ResultSet execQueryStatement(SqlStatement statement, DBAccessContext dbctx) {
//		ResultSet rs = null;
//		try {
//			PreparedStatement stm = createPrepStatement(statement, dbctx);
//			rs = stm.executeQuery();
//			return rs;
//		} catch (SQLException e) {
//			convertAndRethrowException(e);
//		}
//		return rs;
//	}
//	private PreparedStatement createPrepStatement(SqlStatement statement, DBAccessContext dbctx) throws SQLException {
//		return valueHelper.createPrepStatement(statement, conn);
//	}
//	//only used for unit tests
//	public ResultSet execRawQuery(String sql) {
//		ResultSet rs = null;
//		try {
//			Statement stm = conn.createStatement();
//			rs = stm.executeQuery(sql);
//			return rs;
//		} catch (SQLException e) {
//			convertAndRethrowException(e);
//		}
//		return rs;
//	}
//	public int executeRawSql(String sql) {
//		boolean b = false;
//		int x = 0;
//		try {
//			Statement stm = conn.createStatement();
//			b = stm.execute(sql);
//			x = stm.getUpdateCount();
//		} catch (SQLException e) {
//			convertAndRethrowException(e);
//		}
//		log.logDebug("b:%b %d", b, x);
//		return x;
//	}
//
//	private void convertAndRethrowException(SQLException e) {
//		errorConverter.convertAndRethrowException(e);
//	}
//
//	public boolean doesTableExist(String tableName, PreparedStatementGenerator sqlgen, boolean disableSqlLogging) {
//		String sql = sqlgen.generateTableDetect(tableName);
//		return doesTableExistRaw(sql, disableSqlLogging);
//	}
//	public boolean doesTableExistRaw(String sql, boolean disableSqlLogging) {
//		ResultSet rs = null;
//		boolean tblExists = false;
//		try {
//			if (!disableSqlLogging) {
//				log.log("SQL: %s", sql);
//			}
//			Statement stm = conn.createStatement();
//			rs = stm.executeQuery(sql);
//			if (rs != null && rs.next()) {
//				Boolean b = rs.getBoolean(1);
////				System.out.println(b);
//				tblExists = b;
//			}        
//		} catch (SQLException e) {
//			convertAndRethrowException(e);
//		}
//
//		return tblExists;
//	}
//	
//	public boolean execFieldDetect(String tableName, String fieldName, PreparedStatementGenerator sqlgen, boolean disableSqlLogging) {
//		String sql = sqlgen.generateFieldDetect(tableName, fieldName);
//		return execFieldDetectRaw(sql, disableSqlLogging);
//	}
//
//	public boolean execFieldDetectRaw(String sql, boolean disableSqlLogging) {
//		ResultSet rs = null;
//		boolean tblExists = false;
//		try {
//			if (!disableSqlLogging) {
//				log.log("SQL: %s", sql);
//			}
//			Statement stm = conn.createStatement();
//			rs = stm.executeQuery(sql);
//			if (rs != null && rs.next()) {
//				Boolean b = rs.getBoolean(1);
////				System.out.println(b);
//				tblExists = b;
//			}        
//		} catch (SQLException e) {
//			convertAndRethrowException(e);
//		}
//
//		return tblExists;
//	}
//	
//	
//	public int enumerateDBSchema(PreparedStatementGenerator sqlgen, Log logToUse, DBListingType listingType) {
//		String sql = sqlgen.generateSchemaListing(listingType);
//		ResultSet rs = null;
//		try {
//			//String sql = String.format("SELECT count(*) from %s;", tableName);
//			logToUse.log("SQL: %s", sql);
//			Statement stm = conn.createStatement();
//			rs = stm.executeQuery(sql);
//			String title = listingType.name();
//			if (rs != null) {
//				logToUse.log("--- list of %s ---", title);
//				int n = ResultSetHelper.getColumnCount(rs);
//				while(rs.next()) {
//					StringJoiner joiner = new StringJoiner(",");
//					for(int i = 0; i < n; i++) {
//						Object obj = rs.getObject(i+1);
//						joiner.add(obj == null ? "null" : obj.toString());
//					}
//					log.log(joiner.toString());
//				}
//				logToUse.log("--- end list of %s ---", title);
//			}        
//		} catch (SQLException e) {
//			convertAndRethrowException(e);
//		}
//
//		return 9999;
//	}
//	
//	public String findConstraint(PreparedStatementGenerator sqlgen, String tableName, String fieldName, String constraintType) {
//		String sql = sqlgen.generateSchemaListing(DBListingType.ALL_CONSTRAINTS);
//		ResultSet rs = null;
//		try {
//			log.log("SQL: %s", sql);
//			Statement stm = conn.createStatement();
//			rs = stm.executeQuery(sql);
//			if (rs != null) {
//				int n = ResultSetHelper.getColumnCount(rs);
//				//so we want to enumerate and capture
//				int iConstrainName = 3;
//				int iConstrainType = 4;
//				int iTable = 7;
//				int iColumn = 10;
//				
//				while(rs.next()) {
//					String cname = getRsValue(rs, iConstrainName);
//					String ctype= getRsValue(rs, iConstrainType);
//					String tbl = getRsValue(rs, iTable);
//					String field = getRsValue(rs, iColumn);
//					
//					//for now assume only one
//					if (tableName.equalsIgnoreCase(tbl) && fieldName.equalsIgnoreCase(field)) {
//						if (constraintType.equalsIgnoreCase(ctype)) {
//							return cname;
//						}
//					}
//				}
//			}        
//		} catch (SQLException e) {
//			convertAndRethrowException(e);
//		}
//
//		return null;
//	}
//
//	
//	private String getRsValue(ResultSet rs, int index) throws SQLException {
//		Object obj = rs.getObject(index);
//		return obj == null ? "" : obj.toString();
//	}
//
//	public void close() {
//		try {
//			conn.close();
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//		log.logDebug("end.");
//	}   
//	
//	
//}