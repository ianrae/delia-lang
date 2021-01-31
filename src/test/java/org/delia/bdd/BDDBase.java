package org.delia.bdd;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.delia.base.DBTestHelper;
import org.delia.base.UnitTestLog;
import org.delia.bdd.core.BDDParser;
import org.delia.bdd.core.BDDTest;
import org.delia.bdd.core.BDDTestRunner;
import org.delia.bdd.core.DBInterfaceCreator;
import org.delia.core.DateFormatService;
import org.delia.core.DateFormatServiceImpl;
import org.delia.core.FactoryService;
import org.delia.core.FactoryServiceImpl;
import org.delia.core.TimeZoneService;
import org.delia.core.TimeZoneServiceImpl;
import org.delia.error.SimpleErrorTracker;
import org.delia.log.Log;
import org.delia.util.TextFileReader;
import org.delia.zdb.DBInterfaceFactory;

public abstract class BDDBase implements DBInterfaceCreator {
	
	public static enum BDDGroup {
		R100_comments,
		R200_package,
		R300_scalar,
		R400_struct,
		R500_relation,
		R550_multi_relation,
		R560_self_relation,
		R600_rules,
		R650_rule_fns,
		R700_crud_insert,
		R800_crud_delete,
		R900_crud_update,
		R950_crud_assoc_crud,
		R1000_crud_upsert,
		R1100_userfn,
		R1200_let_scalar, 
		R1300_let_query,
		R1350_filter_expr,
		R1400_let_filterfn,
		R1500_let_queryfn,
		R1550_let_queryfn_relation,
		R1600_let_fetch,
		R1700_let_field_value,
		R1800_let_dollardollar,
		R1900_let_return,
		R2000_sprig,
		R2100_migration,
		R2150_migration_relations,
		R2200_security,
		R2300_multi_relation, 
		R2400_log,
		//R2500 input fn
		R2600_sizeof
	}
	public static class FileHelper {
		
		public String getDir(BDDGroup group) {
			String path = "src/main/resources/test/bdd/";
			String s = group.name().replace('_', '-');
			path += s;
			return path;
		}
	}
	
	//--
	protected FileHelper fileHelper = new FileHelper();
	protected Log log = new UnitTestLog();
	protected int testIndexToRun = -1;
	protected DBInterfaceFactory dbInterfaceToUse;
	protected String diagnosticFilter = "";
	
	protected List<String> filesExecutedL = new ArrayList<>();
	private BDDGroup currentGroup;
	protected boolean enableAllFileCheck = true;
	protected boolean disableAllSlowTests = DBTestHelper.disableAllSlowTests;
	
	//a test that is part of a group but will be tested separately
	protected void ignoreTest(String filename) {
		filesExecutedL.add(filename);
	}
	

	/**
	 * When we want to run all unit tests but not have to wait
	 * 15 minutes for H2 and Postgress BDD tests to run,
	 * set disableAllSlowTests to true. They will fail immediately.
	 */
	protected void disableAllSlowTestsIfNeeded() {
		if (disableAllSlowTests) {
			throw new IllegalArgumentException("disable SLOW tests");
		}
	}
	
	protected String testFile(BDDGroup group, String filename) {
		String s = fileHelper.getDir(group);
		s += '/' + filename;
		return s;
	}
	
	protected int runR300File(String filename, int numTests) {
		return runBDDFile(BDDGroup.R300_scalar, filename, numTests);
	}
	protected int runR400File(String filename, int numTests) {
		return runBDDFile(BDDGroup.R400_struct, filename, numTests);
	}
	protected int runR500File(String filename, int numTests) {
		return runBDDFile(BDDGroup.R500_relation, filename, numTests);
	}
	protected int runR550File(String filename, int numTests) {
		return runBDDFile(BDDGroup.R550_multi_relation, filename, numTests);
	}
	protected int runR560File(String filename, int numTests) {
		return runBDDFile(BDDGroup.R560_self_relation, filename, numTests);
	}
	protected int runR600File(String filename, int numTests) {
		return runBDDFile(BDDGroup.R600_rules, filename, numTests);
	}
	protected int runR650File(String filename, int numTests) {
		return runBDDFile(BDDGroup.R650_rule_fns, filename, numTests);
	}
	protected int runR700File(String filename, int numTests) {
		return runBDDFile(BDDGroup.R700_crud_insert, filename, numTests);
	}
	protected int runR800File(String filename, int numTests) {
		return runBDDFile(BDDGroup.R800_crud_delete, filename, numTests);
	}
	protected int runR900File(String filename, int numTests) {
		return runBDDFile(BDDGroup.R900_crud_update, filename, numTests);
	}
	protected int runR950File(String filename, int numTests) {
		return runBDDFile(BDDGroup.R950_crud_assoc_crud, filename, numTests);
	}
	protected int runR1000File(String filename, int numTests) {
		return runBDDFile(BDDGroup.R1000_crud_upsert, filename, numTests);
	}
	protected int runR1100File(String filename, int numTests) {
		return runBDDFile(BDDGroup.R1100_userfn, filename, numTests);
	}
	protected int runR1200File(String filename, int numTests) {
		return runBDDFile(BDDGroup.R1200_let_scalar, filename, numTests);
	}
	protected int runR1300File(String filename, int numTests) {
		return runBDDFile(BDDGroup.R1300_let_query, filename, numTests);
	}
	protected int runR1350File(String filename, int numTests) {
		return runBDDFile(BDDGroup.R1350_filter_expr, filename, numTests);
	}
	protected int runR1400File(String filename, int numTests) {
		return runBDDFile(BDDGroup.R1400_let_filterfn, filename, numTests);
	}
	protected int runR1500File(String filename, int numTests) {
		return runBDDFile(BDDGroup.R1500_let_queryfn, filename, numTests);
	}
	protected int runR1550File(String filename, int numTests) {
		return runBDDFile(BDDGroup.R1550_let_queryfn_relation, filename, numTests);
	}
	protected int runR1600File(String filename, int numTests) {
		return runBDDFile(BDDGroup.R1600_let_fetch, filename, numTests);
	}
	protected int runR1700File(String filename, int numTests) {
		return runBDDFile(BDDGroup.R1700_let_field_value, filename, numTests);
	}
	protected int runR1800File(String filename, int numTests) {
		return runBDDFile(BDDGroup.R1800_let_dollardollar, filename, numTests);
	}
	protected int runR1900File(String filename, int numTests) {
		return runBDDFile(BDDGroup.R1900_let_return, filename, numTests);
	}
	protected int runR2000File(String filename, int numTests) {
		return runBDDFile(BDDGroup.R2000_sprig, filename, numTests);
	}
	protected int runR2100File(String filename, int numTests) {
		return runBDDFile(BDDGroup.R2100_migration, filename, numTests);
	}
	protected int runR2150File(String filename, int numTests) {
		return runBDDFile(BDDGroup.R2150_migration_relations, filename, numTests);
	}
	protected int runR2200File(String filename, int numTests) {
		return runBDDFile(BDDGroup.R2200_security, filename, numTests);
	}
	protected int runR2300File(String filename, int numTests) {
		return runBDDFile(BDDGroup.R2300_multi_relation, filename, numTests);
	}
	protected int runR2400File(String filename, int numTests) {
		return runBDDFile(BDDGroup.R2400_log, filename, numTests);
	}
	protected int runR2600Sizeof(String filename, int numTests) {
		return runBDDFile(BDDGroup.R2600_sizeof, filename, numTests);
	}
	protected int runBDDFile(BDDGroup group, String filename, int numTests) {
		log.log("********* FILE: %s *********************", filename);
		currentGroup = group;
		filesExecutedL.add(filename);
		String path = testFile(group, filename);
		TextFileReader reader = new TextFileReader();
		List<String> lines = reader.readFile(path);
		BDDParser parser = new BDDParser();
		
		List<BDDTest> tests = parser.parse(lines);
		BDDTestRunner runner = new BDDTestRunner(this);
		runner.diagnosticFilter = diagnosticFilter;
		if (testIndexToRun >= 0) {
			runner.setTestToRun(testIndexToRun);
		}
		int passes = runner.runTests(tests, dbInterfaceToUse);
		
		if (testIndexToRun < 0) {
			int n = runner.numSkippedTests + numTests;
			if (n != tests.size()) {
				log.log("failed tests in: %s", filename);
				enableAllFileCheck = false;
			}
			assertEquals(n, tests.size());
		}
		if (numTests != passes) {
			enableAllFileCheck = false;
		}
		assertEquals(numTests, passes);
		return passes;
	}
	
	protected void chkAllFiles() {
		if (!enableAllFileCheck) {
			return;
		}
		FileHelper fileHelper = new FileHelper();
		//we assume each test method only does files in one group
		String dir = fileHelper.getDir(currentGroup);
		File file = new File(dir);       
		Collection<File> files = FileUtils.listFiles(file, null, false);     
		List<String> missedL = new ArrayList<>();
		for(File file2 : files){
			String filename = FilenameUtils.getName(file2.getAbsolutePath());
			//log.log("..seen: %s", filename);
			if (filesExecutedL.contains(filename)) {
				filesExecutedL.remove(filename);
			} else {
				missedL.add(filename);
			}
		}		
		
		for(String filename: missedL) {
			log.log("NOT-EXECUTED: %s", filename);
		}
		for(String filename: filesExecutedL) {
			log.log("DOUBLE-EXECUTED: %s", filename);
		}
		assertEquals(0, filesExecutedL.size());
		assertEquals(0, missedL.size());
	}

	public abstract DBInterfaceFactory createForTest();
	
	protected FactoryService createFactorySvc() {
		return new FactoryServiceImpl(log, new SimpleErrorTracker(log));
	}

	protected Date createDateFromStr(String dateStr) {
		TimeZoneService tzSvc = new TimeZoneServiceImpl();
		DateFormatService fmtSvc = new DateFormatServiceImpl(tzSvc);
		return fmtSvc.parseLegacy(dateStr);
	}
}
