//package org.delia.bddnew;
//
//import org.delia.bddnew.core.BDDTester2;
//import org.delia.bddnew.core.MyFakeSQLDBInterface;
//import org.delia.db.DBInterface;
//import org.delia.db.DBType;
//import org.delia.h2.H2ConnectionHelper;
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//
//public class BDDH2PostgresTests extends NewBDDBase {
//	
//	//TODO: R100
//	//TODO: R200
//	//R250 - handled in t0-date-timezone.txt
//	
//	@Test
//	public void testR300() {
//		runR300File("t0-int.txt", 10);
//		runR300File("t0-int-custom.txt", 10);
//		runR300File("t0-bool.txt", 5);
//		runR300File("t0-bool-custom.txt", 5);
//		runR300File("t0-long.txt", 10);
//		runR300File("t0-long-custom.txt", 10);
//		runR300File("t0-number.txt", 13);
//		runR300File("t0-number-custom.txt", 13);
//		runR300File("t0-string.txt", 11);
//		runR300File("t0-string-custom.txt", 5);
//		runR300File("t0-date.txt", 6);
//		runR300File("t0-date-custom.txt", 6);
//		runR300File("t0-date-timezone.txt",7);
//	}
//	@Test
//	public void testR400() {
//		runR400File("t0-struct-key.txt", 9);
//		runR400File("t0-field-unique.txt", 6);
//		runR400File("t0-struct.txt", 4);
//		runR400File("t0-struct-inheritance.txt", 6);
//		runR400File("t0-field-unique.txt", 6);
//		runR400File("t0-field-primarykey.txt", 6);
//		runR400File("t0-field-serial.txt", 8);
//	}
//	@Test
//	public void testR500() {
//		runR500File("t0-relation-one-to-one.txt", 9);
//		runR500File("t0-relation-one-to-one-oneway.txt", 8);
//		runR500File("t0-relation-one-to-many.txt", 9);
//		runR500File("t0-relation-many-to-many.txt", 11);
//		runR500File("t0-relation.txt", 2);
//	}
//	@Test
//	public void testR600() {
//		runR600File("t0-rule-crud.txt", 3);
//		runR600File("t0-rule-maxlen.txt", 6);
//		runR600File("t0-rule-maxlen-scalar.txt", 2);
//		runR600File("t0-rule-contains.txt", 6);
//		runR600File("t0-rule-contains-scalar.txt", 2);
//		runR600File("t0-rule-compare-inheritance.txt", 4);
//		
//		runR600File("t0-rule-compare-int.txt", 4);
//		runR600File("t0-rule-compare-int-scalar.txt", 3);
//		runR600File("t0-rule-compare-long.txt", 2);
//		runR600File("t0-rule-compare-number.txt", 2);
//		runR600File("t0-rule-compare-string.txt", 2);
//		runR600File("t0-rule-compare-boolean.txt", 2);
//		runR600File("t0-rule-compare-date.txt", 2);
//		runR600File("t0-rule-compare-relation.txt", 2);
//		runR600File("t0-rule-string-len.txt", 0);
//	}
//	
//	@Test
//	public void testR650() {
//		runR650File("t0-rulefn-all.txt", 0);
//	}
//	
//	@Test
//	public void testR700() {
//		runR700File("t0-insert.txt", 6);
//	}
//	
//	@Test
//	public void testR800() {
//		runR800File("t0-delete.txt", 4);
//	}
//	
//	@Test
//	public void testR900() {
//		runR900File("t0-update.txt", 7);
//	}
//	
//	@Test
//	public void testR1000() {
//		runR1000File("t0-upsert.txt", 0);
//	}
//	
//	@Test
//	public void testR1100() {
//		runR1100File("t0-userfn.txt", 1);
//	}
//	
//	@Test
//	public void testR1200() {
//		runR1200File("t0-let-scalar.txt", 6);
//	}
//	
//	@Test
//	public void testR1300() {
//		runR1300File("t0-let-query.txt", 6);
//	}
//	
//	@Test
//	public void testR1350() {
//		runR1350File("t0-filter-op-int.txt", 13);
//		runR1350File("t0-filter-op-long.txt", 13);
//		runR1350File("t0-filter-op-number.txt", 13);
//		runR1350File("t0-filter-op-boolean.txt", 6);
//		runR1350File("t0-filter-op-string.txt", 13);
//		runR1350File("t0-filter-op-date.txt", 13);
//		runR1350File("t0-filter-op-relation.txt", 13);
//		runR1350File("t0-filter-and-or.txt", 7);
//		runR1350File("t0-filter-in.txt", 3);
//		runR1350File("t0-filter-like.txt", 6);
//		runR1350File("t0-filter-ilike.txt", 0);
//		runR1350File("t0-filter-rlike.txt", 0);
//	}
//	
//	@Test
//	public void testR1400() {
//		runR1400File("t0-filterfn-date.txt", 12);
//	}
//	
//	@Test
//	public void testR1500() {
//		runR1500File("t0-queryfn-orderby.txt", 4);
//		runR1500File("t0-queryfn-distinct.txt", 0);
//		runR1500File("t0-queryfn-flatten.txt", 0);
//		runR1500File("t0-queryfn-count.txt", 2);
//		runR1500File("t0-queryfn-exist.txt", 2);
//		runR1500File("t0-queryfn-first.txt", 2);
//		runR1500File("t0-queryfn-last.txt", 2);
//		runR1500File("t0-queryfn-ith.txt", 4);
//		
//		runR1500File("t0-queryfn-min.txt", 0);
//		runR1500File("t0-queryfn-min-int.txt", 4);
//		runR1500File("t0-queryfn-min-long.txt", 4);
//		runR1500File("t0-queryfn-min-number.txt", 4);
//		runR1500File("t0-queryfn-min-bool.txt", 0);
//		runR1500File("t0-queryfn-min-relation.txt", 0);
//		runR1500File("t0-queryfn-min-string.txt", 4);
//		
//		runR1500File("t0-queryfn-max.txt", 0);
//		runR1500File("t0-queryfn-max-int.txt", 4);
//		runR1500File("t0-queryfn-max-long.txt", 4);
//		runR1500File("t0-queryfn-max-bool.txt", 0);
//		runR1500File("t0-queryfn-max-relation.txt", 0);
//		runR1500File("t0-queryfn-max-string.txt", 4);
//		runR1500File("t0-queryfn-avg.txt", 0);
//		runR1500File("t0-queryfn-limit.txt", 5);
//		runR1500File("t0-queryfn-offset.txt", 5);
//	}
//	
//	@Test
//	public void testR1600() {
//		runR1600File("t0-fetch.txt", 3);
//		runR1600File("t0-fetch-field.txt", 6);
//		runR1600File("t0-fetch-fk.txt", 0);
//		runR1600File("t0-fetch-fks.txt", 4);
//		runR1600File("t0-fetch-all.txt", 0);
//	}
//	
//	@Test
//	public void testR1700() {
//		runR1700File("t0-let-field-single.txt", 3);
//	}
//	
//	@Test
//	public void testR1800() {
//		runR1800File("t0-dollardollar.txt", 2);
//	}
//	
//	@Test
//	public void testR1900() {
//		runR1900File("t0-let-return.txt", 0);
//	}
//	
//	@Test
//	public void testR2000() {
//		runR2000File("t0-sprig.txt", 4);
//	}
//	
//	@Test
//	public void testR2100() {
//		runR2100File("t0-migration.txt", 2);
//		runR2100File("t0-migration2.txt", 2);
//		runR2100File("t0-migration3.txt", 2);
//		runR2100File("t0-migration3a.txt", 2);
//		runR2100File("t0-migration3b.txt", 2);
//		runR2100File("t0-migration4.txt", 2);
//	}
//	@Test
//	public void testR2200() {
//		runR2200File("t0-security-sql-injection.txt", 2);
//	}
//	
//	@Test
//	public void test8Debug() {
//		testIndexToRun = 0;
//		BDDTester2.disableSQLLoggingDuringSchemaMigration = false;
//		enableSQLLogging = true;
////		runR1500File("t0-queryfn-orderby.txt", 4);
//		runR900File("t0-update.txt", 7);
//	}
//	
//	//---
//	private DBType dbType = DBType.MEM;
//	private boolean cleanTables = true;
//	private boolean enableSQLLogging = true;
//	
//	@Before
//	public void init() {
//		H2ConnectionHelper.usePostgresVariant = true;
//	}
//	@After
//	public void cleanup() {
//		H2ConnectionHelper.usePostgresVariant = false;
//	}
//
//	@Override
//	protected int runBDDFile(BDDGroup group, String filename, int numTests) {
//		MyFakeSQLDBInterface db = new MyFakeSQLDBInterface(dbType);
//		db.cleanTables = cleanTables;
//		dbInterfaceToUse = db;
////		DeliaClient.forcedDBInterface = db;
//		if (enableSQLLogging) {
//			dbInterfaceToUse.enableSQLLogging(true);
//		}
//		return super.runBDDFile(group, filename, numTests);
//	}
//	@Override
//	public DBInterface createForTest() {
//		MyFakeSQLDBInterface db = new MyFakeSQLDBInterface(dbType);
//		db.cleanTables = cleanTables;
//		dbInterfaceToUse = db;
////		DeliaClient.forcedDBInterface = db;
//		if (enableSQLLogging) {
//			dbInterfaceToUse.enableSQLLogging(true);
//		}
//		return db;
//	}
//}
