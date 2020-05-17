//package org.delia.db.h2;
//
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.sql.Statement;
//
//import org.delia.core.FactoryService;
//import org.delia.db.DBConnectionBase;
//import org.delia.db.DBErrorConverter;
//import org.delia.db.sql.ConnectionFactory;
//import org.delia.db.sql.prepared.PreparedStatementGenerator;
//
//public class H2DBConnection extends DBConnectionBase {
//
//	public H2DBConnection(FactoryService factorySvc, ConnectionFactory connectionFactory, DBErrorConverter errorConverter) {
//		super(factorySvc, connectionFactory, errorConverter);
//	}
//
//	public boolean newExecTableDetect(String tableName, PreparedStatementGenerator sqlgen, boolean disableSqlLogging) {
//		return super.doesTableExist(tableName, sqlgen, disableSqlLogging);
//	}
//
//	//hacky. don't use
//	public boolean execTableDetect(String tableName) {
//		ResultSet rs = null;
//		try {
//			String sql = String.format("SELECT count(*) from %s;", tableName);
//			log.log("SQL: %s", sql);
//			Statement stm = conn.createStatement();
//			rs = stm.executeQuery(sql);
//		} catch (SQLException e) {
//			String msg = e.getMessage();
//			String target = String.format("Table \"%s\" not found", tableName.toUpperCase());
//			if (msg.indexOf(target) >= 0) {
//				log.logDebug("NO TABLE FOUND: " + tableName);
//				return false;
//			}
//			//				e.printStackTrace();
//		}
//		return true;
//	}
//
//
//	public void close() {
//		try {
//			conn.close();
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		log.logDebug("end.");
//	}
//}