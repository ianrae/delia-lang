package org.delia.zdb.h2;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.StringJoiner;

import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.DBErrorConverter;
import org.delia.db.ResultSetHelper;
import org.delia.db.SqlStatement;
import org.delia.db.ValueHelper;
import org.delia.db.sql.ConnectionFactory;
import org.delia.type.DType;
import org.delia.zdb.DBConnection;
import org.delia.zdb.DBExecuteContext;

public class H2ZDBConnection extends ServiceBase implements DBConnection {
	protected Connection conn;
	protected ConnectionFactory connectionFactory;
	protected ValueHelper valueHelper;
	protected DBErrorConverter errorConverter;

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
	public ResultSet execQueryStatement(SqlStatement statement, DBExecuteContext dbctx) {
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
	protected PreparedStatement createPrepStatement(SqlStatement statement) throws SQLException {
		return valueHelper.createPrepStatement(statement, conn);
	}
	protected void convertAndRethrowException(SQLException e) {
		errorConverter.convertAndRethrowException(e);
	}

	@Override
	public void execStatement(SqlStatement statement, DBExecuteContext sqlctx) {
		boolean b = false;
		try {
			PreparedStatement stm = createPrepStatement(statement);
			b = stm.execute();
		} catch (SQLException e) {
			convertAndRethrowException(e);
		}
	}

	@Override
	public int executeCommandStatement(SqlStatement statement, DBExecuteContext sqlctx) {
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
	public int executeCommandStatementGenKey(SqlStatement statement, DType keyType, DBExecuteContext sqlctx) {
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
	public void enumerateDBSchema(String sql, String title, DBExecuteContext dbctx) {
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
	public String findConstraint(String sql, String tableName, String fieldName, String constraintType, boolean useFieldName) {
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
					//TODO this needs to be more robust. some constrains have names like:
					//customer_height_key  or 2200_402813_1_not_null
					if (useFieldName) {
						if (tableName.equalsIgnoreCase(tbl) && fieldName.equalsIgnoreCase(field)) {
							if (constraintType.equalsIgnoreCase(ctype)) {
								return cname;
							}
						}
					} else {
						if (tableName.equalsIgnoreCase(tbl) && field.toLowerCase().contains(field)) {
							if (constraintType.equalsIgnoreCase(ctype)) {
								return cname;
							}
						}
					}
					
					
				}
			}        
		} catch (SQLException e) {
			convertAndRethrowException(e);
		}

		return null;
	}
	protected String getRsValue(ResultSet rs, int index) throws SQLException {
		Object obj = rs.getObject(index);
		return obj == null ? "" : obj.toString();
	}
}