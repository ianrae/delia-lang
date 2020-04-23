package org.delia.db.sql;

import static org.junit.Assert.assertEquals;

import org.delia.base.UnitTestLog;
import org.delia.compiler.ast.InsertStatementExp;
import org.delia.compiler.ast.TypeStatementExp;
import org.delia.core.FactoryService;
import org.delia.core.FactoryServiceImpl;
import org.delia.db.DBAccessContext;
import org.delia.db.TableExistenceService;
import org.delia.db.TableExistenceServiceImpl;
import org.delia.db.memdb.MemDBInterface;
import org.delia.db.sql.table.FieldGenFactory;
import org.delia.db.sql.table.TableCreator;
import org.delia.error.ErrorTracker;
import org.delia.error.SimpleErrorTracker;
import org.delia.log.Log;
import org.delia.parser.LineChecker;
import org.delia.runner.CompilerHelper;
import org.delia.runner.ResultValue;
import org.delia.runner.Runner;
import org.delia.runner.RunnerHelper;
import org.junit.Test;




public class TableCreatorTests {
	
	@Test
	public void test() {
		TypeStatementExp exp0 = chkType("type Actor struct {id int unique, firstName string, flag boolean} end", "type Actor struct {id int unique, firstName string, flag boolean } end");
		InsertStatementExp exp = chkInsert("insert Actor {id:44, firstName:'bob', flag:true }", "insert Actor {id: 44,firstName: 'bob',flag: true }");
		
		Runner runner = initRunner();
		ResultValue res = runner.executeOneStatement(exp0);
		chkResOK(res);
		assertEquals(true, runner.getRegistry().existsType("Actor"));
		
		String sql = tblCreator.generateCreateTable("Actor", null);
		log.log("sql: " + sql);
		
		LineChecker checker = new LineChecker(sql);
		checker.chkLine("CREATE TABLE Actor (");
		checker.chkLine("id Int UNIQUE,");
		checker.chkLine("firstName VARCHAR(4096),");
		checker.chkLine("flag BOOLEAN");
		checker.chkLine(");");		
	}
	
	
//	
//	@Test
//	public void testCreateTableOld() {
//		TypeStatementExp exp0 = chkType("type Actor struct {id int unique, firstName string, flag boolean} end", "type Actor struct {id int unique, firstName string, flag boolean } end");
//		InsertStatementExp exp = chkInsert("insert Actor {id:44, firstName:'bob', flag:true }", "insert Actor {id: 44,firstName: 'bob',flag: true }");
//		
//		Runner runner = initRunner();
//		ResultValue res = runner.executeOneStatement(exp0);
//		chkResOK(res);
//		assertEquals(true, runner.getRegistry().existsType("Actor"));
//		
//		String sql = sqlgen.generateCreateTable("Actor", null);
//		log.log("sql: " + sql);
//		
//		LineChecker checker = new LineChecker(sql);
//		checker.chkLine("CREATE TABLE Actor (");
//		checker.chkLine("id Int UNIQUE,");
//		checker.chkLine("firstName VARCHAR(255),");
//		checker.chkLine("flag BOOLEAN");
//		checker.chkLine(");");		
//	}
//	
	// --
	private MemDBInterface dbInterface;
	private TableCreator tblCreator;
	private Log log = new UnitTestLog();
	private RunnerHelper helper = new RunnerHelper();
	protected CompilerHelper chelper = new CompilerHelper(null, log);

	private Runner initRunner()  {
		ErrorTracker et = new SimpleErrorTracker(log);
		dbInterface = new MemDBInterface();
//		dbInterface.createTable("Customer"); //!! fake schema
		
		FactoryService factorySvc = new FactoryServiceImpl(log, et);
		Runner runner = helper.create(factorySvc, dbInterface);

		TableExistenceService existSvc = new TableExistenceServiceImpl(dbInterface, new DBAccessContext(runner));
		tblCreator = new TableCreator(factorySvc, runner.getRegistry(), new FieldGenFactory(factorySvc), new SimpleSqlNameFormatter(), existSvc);
		return runner;
	}
	
	private void chkResOK(ResultValue res) {
		helper.chkResOK(res);
	}
	private TypeStatementExp chkType(String input, String output) {
		return chelper.chkType(input, output);
	}
	private InsertStatementExp chkInsert(String input, String output) {
		return chelper.chkInsert(input, output);
	}
}
