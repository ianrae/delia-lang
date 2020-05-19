package org.delia.db.h2.test;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.delia.core.FactoryService;
import org.delia.db.DBHelper;
import org.delia.db.DBType;
import org.delia.db.schema.SchemaMigrator;
import org.delia.db.sql.prepared.SqlStatement;
import org.delia.log.Log;
import org.delia.log.LogLevel;
import org.delia.zdb.ZDBExecutor;
import org.delia.zdb.ZDBInterfaceFactory;
import org.delia.zdb.h2.H2ZDBExecutor;

public class H2TestCleaner {
	
	private DBType dbType;
	private ZDBInterfaceFactory dbInterface;
	private Log log;

	public H2TestCleaner(DBType dbType) {
		this.dbType = dbType;
	}

	//h2 persists tables across runs, so cleanup first
	public void deleteKnownTables(FactoryService factorySvc, ZDBInterfaceFactory innerInterface) {
		this.dbInterface = innerInterface;
		this.log = factorySvc.getLog();
		boolean b = innerInterface.isSQLLoggingEnabled();
		
		try(ZDBExecutor executor = innerInterface.createExecutor()) {
			innerInterface.enableSQLLogging(false);
//			System.out.println("dropping...");
			log.log("CLEAN tables..");
			safeDeleteTable(executor, "cars");
			safeDeleteTable(executor, "CustomerAddressAssoc");
			safeDeleteTable(executor, "AddressCustomerAssoc");
			safeDeleteTable(executor, "AddressCustomerDat1");
			safeDeleteTable(executor, "Customer");
			safeDeleteTable(executor, "CUSTOMERS");
			safeDeleteTable(executor, "Address");
			safeDeleteTable(executor, "Customer");
			safeDeleteTable(executor, "Address");
			safeDeleteTable(executor, "CustomerAddressAssoc");
			safeDeleteTable(executor, "AddressCustomerAssoc");
			safeDeleteTable(executor, "AddressCustomerDat1");
			safeDeleteTable(executor, "CUSTOMERADDRESSDAT1");
			safeDeleteTable(executor, "Customer__BAK");
			safeDeleteTable(executor, "Other");
			safeDeleteTable(executor, "CustomerOtherDat1");
			safeDeleteTable(executor, "Other__BAK");
			
			safeDeleteTable(executor, "Actor");
			safeDeleteTable(executor, "Flight");
			safeDeleteTable(executor, "Flight2");
			safeDeleteTable(executor, "BASE");
			safeDeleteTable(executor, "BASE2");
			safeDeleteTable(executor, "Category");
			safeDeleteTable(executor, "Product");

			String tbl = SchemaMigrator.SCHEMA_TABLE;
			safeDeleteTable(executor, tbl.toLowerCase());
			tbl = SchemaMigrator.DAT_TABLE;
			safeDeleteTable(executor, tbl.toLowerCase());
		} catch (Exception e1) {
			DBHelper.handleCloseFailure(e1);
		}
		innerInterface.enableSQLLogging(b);
	}
	
	public void deleteTables(FactoryService factorySvc, ZDBInterfaceFactory innerInterface, String tables) {
		try(ZDBExecutor executor = innerInterface.createExecutor()) {
			executor.getLog().setLevel(LogLevel.ERROR);
//			boolean b = innerInterface.isSQLLoggingEnabled();
//			innerInterface.enableSQLLogging(false);
//		System.out.println("dropping...");
			Log log = factorySvc.getLog();
			
			log.log("--delete TABLES -- ");
			String[] ar = tables.split(",");
			for(String tbl: ar) {
				tbl = adjustTblName(tbl);
				log.logDebug("delete table: %s", tbl);
				safeDeleteTable(executor, tbl);
			}
		} catch (Exception e) {
			DBHelper.handleCloseFailure(e);
		}
	}

	private String adjustTblName(String tbl) {
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

	public void safeDeleteTable(ZDBExecutor executor, String tblName) {
		tblName = adjustTblName(tblName);
		try {
			if (executor instanceof H2ZDBExecutor) {
				if (!deleteH2TableCascade(executor, tblName)) {
					executor.deleteTable(tblName);
				}
//				deleteContraintsForTable(executor, tblName);
			} else {
				executor.deleteTable(tblName);
			}
		} catch (Exception e) {
			System.out.print(e.getMessage());
		}
	}

	public void deleteContraintsForTable(String tblName) {
		try(ZDBExecutor zexec = dbInterface.createExecutor()) {
			deleteContraintsForTable(zexec, tblName);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
	
	public void deleteContraintsForTable(ZDBExecutor executor, String tblName) throws SQLException {
		String sql = String.format("SELECT CONSTRAINT_NAME FROM information_schema.constraints WHERE  table_schema = 'PUBLIC' and table_name = '%s'", tblName);
		if (executor instanceof H2ZDBExecutor) {
			H2ZDBExecutor h2exec = (H2ZDBExecutor) executor;
			SqlStatement statement = new SqlStatement();
			statement.sql = sql;
			ResultSet rs = h2exec.getDBConnection().execQueryStatement(statement, null);
			while(rs.next()) {
				String s = rs.getString(1);
				if (rs.wasNull()) {
					s = null;
				}
				
				log.logDebug("\n%s: ndropping CONSTRAINT: %s", tblName, s);
				sql = String.format("ALTER TABLE %s DROP constraint %s", tblName, s);
				SqlStatement statement2 = new SqlStatement();
				statement2.sql = sql;
				h2exec.getDBConnection().execStatement(statement2, null);
		    }
			
			sql = String.format("DROP TABLE if exists %s cascade;", tblName);
			log.logDebug(sql);
			statement = new SqlStatement();
			statement.sql = sql;
			h2exec.getDBConnection().execStatement(statement, null);
		}
	}
	private boolean deleteH2TableCascade(ZDBExecutor executor, String tblName) throws SQLException {
		if (executor instanceof H2ZDBExecutor) {
			H2ZDBExecutor h2exec = (H2ZDBExecutor) executor;
			String sql = String.format("DROP TABLE if exists %s cascade;", tblName);
			//log.log(sql);
			SqlStatement statement = new SqlStatement();
			statement.sql = sql;
			h2exec.getDBConnection().execStatement(statement, null);
			return true;
		} else {
			return false;
		}
	}

}
