package org.delia.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.delia.core.FactoryService;
import org.delia.log.Log;
import org.delia.log.LogLevel;
import org.delia.zdb.DBExecutor;
import org.delia.zdb.DBInterfaceFactory;
import org.delia.zdb.h2.H2ZDBExecutor;
import org.delia.zdb.postgres.PostgresZDBExecutor;

public class DBTableRemover {
	
	protected DBType dbType;
	protected DBInterfaceFactory dbInterface;
	protected Log log;

	public DBTableRemover(DBType dbType) {
		this.dbType = dbType;
	}

	//h2 persists tables across runs, so cleanup first
	public void deleteKnownTables(FactoryService factorySvc, DBInterfaceFactory innerInterface, List<String> tableL) {
		this.dbInterface = innerInterface;
		this.log = factorySvc.getLog();
		boolean b = innerInterface.isSQLLoggingEnabled();
		
		try(DBExecutor executor = innerInterface.createExecutor()) {
			innerInterface.enableSQLLogging(false);
			LogLevel saveLevel = executor.getLog().getLevel();
			executor.getLog().setLevel(LogLevel.OFF);
//			System.out.println("dropping...");
			log.log("CLEAN tables..");
			for(String tbl: tableL) {
				tbl = adjustTblName(tbl);
				safeDeleteTable(executor, tbl);
			}

			executor.getLog().setLevel(saveLevel);
		} catch (Exception e1) {
			DBHelper.handleCloseFailure(e1);
		}
		innerInterface.enableSQLLogging(b);
	}
	
	protected String adjustTblName(String tbl) {
		switch(dbType) {
		case H2:
			return tbl.toUpperCase();
		case POSTGRES:
			return tbl.toLowerCase();
		case MEM:
		default:
			return tbl;
		}
	}

	public void safeDeleteTable(DBExecutor executor, String tblName) {
		tblName = adjustTblName(tblName);
		try {
			if (executor instanceof H2ZDBExecutor) {
				if (!deleteH2TableCascade(executor, tblName)) {
					executor.deleteTable(tblName);
				}
//				deleteContraintsForTable(executor, tblName);
			} else if (executor instanceof PostgresZDBExecutor) {
				if (!deletePGTableCascade(executor, tblName)) {
					executor.deleteTable(tblName);
				}
			} else {
				executor.deleteTable(tblName);
			}
		} catch (Exception e) {
			System.out.print(e.getMessage());
		}
	}

	public void deleteContraintsForTable(String tblName) {
		try(DBExecutor zexec = dbInterface.createExecutor()) {
			deleteContraintsForTable(zexec, tblName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}	
	
	public void deleteContraintsForTable(DBExecutor executor, String tblName) throws SQLException {
		String sql = String.format("SELECT CONSTRAINT_NAME FROM information_schema.constraints WHERE  table_schema = 'PUBLIC' and table_name = '%s'", tblName);
		if (executor instanceof H2ZDBExecutor) {
			H2ZDBExecutor h2exec = (H2ZDBExecutor) executor;
			SqlStatement statement = new SqlStatement(null);
			statement.sql = sql;
			ResultSet rs = h2exec.getDBConnection().execQueryStatement(statement, null);
			while(rs.next()) {
				String s = rs.getString(1);
				if (rs.wasNull()) {
					s = null;
				}
				
				log.logDebug("\n%s: ndropping CONSTRAINT: %s", tblName, s);
				sql = String.format("ALTER TABLE %s DROP constraint %s", tblName, s);
				SqlStatement statement2 = new SqlStatement(null);
				statement2.sql = sql;
				h2exec.getDBConnection().execStatement(statement2, null);
		    }
			
			sql = String.format("DROP TABLE if exists %s cascade;", tblName);
			log.logDebug(sql);
			statement = new SqlStatement(null);
			statement.sql = sql;
			h2exec.getDBConnection().execStatement(statement, null);
		}
	}
	protected boolean deleteH2TableCascade(DBExecutor executor, String tblName) throws SQLException {
		if (executor instanceof H2ZDBExecutor) {
			H2ZDBExecutor h2exec = (H2ZDBExecutor) executor;
			String sql = String.format("DROP TABLE if exists %s cascade;", tblName);
			//log.log(sql);
			SqlStatement statement = new SqlStatement(null);
			statement.sql = sql;
			h2exec.getDBConnection().execStatement(statement, null);
			return true;
		} else {
			return false;
		}
	}
	protected boolean deletePGTableCascade(DBExecutor executor, String tblName) throws SQLException {
		if (executor instanceof PostgresZDBExecutor) {
			PostgresZDBExecutor h2exec = (PostgresZDBExecutor) executor;
			String sql = String.format("DROP TABLE if exists %s cascade;", tblName);
			//log.log(sql);
			SqlStatement statement = new SqlStatement(null);
			statement.sql = sql;
			h2exec.getDBConnection().execStatement(statement, null);
			return true;
		} else {
			return false;
		}
	}

}
