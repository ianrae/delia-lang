package org.delia.h2;

import static org.junit.Assert.assertEquals;

import org.delia.api.Delia;
import org.delia.api.DeliaFactory;
import org.delia.assoc.DatIdMap;
import org.delia.base.DBHelper;
import org.delia.base.DBTestHelper;
import org.delia.core.FactoryService;
import org.delia.core.FactoryServiceImpl;
import org.delia.db.DBType;
import org.delia.db.h2.test.H2TestCleaner;
import org.delia.db.schema.SchemaMigrator;
import org.delia.error.SimpleErrorTracker;
import org.delia.log.Log;
import org.delia.log.SimpleLog;
import org.delia.runner.DoNothingVarEvaluator;
import org.delia.runner.LegacyRunner;
import org.delia.runner.RunnerHelper;
import org.delia.zdb.DBInterfaceFactory;

public class DeliaInitializer {
	private DBInterfaceFactory dbInterface;
	private Log log;
	private SimpleErrorTracker et;
	private FactoryService factorySvc;
	private LegacyRunner runner;

	public LegacyRunner init(DBType dbType) {
		return init(dbType, new SimpleLog());
	}
	public LegacyRunner init(DBType dbType, Log log)  {
		this.log = log;
		this.et = new SimpleErrorTracker(log);
		this.factorySvc = new FactoryServiceImpl(log, et);
		boolean b = initDB(dbType);
		if (!b) {
			return null;
		}

		RunnerHelper helper = new RunnerHelper();
		this.runner = helper.create(factorySvc, dbInterface);
//		this.runner.legacyTypeMode = true;
//		b = runner.init(null);
//		assertEquals(true, b);
//		dbInterface.setRegistry(runner.getRegistry());
//		dbInterface.setVarEvaluator(runner);
		return runner;
	}
	
	private boolean initDB(DBType dbType) {
		switch(dbType) {
		case MEM:
		{
			dbInterface = DBTestHelper.createMEMDb(factorySvc);
			DBHelper.createTable(dbInterface, "Customer"); //!! fake schema
//			dbInterface.init(factorySvc);
		}
		break;
		case H2:
		{
			Delia deliaTmp = DeliaFactory.create(H2ConnectionHelper.getTestDB(), DBType.H2, log, factorySvc);
			dbInterface = deliaTmp.getDBInterface(); //new H2DBInterface(factorySvc, H2ConnectionHelper.getTestDB());
//			dbInterface.init(factorySvc);
			
			H2TestCleaner cleaner = new H2TestCleaner(DBType.H2);
			cleaner.deleteKnownTables(factorySvc, dbInterface);
		}
		break;
		}
		return true;
	}

	public DBInterfaceFactory getDbInterface() {
		return dbInterface;
	}
	
	public SchemaMigrator createSchemaMigrator() {
		SchemaMigrator migrator = new SchemaMigrator(factorySvc, dbInterface, runner.getRegistry(), new DoNothingVarEvaluator(), new DatIdMap());
		return migrator;
	}
}