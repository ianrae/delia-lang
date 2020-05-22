package org.delia.bdd.old;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.delia.base.UnitTestLog;
import org.delia.compiler.ast.InsertStatementExp;
import org.delia.compiler.ast.TypeStatementExp;
import org.delia.db.DBType;
import org.delia.db.schema.SchemaMigrator;
import org.delia.db.schema.SchemaType;
import org.delia.h2.DeliaInitializer;
import org.delia.log.Log;
import org.delia.runner.CompilerHelper;
import org.delia.runner.ResultValue;
import org.delia.runner.Runner;
import org.delia.runner.RunnerHelper;



public class BDDTestBase {

	protected void setupTypes() {
		assertEquals(false, runner.getRegistry().existsType("Actor"));
		
		//type Actor
		TypeStatementExp exp0 = chelper.chkType("type Actor struct {id int unique, firstName string, flag boolean} end", "type Actor struct {id int unique, firstName string, flag boolean } end");
		ResultValue res = runner.executeOneStatement(exp0);
		helper.chkResOK(res);
		assertEquals(true, runner.getRegistry().existsType("Actor"));
		
		//type Zoo
		exp0 = chelper.chkType("type Zoo struct {id int unique, firstName string, flag boolean} end", "type Zoo struct {id int unique, firstName string, flag boolean } end");
		res = runner.executeOneStatement(exp0);
		helper.chkResOK(res);
		assertEquals(true, runner.getRegistry().existsType("Zoo"));
		
		//migrate db
		migrateDB(initter, "Actor", "Zoo");
	}
	
	protected void addData() {
		for(int i = 0; i < 4; i++) {
			String name = String.format("name%d", i);
			String dang = String.format("insert Actor {id:%d, firstName:'%s',flag:true }", 10+i, name);
			InsertStatementExp exp = chelper.chkInsert(dang, null);
			ResultValue res = runner.executeOneStatement(exp);
			helper.chkResOK(res);
		}
	}
	
	//--
	private Log log = new UnitTestLog();
	protected RunnerHelper helper = new RunnerHelper();
	protected CompilerHelper chelper = new CompilerHelper(null);
	protected DeliaInitializer initter;
	protected Runner runner;
	protected BDDQueryHelper qhelper;

	public static final String TYPE0 = "Zoo"; //type with 0 values
	public static final String TYPE1 = "Actor"; //type with 4 values
	
	public void init() {
		initter = new DeliaInitializer();
		runner = initter.init(DBType.MEM, log);
		qhelper = new BDDQueryHelper(initter, runner);
		
		setupTypes();
		addData();
	}

	protected void log(String msg) {
		log.log(msg);
	}
	
	protected void migrateDB(DeliaInitializer initter, String typeName, String typeName2) {
		SchemaMigrator migrator = initter.createSchemaMigrator();
		boolean b = migrator.createSchemaTableIfNeeded();
		assertEquals(true, b);
		b = migrator.dbNeedsMigration();
		assertEquals(true, b);

		List<SchemaType> list = migrator.parseFingerprint(migrator.getDbFingerprint());
		assertEquals(0, list.size());

		List<SchemaType> list2 = migrator.parseFingerprint(migrator.getCurrentFingerprint());
		assertEquals(2, list2.size());
		assertEquals(typeName, list2.get(0).typeName);

		List<SchemaType> diffL = migrator.calcDiff(list, list2);
		assertEquals(2, diffL.size());
		assertEquals(typeName, diffL.get(0).typeName);
		log("migrate: +" + typeName + " +" + typeName2);

		b = migrator.performMigrations(diffL, true);
		assertEquals(true, b);
		migrator.close();
	}
}
