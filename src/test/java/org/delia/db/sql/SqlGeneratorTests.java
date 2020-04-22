package org.delia.db.sql;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.delia.base.UnitTestLog;
import org.delia.compiler.ast.FilterExp;
import org.delia.compiler.ast.IdentExp;
import org.delia.compiler.ast.InsertStatementExp;
import org.delia.compiler.ast.IntegerExp;
import org.delia.compiler.ast.LetStatementExp;
import org.delia.compiler.ast.QueryExp;
import org.delia.compiler.ast.TypeStatementExp;
import org.delia.compiler.ast.UpdateStatementExp;
import org.delia.core.FactoryService;
import org.delia.core.FactoryServiceImpl;
import org.delia.db.DBAccessContext;
import org.delia.db.QueryBuilderService;
import org.delia.db.QueryContext;
import org.delia.db.QuerySpec;
import org.delia.db.TableExistenceService;
import org.delia.db.TableExistenceServiceImpl;
import org.delia.db.h2.SqlHelperFactory;
import org.delia.db.memdb.MemDBInterface;
import org.delia.db.sql.prepared.InsertStatementGenerator;
import org.delia.db.sql.prepared.PreparedStatementGenerator;
import org.delia.db.sql.prepared.SqlStatement;
import org.delia.db.sql.table.FieldGenFactory;
import org.delia.db.sql.table.TableCreator;
import org.delia.db.sql.table.TableInfo;
import org.delia.error.ErrorTracker;
import org.delia.error.SimpleErrorTracker;
import org.delia.log.Log;
import org.delia.parser.LineChecker;
import org.delia.runner.CompilerHelper;
import org.delia.runner.DoNothingVarEvaluator;
import org.delia.runner.FilterEvaluator;
import org.delia.runner.QueryResponse;
import org.delia.runner.ResultValue;
import org.delia.runner.Runner;
import org.delia.runner.RunnerHelper;
import org.delia.runner.RunnerImpl;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.valuebuilder.ScalarValueBuilder;
import org.junit.Test;




public class SqlGeneratorTests {
	
	@Test
	public void testCreateTable() {
		TypeStatementExp exp0 = chkType("type Actor struct {id int unique, firstName string, flag boolean} end", "type Actor struct {id int unique, firstName string, flag boolean } end");
		InsertStatementExp exp = chkInsert("insert Actor {id:44, firstName:'bob', flag:true }", "insert Actor {id: 44,firstName: 'bob',flag: true }");
		
		Runner runner = initRunner();
		ResultValue res = runner.executeOneStatement(exp0);
		chkResOK(res);
		assertEquals(true, runner.getRegistry().existsType("Actor"));
		
//		String sql = sqlgen.generateCreateTable("Actor", null);
		String sql = tblCreator.generateCreateTable("Actor", null);
		log.log("sql: " + sql);
		
		LineChecker checker = new LineChecker(sql);
		checker.chkLine("CREATE TABLE Actor (");
		checker.chkLine("id Int UNIQUE,");
		checker.chkLine("firstName VARCHAR(4096),");
		checker.chkLine("flag BOOLEAN");
		checker.chkLine(");");		
	}
	@Test
	public void testQuery() {
		Runner runner = initRunner();
		assertEquals(true, runner.getRegistry().existsType("Customer"));
		
		QuerySpec spec = new QuerySpec();
		spec.queryExp = new QueryExp(99, new IdentExp("Customer"), null, null);
//		String sql = sqlgen.generateQuery(spec);
		SqlStatement statement = this.prepGen.generateQuery(spec);
		log.log("sql: " + statement.sql);
		
		LineChecker checker = new LineChecker(statement.sql);
		checker.chkLine("SELECT * FROM Customer;");
	}
	@Test
	public void testInsert() {
		TypeStatementExp exp0 = chkType("type Actor struct {id int unique, firstName string, flag boolean} end", "type Actor struct {id int unique, firstName string, flag boolean } end");
		InsertStatementExp exp = chkInsert("insert Actor {id:44, firstName:'bob', flag:true }", "insert Actor {id: 44,firstName: 'bob',flag: true }");
		
		Runner runner = initRunner();
		ResultValue res = runner.executeOneStatement(exp0);
		chkResOK(res);
		assertEquals(true, runner.getRegistry().existsType("Actor"));
		
		DBAccessContext ctx = new DBAccessContext(runner);
		dbInterface.createTable("Actor", ctx); //!! fake schema
		res = runner.executeOneStatement(exp);
		chkResOK(res);
		
		LetStatementExp exp2 = chkQueryLet("let a = Actor[44]", "let a = Actor[44]");
		res = runner.executeOneStatement(exp2);
		QueryResponse qresp = chkResQuery(res, "Actor");
		DValue dval = qresp.getOne();
		
		List<TableInfo> tblinfoL = new ArrayList<>();
		SqlStatement statement = insertGen.generateInsert(dval, tblinfoL);
		log.log("sql: " + statement.sql);
		
		LineChecker checker = new LineChecker(statement.sql);
		checker.chkLine("INSERT INTO Actor (id, firstName, flag)");
//		checker.chkLine("VALUES (44,'bob',true);");
		checker.chkLine("VALUES (?,?,?);");
		assertEquals(3, statement.paramL.size());
		chkParamInt(statement, 0, 44);
		chkParamString(statement, 1, "bob");
		chkParamBool(statement, 2, true);
	}
	@Test
	public void testDelete() {
		Runner runner = initRunner();
		assertEquals(true, runner.getRegistry().existsType("Customer"));
		
		IntegerExp exp = new IntegerExp(1);
		FilterExp filter = new FilterExp(99, exp);
		QuerySpec spec = new QuerySpec();
		spec.queryExp = new QueryExp(99, new IdentExp("Customer"), filter, null);
//		String sql = sqlgen.generateDelete(spec);
		SqlStatement statement = this.prepGen.generateDelete(spec);
		log.log("sql: " + statement.sql);
		
		LineChecker checker = new LineChecker(statement.sql);
		checker.chkLine("DELETE FROM Customer  WHERE  id=?;");
	}
	
	@Test
	public void testUpdate() {
		TypeStatementExp exp0 = chkType("type Actor struct {id int unique, firstName string, flag boolean} end", "type Actor struct {id int unique, firstName string, flag boolean } end");
		InsertStatementExp insexp = chkInsert("insert Actor {id:44, firstName:'bob', flag:true }", "insert Actor {id: 44,firstName: 'bob',flag: true }");
		UpdateStatementExp exp = this.chkUpdate("update Actor[44] {firstName:'ubob', flag:false}", "update Actor[44] {firstName: 'ubob',flag: false }");
				
		Runner runner = initRunner();
		ResultValue res = runner.executeOneStatement(exp0);
		chkResOK(res);
		assertEquals(true, runner.getRegistry().existsType("Actor"));
		
		DBAccessContext ctx = new DBAccessContext(runner);
		dbInterface.createTable("Actor", ctx); //!! fake schema
		res = runner.executeOneStatement(insexp);
		chkResOK(res);
		res = runner.executeOneStatement(exp);
		chkResOK(res);
		
		LetStatementExp exp2 = chkQueryLet("let a = Actor[44]", "let a = Actor[44]");
		res = runner.executeOneStatement(exp2);
		QueryResponse qresp = chkResQuery(res, "Actor");
		DValue dval = qresp.getOne();
		
		//we want a partial set of fields
		dval.asMap().remove("id");
		
		QuerySpec spec = new QuerySpec();
		QueryBuilderService builderSvc = factorySvc.getQueryBuilderService();
		ScalarValueBuilder scalarSvc = factorySvc.createScalarValueBuilder(runner.getRegistry());
		spec.queryExp = builderSvc.createPrimaryKeyQuery("Customer", scalarSvc.buildInt(44));
		
		List<TableInfo> tblinfoL = new ArrayList<>();
//		String sql = sqlgen.generateUpdate(dval, tblinfoL, spec);
		SqlStatement statement = this.prepGen.generateUpdate(dval, tblinfoL, spec);
		log.log("sql: " + statement.sql);
		
		LineChecker checker = new LineChecker(statement.sql);
//		checker.chkLine("UPDATE Actor SET firstName='ubob', flag=false");
//		checker.chkLine(" WHERE id=44;");
		checker.chkLine("UPDATE Actor SET firstName=?, flag=? WHERE  id=?;");
		assertEquals(3, statement.paramL.size());
		chkParamString(statement, 0, "ubob");
		chkParamBool(statement, 1, false);
		chkParamInt(statement, 2, 44);
	}
	
	@Test
	public void testCount() {
		TypeStatementExp exp0 = chkType("type Actor struct {id int unique, firstName string, flag boolean} end", "type Actor struct {id int unique, firstName string, flag boolean } end");
		InsertStatementExp insexp = chkInsert("insert Actor {id:44, firstName:'bob', flag:true }", "insert Actor {id: 44,firstName: 'bob',flag: true }");
				
		Runner runner = initRunner();
		ResultValue res = runner.executeOneStatement(exp0);
		chkResOK(res);
		assertEquals(true, runner.getRegistry().existsType("Actor"));
		
		DBAccessContext ctx = new DBAccessContext(runner);
		dbInterface.createTable("Actor", ctx); //!! fake schema
		res = runner.executeOneStatement(insexp);
		chkResOK(res);
		
		LetStatementExp exp2 = chkQueryLet("let a = Actor[true].count()", "let a = Actor[true].count()");
		res = runner.executeOneStatement(exp2);
		chkResOK(res);
		QueryResponse qresp = (QueryResponse) res.val;
		DValue dval = qresp.getOne();
		assertEquals(1, dval.asLong());
		
		QueryBuilderService builderSvc = factorySvc.getQueryBuilderService();
		QueryExp queryExp = builderSvc.createCountQuery("Actor");
		
		QuerySpec spec = builderSvc.buildSpec(queryExp, runner);
		QueryContext qtx = new QueryContext();
		DBAccessContext dbctx = new DBAccessContext(runner);
		qresp = dbInterface.executeQuery(spec, qtx, dbctx);
		dval = qresp.getOne();
		assertEquals("Actor", dval.getType().getName());
		//Note. we're using Mem db so it ignores .count() and returns Actor
		//actual H2,Postgres would return long (use select count(*)...)
		//so we need to manually invoke .count()
		RunnerImpl runnerImpl = (RunnerImpl) runner;
		res = new ResultValue();
		runnerImpl.runQueryFnsIfNeeded(queryExp, qresp, res);
		dval = qresp.getOne();
		assertEquals(1, dval.asLong()); //now its an int
		
	}
	private void chkParamInt(SqlStatement statement, int i, int expected) {
		assertEquals(expected, statement.paramL.get(i).asInt());
	}
	private void chkParamBool(SqlStatement statement, int i, boolean expected) {
		assertEquals(expected, statement.paramL.get(i).asBoolean());
	}
	private void chkParamString(SqlStatement statement, int i, String expected) {
		assertEquals(expected, statement.paramL.get(i).asString());
	}
	
	// --
	private MemDBInterface dbInterface;
	private Log log = new UnitTestLog();
	private RunnerHelper helper = new RunnerHelper();
	protected CompilerHelper chelper = new CompilerHelper(null, log);
	protected FactoryService factorySvc;
	protected Runner runner;
	private TableCreator tblCreator;
	private PreparedStatementGenerator prepGen;
	private InsertStatementGenerator insertGen;

	private Runner initRunner()  {
		ErrorTracker et = new SimpleErrorTracker(log);
		dbInterface = new MemDBInterface();
//		dbInterface.createTable("Customer"); //!! fake schema
		
		factorySvc = new FactoryServiceImpl(log, et);
		runner = helper.create(factorySvc, dbInterface);

		SqlNameFormatter nameFormatter = new SimpleSqlNameFormatter();
		tblCreator = new TableCreator(factorySvc, runner.getRegistry(), new FieldGenFactory(factorySvc), new SimpleSqlNameFormatter());
		SqlHelperFactory sqlHelperFactory = new SqlHelperFactory(factorySvc);
		TableExistenceService existSvc = new TableExistenceServiceImpl(dbInterface, new DBAccessContext(runner));
		this.prepGen = 	new PreparedStatementGenerator(factorySvc, runner.getRegistry(), sqlHelperFactory, new DoNothingVarEvaluator(), existSvc);
		this.insertGen = new InsertStatementGenerator(factorySvc, runner.getRegistry(), nameFormatter, existSvc);

		return runner;
	}
	
	private QueryResponse chkResQuery(ResultValue res, String typeName) {
		return helper.chkResQuery(res, typeName);
	}

	private TypeStatementExp chkType(String input, String output) {
		return chelper.chkType(input, output);
	}
	private InsertStatementExp chkInsert(String input, String output) {
		return chelper.chkInsert(input, output);
	}
	private UpdateStatementExp chkUpdate(String input, String output) {
		return chelper.chkUpdate(input, output);
	}
	private LetStatementExp chkQueryLet(String input, String output) {
		return chelper.chkQueryLet(input, output);
	}
	private void chkResOK(ResultValue res) {
		this.helper.chkResOK(res);
	}

}
