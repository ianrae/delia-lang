package org.delia.db.sql;

import static org.junit.Assert.assertEquals;

import org.delia.api.Delia;
import org.delia.api.DeliaSession;
import org.delia.base.UnitTestLog;
import org.delia.builder.ConnectionBuilder;
import org.delia.builder.ConnectionInfo;
import org.delia.builder.DeliaBuilder;
import org.delia.compiler.ast.InsertStatementExp;
import org.delia.compiler.ast.TypeStatementExp;
import org.delia.core.FactoryService;
import org.delia.db.DBAccessContext;
import org.delia.db.DBInterface;
import org.delia.db.DBType;
import org.delia.db.TableExistenceService;
import org.delia.db.TableExistenceServiceImpl;
import org.delia.db.sql.table.FieldGenFactory;
import org.delia.db.sql.table.TableCreator;
import org.delia.log.Log;
import org.delia.parser.LineChecker;
import org.delia.runner.CompilerHelper;
import org.delia.runner.DoNothingVarEvaluator;
import org.delia.runner.ResultValue;
import org.delia.runner.RunnerHelper;
import org.delia.type.DTypeRegistry;
import org.junit.Test;


public class TableCreatorTests {
	
	public static class NewLegacyRunner {
		private Delia delia;
		private DeliaSession session = null;

		public NewLegacyRunner(Delia delia) {
			this.delia = delia;
		}
		
		public DeliaSession begin(String src) {
			session = delia.beginSession(src);
			return session;
		}

		public ResultValue executeOneStatement(TypeStatementExp exp0) {
			String src = exp0.strValue(); //TODO this may not be correct
			return continueExecution(src);
		}
		public ResultValue continueExecution(String src){
			ResultValue res = delia.continueExecution(src, session);
			return res;
		}

		public DTypeRegistry getRegistry() {
			return session.getExecutionContext().registry;
		}

		public DBAccessContext createDBAccessContext() {
			DBAccessContext dbctx = new DBAccessContext(getRegistry(), new DoNothingVarEvaluator());
			return dbctx;
		}
	}
	
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
	private DBInterface dbInterface;
	private TableCreator tblCreator;
	private Log log = new UnitTestLog();
	private RunnerHelper helper = new RunnerHelper();
	protected CompilerHelper chelper = new CompilerHelper(null, log);
	private Delia delia;

	private NewLegacyRunner initRunner(String src)  {
//		ErrorTracker et = new SimpleErrorTracker(log);
//		dbInterface = new MemDBInterface();
//		dbInterface.createTable("Customer"); //!! fake schema
		
		ConnectionInfo info = ConnectionBuilder.dbType(DBType.MEM).build();
		Delia delia = DeliaBuilder.withConnection(info).log(log).build();
		this.delia = delia;
		dbInterface = delia.getDBInterface();
		
//		TableExistenceService existSvc = new TableExistenceServiceImpl(dbInterface, new DBAccessContext(runner));
//		tblCreator = new TableCreator(delia.getFactoryService(), runner.getRegistry(), new FieldGenFactory(factorySvc), new SimpleSqlNameFormatter(), existSvc);
		NewLegacyRunner runner = new NewLegacyRunner(delia);
		runner.begin(src);
		return runner;
	}
	private TableCreator createTableCreator(NewLegacyRunner runner)  {
		DBAccessContext dbctx = runner.createDBAccessContext();
		TableExistenceService existSvc = new TableExistenceServiceImpl(dbInterface, dbctx);
		FactoryService factorySvc = delia.getFactoryService();
		tblCreator = new TableCreator(factorySvc, runner.getRegistry(), new FieldGenFactory(factorySvc), new SimpleSqlNameFormatter(), existSvc);
		return tblCreator;
	}
	
	
	private void chkResOK(ResultValue res) {
		helper.chkResOK(res);
	}
	private TypeStatementExp chkType(String input, String output) {
		return chelper.chkType(input, output);
	}
	private ResultValue chkInsert(NewLegacyRunner runner, String input) {
		return runner.continueExecution(input);
	}
}
