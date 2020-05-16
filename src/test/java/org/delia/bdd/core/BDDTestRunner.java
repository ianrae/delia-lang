package org.delia.bdd.core;

import java.util.List;
import java.util.StringJoiner;

import org.delia.base.UnitTestLog;
import org.delia.core.FactoryServiceImpl;
import org.delia.db.DBInterface;
import org.delia.log.Log;
import org.delia.zdb.ZDBInterfaceFactory;

public class BDDTestRunner {
	private Log log = new UnitTestLog();
	private int testIndexToRun = -1;
	public int numSkippedTests;
	private DBInterface retainedDBinterface;
	private ZDBInterfaceFactory retainedZDB;
	private DBInterfaceCreator creator;
	
	public BDDTestRunner(DBInterfaceCreator creator) {
		this.creator = creator;
	}

	public int runTests(List<BDDTest> tests, DBInterface dbInterface) {
		int numPass = 0;
		BDDFeature currentFeature = null;
		int index = 0;
		int numSkipped = 0;
		retainedDBinterface = dbInterface;
		for(BDDTest test: tests) {
			if (test.feature != currentFeature) {
				currentFeature = test.feature;
				log.log("FEATURE: %s", currentFeature.feature);
			}
			
			if (test.skip) {
				numSkipped++;
			} else if (testIndexToRun < 0) {
				numPass += runTest(test, index);
			} else if (testIndexToRun == index) {
				numPass += runTest(test, index);
			}
			index++;
		}

		numSkippedTests = numSkipped;
		int numFail = tests.size() - numPass - numSkipped;
		log.log("");
		String strFail = numFail == 0 ? "FAIL" : "**FAIL**";
		int total = numPass + numFail + numSkipped;
		if (testIndexToRun < 0) {
			log.log("PASS: %d: %s: %d SKIPPED: %d tests (%d)", numPass, strFail, numFail, numSkipped, total);
		}
		
		return numPass;
	}

	private int runTest(BDDTest test, int index) {
		log("");
		log("---------------------------------------");
		log(String.format("Test%d: %s...", index, test.title));
		String src = buildFrom(test.givenL);
		src += "\n";
		src += buildFrom(test.whenL);
		
		FactoryServiceImpl.nextZDBToUse = retainedZDB;
		BDDTesterEx tester = new BDDTesterEx(retainedDBinterface, creator, test, test.cleanTables);
		boolean pass = false;
		
		ThenValue thenVal = createThenValue(test); 
		thenVal.expectDVal = test.expectDVal;
		
		switch(test.expectedType) {
		case "string":
			pass = tester.chkString(src, thenVal);
			break;
		case "int":
			pass = tester.chkInt(src, thenVal);
			break;
		case "long":
			pass = tester.chkLong(src, thenVal);
			break;
		case "number":
			pass = tester.chkNumber(src, thenVal);
			break;
		case "boolean":
			pass = tester.chkBool(src, thenVal);
			break;
		case "date":
			pass = tester.chkDate(src, thenVal);
			break;
		case "struct":
			pass = tester.chkStruct(src, thenVal);
			break;
		default:
			log("UKNONWN THEN-TYPE: " + test.expectedType);
		}
		
		if (test.chainNextTest) {
			retainedDBinterface = tester.dbInterface;
			this.retainedZDB = FactoryServiceImpl.retainedZDBFactory;
		} else {
			retainedDBinterface = null;
			FactoryServiceImpl.retainedZDBFactory = null; //clear
		}
		
		if (!pass) {
			log(String.format("**Test%d: %s FAILED!**", index, test.title));
		}
		return pass ? 1 : 0;
	}
	private ThenValue createThenValue(BDDTest test) {
		if (test.thenL.size() == 1) {
			return new ThenValue(test.thenL.get(0));
		} else {
			return new ThenValue(test.thenL);
		}
	}

	private void log(String s) {
		System.out.println(s);
	}

	private String buildFrom(List<String> list) {
		StringJoiner joiner = new StringJoiner("\n");
		for(String s: list) {
			joiner.add(s);
		}
		return joiner.toString();
	}

	public void setTestToRun(int testIndexToRun) {
		this.testIndexToRun = testIndexToRun;
	}
}