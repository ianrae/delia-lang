package org.delia.runner;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.delia.base.DBHelper;
import org.delia.base.DBTestHelper;
import org.delia.core.FactoryService;
import org.delia.core.FactoryServiceImpl;
import org.delia.db.schema.SchemaMigrator;
import org.delia.db.schema.SchemaType;
import org.delia.error.ErrorTracker;
import org.delia.error.SimpleErrorTracker;
import org.delia.hld.HLDFactory;
import org.delia.hld.HLDFactoryImpl;
import org.delia.log.Log;
import org.delia.log.SimpleLog;
import org.delia.zdb.DBInterfaceFactory;
import org.junit.Test;

/**
 * TODO:
 * -(done) detects entire types I/U/D
 * -add support for field I/U/D
 * 
 * @author Ian Rae
 *
 */
public class SchemaMigratorTests {

	@Test
	public void testEmptyDB() {
		initRunner();
		boolean b = migrator.createSchemaTableIfNeeded();
		assertEquals(true, b);
		b = migrator.dbNeedsMigration();
		assertEquals(true, b);

		List<SchemaType> list = migrator.parseFingerprint(migrator.getDbFingerprint());
		assertEquals(0, list.size());

		List<SchemaType> list2 = migrator.parseFingerprint(migrator.getCurrentFingerprint());
		assertEquals(1, list2.size());
		assertEquals("Customer", list2.get(0).typeName);

		List<SchemaType> diffL = migrator.calcDiff(list, list2);
		assertEquals(1, diffL.size());
		assertEquals("Customer", diffL.get(0).typeName);

		b = migrator.performMigrations(diffL, true);
		assertEquals(true, b);

		String fingerprint = migrator.calcDBFingerprint();
		assertEquals("(v1)Customer:struct:{id:int:U/0,firstName:string:/0,lastName:string:O/0,points:int:O/0,flag:boolean:O/0}\n", fingerprint);
		
		//and migrate again (should be nothing to do)
		b = migrator.dbNeedsMigration();
		assertEquals(false, b);

		list = migrator.parseFingerprint(migrator.getDbFingerprint());
		assertEquals(1, list.size());

		list2 = migrator.parseFingerprint(migrator.getCurrentFingerprint());
		assertEquals(1, list2.size());
		assertEquals("Customer", list2.get(0).typeName);

		diffL = migrator.calcDiff(list, list2);
		assertEquals(0, diffL.size());
		migrator.close();
	}


	// --
	//private Runner runner;
	private DBInterfaceFactory dbInterface;
	private SchemaMigrator migrator;
	private RunnerHelper helper = new RunnerHelper();

	private Runner initRunner()  {
		Log log = new SimpleLog();
		ErrorTracker et = new SimpleErrorTracker(log);
		FactoryService factorySvc = new FactoryServiceImpl(log, et);
		dbInterface = DBTestHelper.createMEMDb(factorySvc);
		DBHelper.createTable(dbInterface, "Customer"); //!! fake schema

		LegacyRunner runner = helper.create(factorySvc, dbInterface);

		HLDFactory hldFactory = new HLDFactoryImpl();
		migrator = new SchemaMigrator(factorySvc, dbInterface, hldFactory, runner.getRegistry(), runner.innerRunner, null);
		return runner.innerRunner;
	}

}
