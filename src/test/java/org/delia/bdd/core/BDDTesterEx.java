package org.delia.bdd.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.delia.DeliaSession;
import org.delia.base.UnitTestLog;
import org.delia.bdd.core.checker.BDDResult;
import org.delia.bdd.core.checker.BoolChecker;
import org.delia.bdd.core.checker.DateChecker;
import org.delia.bdd.core.checker.IntChecker;
import org.delia.bdd.core.checker.LongChecker;
import org.delia.bdd.core.checker.StringChecker;
import org.delia.bdd.core.checker.StructChecker;
import org.delia.bdd.core.checker.ValueChecker;
import org.delia.bdd.core.checker.ValueCheckerBase;
import org.delia.client.ClientTests.DeliaClient;
import org.delia.error.DeliaError;
import org.delia.log.Log;
import org.delia.runner.DeliaException;
import org.delia.runner.ResultValue;
import org.delia.type.BuiltInTypes;
import org.delia.type.DType;
import org.delia.type.DValue;
import org.delia.type.Shape;
import org.delia.valuebuilder.IntegerValueBuilder;
import org.delia.zdb.DBInterfaceFactory;
import org.delia.zdb.mem.MemDBInterfaceFactory;

public class BDDTesterEx {
	private static class NumberChecker extends ValueCheckerBase {
		@Override
		public void chkShape(BDDResult bddres) {
			assertEquals(Shape.NUMBER, bddres.res.shape);
		}

		@Override
		public boolean compareObj(ThenValue thenVal, DValue dval, Log log) {
			Double expected = Double.parseDouble(thenVal.expected);
			Double s = dval.asNumber();
			if (expected == null && s == null) {
				return true;
			} else if (!expected.equals(s)) {
				String err = String.format("value-mismatch: expected '%g' but got '%g'", expected, s);
				log.logError(err);
				return false;
			} else {
				return true;
			}
		}
	}

	private Log log = new UnitTestLog();

	private DeliaClient client;
	DBInterfaceFactory dbInterface;
	private String diagnosticFilter;
	private BDDTest currentTest;

	BDDTesterEx(DBInterfaceFactory retainedDBInterface, DBInterfaceCreator creator, BDDTest test, String cleanTables, String diagnosticFilter) {
		this.currentTest = test;
		this.diagnosticFilter = diagnosticFilter;
		if (retainedDBInterface == null) {
			dbInterface = creator.createForTest(); 
		} else {
			dbInterface = retainedDBInterface;
			if (dbInterface instanceof MyFakeSQLDBInterface) {
				MyFakeSQLDBInterface fakedb = (MyFakeSQLDBInterface) dbInterface;
				if (fakedb.execCount > 0) { //clean db first time
					log.log("use previous DBInterface!");
					fakedb.cleanTables = false;
				}
				fakedb.execCount++;
			}
		}
		client = new DeliaClient(dbInterface);
		
		if (dbInterface instanceof MemDBInterfaceFactory) {
			MemDBInterfaceFactory memdb = (MemDBInterfaceFactory) dbInterface;
			//memdb.createTablesAsNeededFlag = true;
		}
		
		if (cleanTables != null) {
			if (dbInterface instanceof MyFakeSQLDBInterface) {
				MyFakeSQLDBInterface mf = (MyFakeSQLDBInterface) dbInterface;
				mf.tablesToClean = cleanTables;
			}
		}
		
		if (dbInterface instanceof MyFakeSQLDBInterface) {
			MyFakeSQLDBInterface mf = (MyFakeSQLDBInterface) dbInterface;
			mf.init(client.getFactorySvc());
		}
	}

	public boolean chkString(String delia, ThenValue thenVal) {
		return chkValue(delia, thenVal, new StringChecker());
	}

	public boolean chkDate(String delia, ThenValue thenVal) {
		return chkValue(delia, thenVal, new DateChecker());
	}

	public boolean chkInt(String delia, ThenValue thenVal) {
		return chkValue(delia, thenVal, new IntChecker());
	}

	public boolean chkBool(String delia, ThenValue thenVal) {
		return chkValue(delia, thenVal, new BoolChecker());
	}

	public boolean chkLong(String delia, ThenValue thenVal) {
		return chkValue(delia, thenVal, new LongChecker());
	}

	public boolean chkNumber(String delia, ThenValue thenVal) {
		return chkValue(delia, thenVal, new NumberChecker());
	}

	public boolean chkStruct(String delia, ThenValue thenVal) {
		return chkValue(delia, thenVal, new StructChecker());
	}

	public boolean chkValue(String delia, ThenValue thenVal, ValueChecker checker) {
		mostRecentSess = null;
		BDDResult bddres = runIt(delia, thenVal);
		if (bddres.res != null) {
			if (bddres.res.getDValueCount() > 1) {
				List<DValue> list = bddres.res.getAsDValueList();
				if (list == null && thenVal.isNull()) {
					return true;
				} else {
					assertNotNull(mostRecentSess);
					checker.setDBSession(mostRecentSess);
					StructChecker structChecker = (StructChecker) checker;
					return structChecker.compareMultiObj(thenVal, list, log);
				}
			} else {
				checker.chkShape(bddres);
				DValue dval = getValAsDVal(thenVal, bddres);
				if (dval == null && thenVal.isNull()) {
					return true;
				} else {
					assertNotNull(mostRecentSess);
					checker.setDBSession(mostRecentSess);
					return checker.compareObj(thenVal, dval, log);
				}
			}
		} else {
			return bddres.ok;
		}
	}

	private DValue getValAsDVal(ThenValue thenVal, BDDResult bddres) {
		if (thenVal.expectDVal) {
			return 	bddres.res.getAsDValue();
		} else {
			//TODO: fix. assume only int for now
			//hack hack hack
			String name = BuiltInTypes.INTEGER_SHAPE.name();
			DType type = new DType(Shape.INTEGER, name, null);
			
			IntegerValueBuilder builder = new IntegerValueBuilder(type);
			Integer n = (Integer) bddres.res.val;
			builder.buildFromString(n.toString());
			builder.finish();
			return builder.getDValue();
		}
	}

	public BDDResult runIt(String deliaSrc, ThenValue thenVal) {
		BDDResult bddres = new BDDResult();
		try {
			bddres.res = runDelia(deliaSrc);
			bddres.ok = true;
		} catch (DeliaException e) {
			String expectedErr = findExpectedError(thenVal);
			if (e.getErrorCount() == 0) {
				log.logError("EXCEPTION(%s): %s", "NoDeliaError", e.getMessage());
				throw new RuntimeException("Exception occured, and can't find ERROR: value!");
			}
			
			String id = e.getFirstError() == null ? "?" : e.getFirstError().getId();
			if (expectedErr == null) {
				e.printStackTrace();
				log.logError("EXCEPTION(%s): %s", id, e.getMessage());
				throw new RuntimeException("Exception occured, and can't find ERROR: value!");
			}

			if (expectedErr.startsWith("ERROR:")) {
				String tmp = StringUtils.substringAfter(expectedErr, ":");
				tmp = StringUtils.substringBefore(tmp, ":").trim();
				DeliaError err = e.getLastError();
				if (err.getId().equals(tmp)) {
					bddres.ok = true;
					return bddres;
				} else {
					log.logError("failed to find ERROR: %s, actual: %s", tmp, id);
				}
			}
			log.logError(e.getMessage());
		}
		return bddres;
	}

	private String findExpectedError(ThenValue thenVal) {
		if (thenVal.expected != null) {
			return thenVal.expected;
		} else {
			BDDHelper helper = new BDDHelper(dbInterface.getDBType());
			List<String> list = helper.adjustForDBType(thenVal.expectedL);
			
			for(String s: list) {
				if (s.startsWith("ERROR: ")) {
					return s;
				}
			}
		}
		return null;
	}

//	private int nextVarNum = 1;
	private DeliaSession mostRecentSess;
	public static boolean disableSQLLoggingDuringSchemaMigration = true;

	private ResultValue runDelia(String src) {
		client.getOptions().disableSQLLoggingDuringSchemaMigration = disableSQLLoggingDuringSchemaMigration;
		if (!currentTest.useSafeMigrationPolicy) {
			client.getOptions().useSafeMigrationPolicy = false;
		}
		client.getFactorySvc().getDiagnosticService().configure(diagnosticFilter);
		
		ResultValue res = client.beginExecution(src);
		assertEquals(true, res.ok);
		this.mostRecentSess = client.getSession();
		return res;
	}
}