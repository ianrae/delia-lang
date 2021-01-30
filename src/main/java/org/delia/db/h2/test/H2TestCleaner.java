package org.delia.db.h2.test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.delia.core.FactoryService;
import org.delia.db.DBTableRemover;
import org.delia.db.DBType;
import org.delia.db.schema.SchemaMigrator;
import org.delia.zdb.DBExecutor;
import org.delia.zdb.DBInterfaceFactory;

public class H2TestCleaner {
	private DBTableRemover tblRemover;

	public H2TestCleaner(DBType dbType) {
		this.tblRemover = new DBTableRemover(dbType);
	}

	//h2 persists tables across runs, so cleanup first
	public void deleteKnownTables(FactoryService factorySvc, DBInterfaceFactory innerInterface) {
		List<String> tableL = new ArrayList<>();
		tableL.add("cars");
		tableL.add("CustomerAddressAssoc");
		tableL.add("AddressCustomerAssoc");
		tableL.add("AddressCustomerDat1");
		tableL.add("CustomerCustomerDat1");
		tableL.add("Customer");
		tableL.add("CUSTOMERS");
		tableL.add("Address");
		tableL.add("Customer");
		tableL.add("Address");
		tableL.add("CustomerAddressAssoc");
		tableL.add("AddressCustomerAssoc");
		tableL.add("AddressCustomerDat1");
		tableL.add("AddressCustomerDat2");
		tableL.add("CustomerAddressDat1");
		tableL.add("CustomerAddressDat2");
		tableL.add("CustomerCustomerDat1");
		tableL.add("Customer__BAK");
		tableL.add("Address__BAK");
		tableL.add("Other");
		tableL.add("CustomerOtherDat1");
		tableL.add("Other__BAK");

		tableL.add("Actor");
		tableL.add("Flight");
		tableL.add("Flight2");
		tableL.add("BASE");
		tableL.add("BASE2");
		tableL.add("Category");
		tableL.add("Product");

		String tbl = SchemaMigrator.SCHEMA_TABLE;
		tableL.add(tbl.toLowerCase());
		tbl = SchemaMigrator.DAT_TABLE;
		tableL.add(tbl.toLowerCase());
		
		tblRemover.deleteKnownTables(factorySvc, innerInterface, tableL);
	}

	public void deleteTables(FactoryService factorySvc, DBInterfaceFactory innerInterface, String tables) {
		String[] ar = tables.split(",");
		List<String> tableL = Arrays.asList(ar);
		tblRemover.deleteKnownTables(factorySvc, innerInterface, tableL);
	}

	public void deleteContraintsForTable(String tblName) {
		tblRemover.deleteContraintsForTable(tblName);
	}	

	public void deleteContraintsForTable(DBExecutor executor, String tblName) throws SQLException {
		tblRemover.deleteContraintsForTable(executor, tblName);
	}
}
