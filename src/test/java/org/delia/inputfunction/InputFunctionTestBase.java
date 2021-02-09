package org.delia.inputfunction;

import static org.junit.Assert.assertEquals;

import org.delia.Delia;
import org.delia.DeliaSession;
import org.delia.app.NorthwindHelper;
import org.delia.base.DBTestHelper;
import org.delia.bdd.BDDBase;
import org.delia.dataimport.DataImportService;
import org.delia.dataimport.ImportLevel;
import org.delia.runner.inputfunction.InputFunctionResult;
import org.delia.runner.inputfunction.LineObjIterator;
import org.delia.zdb.DBInterfaceFactory;

public class InputFunctionTestBase  extends BDDBase {

	// --
	protected final String BASE_DIR = NorthwindHelper.BASE_DIR;
	
	//	private DeliaDao dao;
	protected Delia delia;
	protected DeliaSession session;
	protected int numExpectedColumnsProcessed;

	protected InputFunctionResult buildAndRun(LineObjIterator lineObjIter, int expectedNumRows) {
		DataImportService importSvc = new DataImportService(session, 0);

		InputFunctionResult result = importSvc.executeImport("foo", lineObjIter, ImportLevel.ONE);
		assertEquals(0, result.errors.size());
		assertEquals(expectedNumRows, result.numRowsProcessed);
		assertEquals(expectedNumRows, result.numRowsInserted);
		assertEquals(numExpectedColumnsProcessed, result.numColumnsProcessedPerRow);
		return result;
	}

	@Override
	public DBInterfaceFactory createForTest() {
		DBInterfaceFactory db = DBTestHelper.createMEMDb(createFactorySvc());
		return db;
	}

}
