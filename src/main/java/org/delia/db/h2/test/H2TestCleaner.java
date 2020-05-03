package org.delia.db.h2.test;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.delia.core.FactoryService;
import org.delia.db.DBAccessContext;
import org.delia.db.DBExecutor;
import org.delia.db.DBHelper;
import org.delia.db.DBInterface;
import org.delia.db.DBInterfaceInternal;
import org.delia.db.DBType;
import org.delia.db.h2.H2DBExecutor;
import org.delia.db.h2.H2DBInterface;
import org.delia.db.schema.SchemaMigrator;
import org.delia.log.Log;

public class H2TestCleaner {
	
	private DBType dbType;
	private DBInterface dbInterface;
	private Log log;

	public H2TestCleaner(DBType dbType) {
		this.dbType = dbType;
	}

	//h2 persists tables across runs, so cleanup first
	public void deleteKnownTables(FactoryService factorySvc, DBInterface innerInterface) {
		this.dbInterface = innerInterface;
		this.log = factorySvc.getLog();
		boolean b = innerInterface.isSQLLoggingEnabled();
		
		try(DBExecutor executor = innerInterface.createExector(new DBAccessContext(null, null))) {
			innerInterface.enableSQLLogging(false);
//			System.out.println("dropping...");
			log.log("CLEAN tables..");
			safeDeleteTable(executor, "cars");
			safeDeleteTable(executor, "CustomerAddressAssoc");
			safeDeleteTable(executor, "AddressCustomerAssoc");
			safeDeleteTable(executor, "Customer");
			safeDeleteTable(executor, "CUSTOMERS");
			safeDeleteTable(executor, "Address");
			safeDeleteTable(executor, "Customer");
			safeDeleteTable(executor, "Address");
			safeDeleteTable(executor, "CustomerAddressAssoc");
			safeDeleteTable(executor, "AddressCustomerAssoc");
			safeDeleteTable(executor, "Customer__BAK");
			safeDeleteTable(executor, "Actor");
			safeDeleteTable(executor, "Flight");
			safeDeleteTable(executor, "Flight2");
			safeDeleteTable(executor, "BASE");
			safeDeleteTable(executor, "BASE2");

			String tbl = SchemaMigrator.SCHEMA_TABLE;
			safeDeleteTable(executor, tbl.toLowerCase());
		} catch (Exception e1) {
			DBHelper.handleCloseFailure(e1);
		}
		innerInterface.enableSQLLogging(b);
	}
	
	public void deleteTables(FactoryService factorySvc, DBInterface innerInterface, String tables) {
		try(DBExecutor executor = innerInterface.createExector(new DBAccessContext(null, null))) {
			boolean b = innerInterface.isSQLLoggingEnabled();
			innerInterface.enableSQLLogging(false);
//		System.out.println("dropping...");
			Log log = factorySvc.getLog();
			
			log.log("--delete TABLES -- ");
			String[] ar = tables.split(",");
			for(String tbl: ar) {
				tbl = adjustTblName(tbl);
				log.log("delete table: %s", tbl);
				safeDeleteTable(executor, tbl);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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

	public void safeDeleteTable(DBExecutor executor, String tblName) {
		tblName = adjustTblName(tblName);
		try {
			if (executor instanceof H2DBExecutor) {
				this.deleteH2TableCascade(executor, tblName);
//				deleteContraintsForTable(executor, tblName);
				executor.deleteTable(tblName);
			} else {
				executor.deleteTable(tblName);
			}
		} catch (Exception e) {
			System.out.print(e.getMessage());
			
			if (dbInterface instanceof DBInterfaceInternal) {
				DBInterfaceInternal dbi = (DBInterfaceInternal) dbInterface;
//				dbi.enumerateAllConstraints(log);
			}
			
			//e.printStackTrace();
		}
	}

	private void deleteContraintsForTable(DBExecutor executor, String tblName) throws SQLException {
		String sql = String.format("SELECT CONSTRAINT_NAME FROM information_schema.constraints WHERE  table_schema = 'PUBLIC' and table_name = '%s'", tblName);
		if (executor instanceof H2DBExecutor) {
			H2DBExecutor h2exec = (H2DBExecutor) executor;
			ResultSet rs = h2exec.executeRawQuery(sql);
			while(rs.next()) {
				String s = rs.getString(1);
				if (rs.wasNull()) {
					s = null;
				}
				
				log.log("\n%s: ndropping CONSTRAINT: %s", tblName, s);
				sql = String.format("ALTER TABLE %s DROP constraint %s", tblName, s);
				h2exec.executeRawSql(sql);
		    }
			
			sql = String.format("DROP TABLE if exists %s cascade;", tblName);
			log.log(sql);
			h2exec.executeRawSql(sql);
		}
	}
	private void deleteH2TableCascade(DBExecutor executor, String tblName) throws SQLException {
		if (executor instanceof H2DBExecutor) {
			H2DBExecutor h2exec = (H2DBExecutor) executor;
			String sql = String.format("DROP TABLE if exists %s cascade;", tblName);
			//log.log(sql);
			h2exec.executeRawSql(sql);
		}
	}

}
