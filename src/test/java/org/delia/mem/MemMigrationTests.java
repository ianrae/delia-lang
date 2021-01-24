package org.delia.mem;

import static org.junit.Assert.assertEquals;

import org.delia.api.DeliaFactory;
import org.delia.core.FactoryServiceImpl;
import org.delia.error.SimpleErrorTracker;
import org.delia.runner.ResultValue;
import org.delia.sort.topo.TopoTestBase;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.zdb.DBExecutor;
import org.junit.Before;
import org.junit.Test;

public class MemMigrationTests extends TopoTestBase {


	@Test
	public void test0() {
		createCustomerTypeWithSerial();

		execStatement("insert Customer {wid:44}");
		ResultValue res = this.execStatement("let x = Customer[true]");
		assertEquals(true, res.ok);
		DValue dval = res.getAsDValue();
		assertEquals(1, dval.asStruct().getField("id").asInt());
		assertEquals(44, dval.asStruct().getField("wid").asInt());

		log("and again...");
		//run new delia with same db
		createNewDelia();
		createCustomerTypeWithSerial();
	}

	@Test
	public void testTables() {
		createCustomerTypeWithSerial();

		execStatement("insert Customer {wid:44}");
		ResultValue res = this.execStatement("let x = Customer[true]");
		assertEquals(true, res.ok);
		DValue dval = res.getAsDValue();
		assertEquals(1, dval.asStruct().getField("id").asInt());
		assertEquals(44, dval.asStruct().getField("wid").asInt());

		log("again...");
		//run new delia with same db
		createNewDelia();

		String src = createCustomerType();
		src += createAddressType();
		execTypeStatement(src);
		chkTblExists("Customer");
		chkTblExists("Address");

		log("and again...");
		//run new delia with same db
		createNewDelia();
		delia.getOptions().useSafeMigrationPolicy = false;

		src = createCustomerType();
		execTypeStatement(src);
		chkTblExists("Customer");
		chkTblExists("Address", false);
		
		log("add Address again...");
		//run new delia with same db
		createNewDelia();

		src = createCustomerType();
		src += createAddressType();
		execTypeStatement(src);
		chkTblExists("Customer");
		chkTblExists("Address");
	}
	
	@Test
	public void testFields() {
		createCustomerTypeWithSerial();

		execStatement("insert Customer {wid:44}");
		ResultValue res = this.execStatement("let x = Customer[true]");
		assertEquals(true, res.ok);
		DValue dval = res.getAsDValue();
		assertEquals(1, dval.asStruct().getField("id").asInt());
		assertEquals(44, dval.asStruct().getField("wid").asInt());

		log("again...");
		createNewDelia();

		String src = createCustomerType(", x int");
		execTypeStatement(src);
		chkTblExists("Customer");
		chkTblExists("Address", false);

		execStatement("insert Customer {wid:45, x:50}");
		res = this.execStatement("let x = Customer[2]");
		assertEquals(true, res.ok);
		dval = res.getAsDValue();
		assertEquals(2, dval.asStruct().getField("id").asInt());
		assertEquals(45, dval.asStruct().getField("wid").asInt());
		assertEquals(50, dval.asStruct().getField("x").asInt());
	}

	// --

	@Before
	public void init() {
		super.init();
//		MemDBInterface memDBInterface = (MemDBInterface) dbInterface;
//		memDBInterface.createTablesAsNeededFlag = false;
		dbInterface.getCapabilities().setRequiresSchemaMigration(true);
	}

	private String createCustomerType() {
		return createCustomerType("");
	}
	private String createCustomerType(String extra) {
		String src = String.format("type %s struct { id int primaryKey serial, wid int %s} end", "Customer", extra);
		src += "\n";
		return src;
	}
	private String createAddressType() {
		String src = String.format("type %s struct { id int primaryKey, points int} end", "Address");
		src += "\n";
		return src;
	}

	private void createCustomerTypeWithSerial() {
		String src = createCustomerType();
		execTypeStatement(src);
		DTypeRegistry registry = sess.getExecutionContext().registry;
		DStructType dtype = (DStructType) registry.getType("Customer");
		assertEquals(false, dtype.fieldIsOptional("id"));
		assertEquals(true, dtype.fieldIsPrimaryKey("id"));
		assertEquals(true, dtype.fieldIsSerial("id"));
	}
	private void chkTblExists(String tableName) {
		chkTblExists(tableName, true);
	}
	private void chkTblExists(String tableName, boolean expected) {
		try(DBExecutor zexec = dbInterface.createExecutor()) {
			boolean b = zexec.doesTableExist(tableName);
			assertEquals(expected, b);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void createNewDelia() {
		delia = DeliaFactory.create(dbInterface, log, new FactoryServiceImpl(log, new SimpleErrorTracker(log)));
	}

}
