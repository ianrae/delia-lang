package org.delia.h2;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.delia.compiler.ast.DeleteStatementExp;
import org.delia.compiler.ast.InsertStatementExp;
import org.delia.compiler.ast.LetStatementExp;
import org.delia.compiler.ast.TypeStatementExp;
import org.delia.db.schema.SchemaMigrator;
import org.delia.db.schema.SchemaType;
import org.delia.runner.CompilerHelper;
import org.delia.runner.QueryResponse;
import org.delia.runner.ResultValue;
import org.delia.runner.Runner;
import org.delia.runner.RunnerHelper;
import org.delia.type.DValue;


public class StartupTestBase {

	protected void basicTest(DeliaInitializer initter, Runner runner) throws Exception {
		assertEquals(false, runner.getRegistry().existsType("Actor"));
		
		//type Customer
		TypeStatementExp exp0 = chkType("type Actor struct {id int unique, firstName string, flag boolean} end", "type Actor struct {id int unique, firstName string, flag boolean } end");
		ResultValue res = runner.executeOneStatement(exp0);
		chkResOK(res);
		assertEquals(true, runner.getRegistry().existsType("Actor"));
		
		//migrate db
		migrateDB(initter, 0, 1, "Actor");

		//should be 0 records
		LetStatementExp exp2 = chkQueryLet("let a = Actor[44]", "let a = Actor[44]");
		res = runner.executeOneStatement(exp2);
		assertEquals(true, res.ok);
		helper.chkResQueryEmpty(res);
		
		//insert 1 record
		InsertStatementExp exp = chkInsert("insert Actor {id:44, firstName:'bob', flag:true }", "insert Actor {id: 44,firstName: 'bob',flag: true }");
		res = runner.executeOneStatement(exp);
		chkResOK(res);

		//query : should be 1
		exp2 = chkQueryLet("let a2 = Actor[44]", "let a2 = Actor[44]");
		res = runner.executeOneStatement(exp2);
		QueryResponse qresp = chkResQuery(res, "Actor");
		DValue dval = qresp.getOne();
		assertEquals("bob", dval.asStruct().getField("firstName").asString());
		
		//insert 1 record
		exp = chkInsert("insert Actor {id:45, firstName:'cam',flag:true }", "insert Actor {id: 45,firstName: 'cam',flag: true }");
		res = runner.executeOneStatement(exp);
		chkResOK(res);
		
		//query : should be 2
		exp2 = chkQueryLet("let a3 = Actor[45]", "let a3 = Actor[45]");
		res = runner.executeOneStatement(exp2);
		qresp = chkResQuery(res, "Actor");
		dval = qresp.getOne();
		assertEquals("cam", dval.asStruct().getField("firstName").asString());
		
		//query all should be 2
		exp2 = chkQueryLet("let a4 = Actor", "let a4 = Actor");
		res = runner.executeOneStatement(exp2);
		qresp = helper.chkResQueryMany(res, 2);
		dval = qresp.dvalList.get(1);
		assertEquals("cam", dval.asStruct().getField("firstName").asString());
		
		//delete 1 record
		DeleteStatementExp exp3 = chelper.chkDelete("delete Actor[44]", "delete Actor[44]");
		res = runner.executeOneStatement(exp3);
		chkResOK(res);
		
		//query : should be 1
		exp2 = chkQueryLet("let a5 = Actor[45]", "let a5 = Actor[45]");
		res = runner.executeOneStatement(exp2);
		qresp = chkResQuery(res, "Actor");
		dval = qresp.getOne();
		assertEquals("cam", dval.asStruct().getField("firstName").asString());
		
//		initter.getDbInterface().close();
		log("end.");
	}

	private void migrateDB(DeliaInitializer initter, int nDB, int nAdded, String typeName) {
		SchemaMigrator migrator = initter.createSchemaMigrator();
		boolean b = migrator.createSchemaTableIfNeeded();
		assertEquals(true, b);
		b = migrator.dbNeedsMigration();
		assertEquals(true, b);

		List<SchemaType> list = migrator.parseFingerprint(migrator.getDbFingerprint());
		assertEquals(nDB, list.size());

		List<SchemaType> list2 = migrator.parseFingerprint(migrator.getCurrentFingerprint());
		assertEquals(1, list2.size());
		assertEquals(typeName, list2.get(0).typeName);

		List<SchemaType> diffL = migrator.calcDiff(list, list2);
		assertEquals(nAdded, diffL.size());
		assertEquals(typeName, diffL.get(0).typeName);

		b = migrator.performMigrations(diffL, true);
		assertEquals(true, b);
	}

	// --
	private RunnerHelper helper = new RunnerHelper();
	protected CompilerHelper chelper = new CompilerHelper(null);

	private void log(String msg) {
		System.out.println(msg);
	}
	private void chkResOK(ResultValue res) {
		helper.chkResOK(res);
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
	private LetStatementExp chkQueryLet(String input, String output) {
		return chelper.chkQueryLet(input, output);
	}

}
