package org.delia.db.sql;

import static org.junit.Assert.assertEquals;

import org.delia.assoc.DatIdMap;
import org.delia.base.UnitTestLog;
import org.delia.core.FactoryService;
import org.delia.db.DBAccessContext;
import org.delia.db.DBInterface;
import org.delia.db.TableExistenceService;
import org.delia.db.TableExistenceServiceImpl;
import org.delia.db.sql.table.FieldGenFactory;
import org.delia.db.sql.table.TableCreator;
import org.delia.log.Log;
import org.delia.parser.LineChecker;
import org.delia.runner.ResultValue;
import org.delia.runner.RunnerHelper;
import org.delia.zdb.ZDBInterfaceFactory;
import org.junit.Test;

public class TableCreatorTests {
	
	@Test
	public void test() {
		NewLegacyRunner runner = initRunner("type Actor struct {id int unique, firstName string, flag boolean} end");
		
//		TypeStatementExp exp0 = chkType("type Actor struct {id int unique, firstName string, flag boolean} end", "type Actor struct {id int unique, firstName string, flag boolean } end");
		ResultValue res = chkInsert(runner, "insert Actor {id:44, firstName:'bob', flag:true }");
		chkResOK(res);
		assertEquals(true, runner.getRegistry().existsType("Actor"));
		
		tblCreator = createTableCreator(runner);
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
	private ZDBInterfaceFactory dbInterface;
	private TableCreator tblCreator;
	private Log log = new UnitTestLog();
	private RunnerHelper helper = new RunnerHelper();

	private NewLegacyRunner initRunner(String src)  {
		NewLegacyRunner runner = new NewLegacyRunner(log);
		runner.begin(src);
		return runner;
	}
	private TableCreator createTableCreator(NewLegacyRunner runner)  {
		DBAccessContext dbctx = runner.createDBAccessContext();
		TableExistenceService existSvc = new TableExistenceServiceImpl(dbInterface, dbctx);
		FactoryService factorySvc = runner.getFactoryService();
		DatIdMap datIdMap = new DatIdMap();
		tblCreator = new TableCreator(factorySvc, runner.getRegistry(), new FieldGenFactory(factorySvc), new SimpleSqlNameFormatter(), existSvc, datIdMap);
		return tblCreator;
	}
	
	
	private void chkResOK(ResultValue res) {
		helper.chkResOK(res);
	}
	private ResultValue chkInsert(NewLegacyRunner runner, String input) {
		return runner.continueExecution(input);
	}
}
