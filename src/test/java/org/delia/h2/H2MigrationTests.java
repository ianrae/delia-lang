package org.delia.h2;

import static org.junit.Assert.assertEquals;

import org.delia.api.DeliaFactory;
import org.delia.core.FactoryService;
import org.delia.core.FactoryServiceImpl;
import org.delia.db.DBAccessContext;
import org.delia.db.DBExecutor;
import org.delia.db.DBType;
import org.delia.db.SchemaContext;
import org.delia.db.h2.H2DBExecutor;
import org.delia.db.h2.H2DBInterface;
import org.delia.db.h2.test.H2TestCleaner;
import org.delia.db.sql.ConnectionFactory;
import org.delia.db.sql.ConnectionFactoryImpl;
import org.delia.error.SimpleErrorTracker;
import org.delia.runner.DoNothingVarEvaluator;
import org.delia.runner.ResultValue;
import org.delia.sort.topo.TopoTestBase;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.junit.Before;
import org.junit.Test;

public class H2MigrationTests extends TopoTestBase {


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
		this.delia.getOptions().useSafeMigrationPolicy = false;

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

		String src = createCustomerType(", x int optional");
		execTypeStatement(src);
		chkTblExists("Customer");

		execStatement("insert Customer {wid:45, x:50}");
		res = this.execStatement("let x = Customer[2]");
		assertEquals(true, res.ok);
		dval = res.getAsDValue();
		assertEquals(2, dval.asStruct().getField("id").asInt());
		assertEquals(45, dval.asStruct().getField("wid").asInt());
		assertEquals(50, dval.asStruct().getField("x").asInt());
		
		log("again...(drop)");
		createNewDelia();
		this.delia.getOptions().useSafeMigrationPolicy = false;

		src = createCustomerType();
		execTypeStatement(src);
		chkTblExists("Customer");

		execStatement("insert Customer {wid:45}");
		res = this.execStatement("let x = Customer[3]");
		assertEquals(true, res.ok);
		dval = res.getAsDValue();
		assertEquals(3, dval.asStruct().getField("id").asInt());
		assertEquals(45, dval.asStruct().getField("wid").asInt());
	}
	//TODO: test add/drop relation field (in parent and child)
	
	@Test
	public void testFieldRename() {
		createCustomerTypeWithSerial();

		execStatement("insert Customer {wid:44}");
		ResultValue res = this.execStatement("let x = Customer[true]");
		assertEquals(true, res.ok);
		DValue dval = res.getAsDValue();
		assertEquals(1, dval.asStruct().getField("id").asInt());
		assertEquals(44, dval.asStruct().getField("wid").asInt());
		
		H2DBExecutor xx = (H2DBExecutor) this.dbInterface.createExector(new DBAccessContext(null, null));
//		xx.getConn().execRawQuery("select * from Customer");
		xx.getConn().execRawQuery("select * from information_schema.tables");
		xx.close();
		
		H2DBInterface dbi = (H2DBInterface) dbInterface;
		dbi.enumerateAllTables(log);
		
		log("again...");
		createNewDelia();

		String src = createCustomerType(", x int optional");
		execTypeStatement(src);
		log("xxxx");
		dbi = (H2DBInterface) dbInterface;
		dbi.enumerateAllTables(log);
		chkTblExists("Customer");

		execStatement("insert Customer {wid:45, x:50}");
		res = this.execStatement("let x = Customer[2]");
		assertEquals(true, res.ok);
		dval = res.getAsDValue();
		assertEquals(2, dval.asStruct().getField("id").asInt());
		assertEquals(45, dval.asStruct().getField("wid").asInt());
		assertEquals(50, dval.asStruct().getField("x").asInt());
		
		log("again...(rename)");
		createNewDelia();
		this.delia.getOptions().useSafeMigrationPolicy = false;

		src = createCustomerType(", y int");
		execTypeStatement(src);
		chkTblExists("Customer");

		execStatement("insert Customer {wid:45, y:50}");
		res = this.execStatement("let x = Customer[3]");
		assertEquals(true, res.ok);
		dval = res.getAsDValue();
		assertEquals(3, dval.asStruct().getField("id").asInt());
		assertEquals(45, dval.asStruct().getField("wid").asInt());
		assertEquals(50, dval.asStruct().getField("y").asInt());
	}
	
	// --
	private FactoryService factorySvc;
	
	@Before
	public void init() {
		this.factorySvc = new FactoryServiceImpl(log, new SimpleErrorTracker(log));
		log("here we go2..");
		ConnectionFactory connFact = new ConnectionFactoryImpl(H2ConnectionHelper.getTestDB(), log);
		dbInterface = new H2DBInterface(factorySvc, connFact);
		dbInterface.enableSQLLogging(true);
		delia = DeliaFactory.create(dbInterface, log, factorySvc);
		delia.getOptions().disableSQLLoggingDuringSchemaMigration = false;
		
		removeTestTables();
	}
	private void removeTestTables() {
		H2TestCleaner cleaner = new H2TestCleaner(DBType.H2);
		cleaner.deleteKnownTables(factorySvc, dbInterface);
		
		DBAccessContext dbctx = new DBAccessContext(null, null);
		DBExecutor dbexecutor = dbInterface.createExector(dbctx);
		SchemaContext ctx = new SchemaContext();
		dbexecutor.deleteTable("Address__BAK", ctx);
		dbexecutor.deleteTable("Customer__BAK", ctx);
	}

	private void createNewDelia() {
		delia = DeliaFactory.create(dbInterface, log, new FactoryServiceImpl(log, new SimpleErrorTracker(log)));
		delia.getOptions().disableSQLLoggingDuringSchemaMigration = false;
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
		DBAccessContext dbctx = new DBAccessContext(sess.getExecutionContext().registry, new DoNothingVarEvaluator());
		try(DBExecutor dbexecutor = dbInterface.createExector(dbctx)) {
			boolean b = dbexecutor.execTableDetect(tableName);
			assertEquals(expected, b);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
