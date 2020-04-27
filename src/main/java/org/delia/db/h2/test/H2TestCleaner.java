package org.delia.db.h2.test;

import org.delia.core.FactoryService;
import org.delia.db.DBAccessContext;
import org.delia.db.DBExecutor;
import org.delia.db.DBInterface;
import org.delia.db.DBType;
import org.delia.db.schema.SchemaMigrator;
import org.delia.log.Log;

public class H2TestCleaner {
	
	private DBType dbType;

	public H2TestCleaner(DBType dbType) {
		this.dbType = dbType;
	}

	//h2 persists tables across runs, so cleanup first
	public void deleteKnownTables(FactoryService factorySvc, DBInterface innerInterface) {
		DBExecutor executor = innerInterface.createExector(new DBAccessContext(null, null));
		boolean b = innerInterface.isSQLLoggingEnabled();
		innerInterface.enableSQLLogging(false);
//		System.out.println("dropping...");
		factorySvc.getLog().log("CLEAN tables..");
		safeDeleteTable(executor, "cars;");
		safeDeleteTable(executor, "CustomerAddressAssoc;");
		safeDeleteTable(executor, "AddressCustomerAssoc;");
		safeDeleteTable(executor, "Customer;");
		safeDeleteTable(executor, "CUSTOMERS;");
		safeDeleteTable(executor, "Address;");
		safeDeleteTable(executor, "Customer;");
		safeDeleteTable(executor, "Customer__BAK;");
		safeDeleteTable(executor, "Actor;");
		safeDeleteTable(executor, "Flight;");
		safeDeleteTable(executor, "Flight2;");
		safeDeleteTable(executor, "BASE;");
		safeDeleteTable(executor, "BASE2;");

		String tbl = SchemaMigrator.SCHEMA_TABLE;
		safeDeleteTable(executor, tbl.toLowerCase());
		executor.close();
		innerInterface.enableSQLLogging(b);
	}
	
	public void deleteTables(FactoryService factorySvc, DBInterface innerInterface, String tables) {
		DBExecutor executor = innerInterface.createExector(new DBAccessContext(null, null));
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
		try {
//			String sql = String.format("DROP TABLE IF EXISTS %s;", tblName);
//			System.out.println(sql);
			executor.deleteTable(tblName);
		} catch (Exception e) {
			System.out.print(e.getMessage());
			//e.printStackTrace();
//			System.out.println("exception: " + e.getMessage());
		}
	}

}
