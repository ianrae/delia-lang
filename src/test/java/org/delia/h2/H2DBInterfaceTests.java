//package org.delia.h2;
//
//import static org.junit.Assert.assertEquals;
//
//import org.delia.base.UnitTestLog;
//import org.delia.compiler.ast.IdentExp;
//import org.delia.compiler.ast.InsertStatementExp;
//import org.delia.compiler.ast.LetStatementExp;
//import org.delia.compiler.ast.QueryExp;
//import org.delia.compiler.ast.TypeStatementExp;
//import org.delia.core.FactoryService;
//import org.delia.core.FactoryServiceImpl;
//import org.delia.db.DBAccessContext;
//import org.delia.db.DBInterface;
//import org.delia.db.DBType;
//import org.delia.db.InsertContext;
//import org.delia.db.QueryContext;
//import org.delia.db.QuerySpec;
//import org.delia.db.TableExistenceService;
//import org.delia.db.TableExistenceServiceImpl;
//import org.delia.db.h2.H2DBConnection;
//import org.delia.db.h2.H2DBInterface;
//import org.delia.db.h2.H2ErrorConverter;
//import org.delia.db.h2.test.H2TestCleaner;
//import org.delia.db.memdb.MemDBInterface;
//import org.delia.db.sql.ConnectionFactory;
//import org.delia.db.sql.ConnectionFactoryImpl;
//import org.delia.db.sql.SimpleSqlNameFormatter;
//import org.delia.db.sql.table.FieldGenFactory;
//import org.delia.db.sql.table.TableCreator;
//import org.delia.error.SimpleErrorTracker;
//import org.delia.log.Log;
//import org.delia.parser.LineChecker;
//import org.delia.runner.CompilerHelper;
//import org.delia.runner.QueryResponse;
//import org.delia.runner.ResultValue;
//import org.delia.runner.Runner;
//import org.delia.runner.RunnerHelper;
//import org.delia.type.DValue;
//import org.delia.zdb.ZDBInterfaceFactory;
//import org.junit.Test;
//
//
//public class H2DBInterfaceTests {
//	
//	@Test
//	public void test1() {
//		TypeStatementExp exp0 = chkType("type Actor struct {id int unique, firstName string} end", "type Actor struct {id int unique, firstName string } end");
//		
//		Runner runner = initMemRunner();
//		ResultValue res = runner.executeOneStatement(exp0);
//		helper.chkResOK(res);
//		assertEquals(true, runner.getRegistry().existsType("Actor"));
//		
//		String sql = this.tableCreator.generateCreateTable("Actor", null);
//		log.log("sql: " + sql);
//		LineChecker checker = new LineChecker(sql);
//		checker.chkLine("CREATE TABLE Actor (");
//		checker.chkLine("id Int UNIQUE,");
//		checker.chkLine("firstName VARCHAR(4096)");
//		checker.chkLine(");");		
//	}
//	
//	@Test
//	public void testDBInterface() throws Exception {
//		Runner runner = initMemRunner();
//		
//		ConnectionFactory connFact = new ConnectionFactoryImpl(H2ConnectionHelper.getTestDB(), log);
//		H2DBConnection conn = new H2DBConnection(factorySvc, connFact, new H2ErrorConverter());
//		conn.openDB();
//		log("here we go2..");
//		H2DBInterface dbi = new H2DBInterface(factorySvc, connFact);
//		dbi.init(factorySvc);
//		this.dbInterface = dbi;
//		
//		log("and..");
//		deleteKnownTables();
//		conn.executeRawSql("DROP TABLE IF EXISTS cars;");
//		conn.executeRawSql("CREATE TABLE cars (id INT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(255), price INT);");
//
//		DBAccessContext dbctx = new DBAccessContext(runner);
//		dbctx.connObject = conn;
////		boolean b = dbi.execTableDetect("cars", dbctx);
////		assertEquals(true, b);
//		
//		boolean b = dbi.doesTableExist("CUSTOMER", dbctx);
//		assertEquals(false, b);
//		dbi.createTable("Customer", dbctx, null);
//		b = dbi.doesTableExist("CUSTOMER", dbctx);
//		assertEquals(true, b);
//		
//		QuerySpec spec = new QuerySpec();
//		spec.queryExp = new QueryExp(99, new IdentExp("Customer"), null, null);
//		QueryContext qtx = new QueryContext();
//		QueryResponse qresp = dbi.executeQuery(spec, qtx, dbctx);
//		chkResQueryEmpty(qresp);
//		
//		conn.close();
//		log("end.");
//	}
//	
//	@Test
//	public void testDBInterfaceInsert() throws Exception {
//		Runner runner = initMemRunner();
//		TypeStatementExp exp0 = chkType("type Actor struct {id int unique, firstName string} end", "type Actor struct {id int unique, firstName string } end");
//		InsertStatementExp exp = chkInsert("insert Actor {id:44, firstName:'bob' }", "insert Actor {id: 44,firstName: 'bob' }");
//		
//		ResultValue res = runner.executeOneStatement(exp0);
//		chkResOK(res);
//		assertEquals(true, runner.getRegistry().existsType("Actor"));
//		
//		//try using the connection factory
//		ConnectionFactory connFactory = new ConnectionFactoryImpl(H2ConnectionHelper.getTestDB(), log);
//		H2DBConnection conn = new H2DBConnection(factorySvc, connFactory, new H2ErrorConverter());
//		conn.openDB();
//		DBAccessContext dbctx = new DBAccessContext(runner);
//		dbctx.connObject = conn;
//		dbInterface.createTable("Actor", dbctx, null); //!! fake schema
//		res = runner.executeOneStatement(exp);
//		chkResOK(res);
//		
//		LetStatementExp exp2 = chkQueryLet("let a = Actor[44]", "let a = Actor[44]");
//		res = runner.executeOneStatement(exp2);
//		QueryResponse qresp = chkResQuery(res, "Actor");
//		DValue dval = qresp.getOne();
//
//		log("here we go2..");
//		H2DBInterface dbi = new H2DBInterface(factorySvc, connFactory);
//		dbi.init(factorySvc);
//		this.dbInterface = dbi;
//
//		log("and..");
//		deleteKnownTables();
//		conn.executeRawSql("CREATE TABLE Actor(id INT PRIMARY KEY AUTO_INCREMENT, firstName VARCHAR(255));");
//
//		QuerySpec spec = new QuerySpec();
//		spec.queryExp = new QueryExp(99, new IdentExp("Actor"), null, null);
//		QueryContext qtx = new QueryContext();
//		qresp = dbi.executeQuery(spec, qtx, dbctx);
//		chkResQueryEmpty(qresp);
//		
//		log("insert..");
//		InsertContext ctx = new InsertContext();
//		dbi.executeInsert(dval, ctx, dbctx);
//
//		log("requery..");
//		spec = new QuerySpec();
//		spec.queryExp = new QueryExp(99, new IdentExp("Actor"), null, null);
//		qresp = dbi.executeQuery(spec, qtx, dbctx);
//		helper.chkResQuery(qresp, "Actor");
//		DValue dval2 = qresp.getOne();
//		assertEquals(44, dval2.asStruct().getField("id").asInt());
//		assertEquals("bob", dval2.asStruct().getField("firstName").asString());
//		
//		conn.close();
//		log("end.");
//	}
//	
//	@Test
//	public void testFieldDetect() throws Exception {
//		Runner runner = initMemRunner();
//		
//		ConnectionFactory connFact = new ConnectionFactoryImpl(H2ConnectionHelper.getTestDB(), log);
//		H2DBConnection conn = new H2DBConnection(factorySvc, connFact, new H2ErrorConverter());
//		conn.openDB();
//		log("here we go2..");
//		H2DBInterface dbi = new H2DBInterface(factorySvc, connFact);
//		dbi.init(factorySvc);
//		this.dbInterface = dbi;
//		
//		log("and..");
//		deleteKnownTables();
//		conn.executeRawSql("DROP TABLE IF EXISTS cars;");
//		conn.executeRawSql("CREATE TABLE cars (id INT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(255), price INT);");
//
//		DBAccessContext dbctx = new DBAccessContext(runner);
//		dbctx.connObject = conn;
//		
////		boolean b1 = dbi.execTableDetect("cars", dbctx);
////		assertEquals(true, b1);
//		boolean b = dbi.doesFieldExist("carx", "nofield", dbctx);
//		assertEquals(false, b);
//		b = dbi.doesFieldExist("car", "nofield", dbctx);
//		assertEquals(false, b);
//		b = dbi.doesFieldExist("CARS", "PRICE", dbctx); //h2 uses uppercase
//		assertEquals(true, b);
//		
//		conn.close();
//		log("end.");
//	}
//	
//	
//	//h2 persists tables across runs, so cleanup first
//	private void deleteKnownTables() {
//		H2TestCleaner cleaner = new H2TestCleaner(DBType.H2);
//		cleaner.deleteKnownTables(factorySvc, dbInterface);
//	}
//
//	// --
//	private ZDBInterfaceFactory dbInterface;
//	private Log log = new UnitTestLog();
//	private SimpleErrorTracker et;
//	private RunnerHelper helper = new RunnerHelper();
//	protected CompilerHelper chelper = new CompilerHelper(null);
//	private FactoryService factorySvc;
//	private TableCreator tableCreator;
//
//
//	private Runner initMemRunner()  {
//		this.et = new SimpleErrorTracker(log);
//		dbInterface = new MemDBInterface();
////		dbInterface.createTable("Customer"); //!! fake schema
//		
//		factorySvc = new FactoryServiceImpl(log, et);
//		Runner runner = helper.create(factorySvc, dbInterface);
//
//		TableExistenceService existSvc = new TableExistenceServiceImpl(dbInterface, new DBAccessContext(runner));
//		this.tableCreator = new TableCreator(factorySvc, runner.getRegistry(), new FieldGenFactory(factorySvc), new SimpleSqlNameFormatter(), existSvc, null);
//		return runner;
//	}
//	
//	private void log(String msg) {
//		System.out.println(msg);
//	}
//	private void chkResOK(ResultValue res) {
//		helper.chkResOK(res);
//	}
//	private QueryResponse chkResQuery(ResultValue res, String typeName) {
//		return helper.chkResQuery(res, typeName);
//	}
//	private TypeStatementExp chkType(String input, String output) {
//		return chelper.chkType(input, output);
//	}
//	private InsertStatementExp chkInsert(String input, String output) {
//		return chelper.chkInsert(input, output);
//	}
//	private LetStatementExp chkQueryLet(String input, String output) {
//		return chelper.chkQueryLet(input, output);
//	}
//	private void chkResQueryEmpty(QueryResponse qresp) {
//		chelper.chkResQueryEmpty(qresp);
//	}
//
//}
