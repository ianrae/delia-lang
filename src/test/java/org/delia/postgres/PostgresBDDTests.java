package org.delia.postgres;

import org.delia.base.UnitTestLog;
import org.delia.bddnew.BDDTestBase;
import org.delia.db.DBType;
import org.delia.log.LogLevel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PostgresBDDTests extends BDDTestBase {

	//TODO: R100
	//TODO: R200
	//R250 - handled in t0-date-timezone.txt

	@Test
	public void testR300() {
		runR300File("t0-int.txt", 10);
		runR300File("t0-int-custom.txt", 10);
		runR300File("t0-bool.txt", 5);
		runR300File("t0-bool-custom.txt", 5);
		runR300File("t0-long.txt", 10);
		runR300File("t0-long-custom.txt", 10);
		runR300File("t0-number.txt", 13);
		runR300File("t0-number-custom.txt", 13);
		runR300File("t0-string.txt", 12);
		runR300File("t0-string-custom.txt", 5);
		runR300File("t0-date.txt", 8);
		runR300File("t0-date-custom.txt", 6);
		runR300File("t0-date-timezone.txt",10);
	}
	@Test
	public void testR400() {
		runR400File("t0-struct-key.txt", 9);
		runR400File("t0-field-unique.txt", 6);
		runR400File("t0-struct.txt", 4);
		runR400File("t0-field-string.txt", 4);
		runR400File("t0-struct-inheritance.txt", 6);
		runR400File("t0-field-optional.txt", 4);
		runR400File("t0-field-primarykey.txt", 6);
		runR400File("t0-field-serial.txt", 7);
	}
	@Test
	public void testR500() {
		runR500File("t0-relation.txt", 2);
		runR500File("t0-relation-one-to-one-oneway.txt", 8);
		runR500File("t0-relation-one-to-many.txt", 9);
		runR500File("t0-relation-many-to-many.txt", 11);
		runR500File("t0-relation.txt", 2);
		runR500File("t0-relation-two.txt", 4);
		//TODO: add test w/o .fks()
	}
	@Test
	public void testR550() {
		runR550File("t0-multirel-1to1-1.txt", 1);
		runR550File("t0-multirel-Nto1-1.txt", 1);
		runR550File("t0-multirel-Nto1-2.txt", 3);
		runR550File("t0-multirel-NtoN-1.txt", 3);
	}
	@Test
	public void testR560() {
		runR560File("t0-self-11.txt", 3);
		runR560File("t0-self-11a.txt", 3);
		runR560File("t0-self-N1.txt", 2);
		runR560File("t0-self-NN.txt", 3); //TODO fix
	}
	@Test
	public void testR600() {
		runR600File("t0-rule-crud.txt", 3);
		runR600File("t0-rule-maxlen.txt", 6);
		runR600File("t0-rule-maxlen-scalar.txt", 2);
		runR600File("t0-rule-contains.txt", 6);
		runR600File("t0-rule-not-contains.txt", 2);
		runR600File("t0-rule-contains-scalar.txt", 2);
		runR600File("t0-rule-compare-inheritance.txt", 4);

		runR600File("t0-rule-compare-int.txt", 4);
		runR600File("t0-rule-compare-int-scalar.txt", 3);
		runR600File("t0-rule-compare-long.txt", 2);
		runR600File("t0-rule-compare-number.txt", 2);
		runR600File("t0-rule-compare-string.txt", 2);
		runR600File("t0-rule-compare-boolean.txt", 2);
		runR600File("t0-rule-compare-date.txt", 2);
		runR600File("t0-rule-compare-relation.txt", 2);
		runR600File("t0-rule-string-len.txt", 0);
	}

	@Test
	public void testR650() {
		runR650File("t0-rulefn-all.txt", 0);
	}

	@Test
	public void testR660() {
		runR660File("t0-rule-uniquefields.txt", 2);
		runR660File("t0-rule-uniquefields2.txt", 2);
//		runR660File("t0-rule-uniquefields-migrate1.txt", 2); TODO fix
//		runR660File("t0-rule-uniquefields-migrate2.txt", 2);
//		runR660File("t0-rule-uniquefields-migrate3.txt", 2);
	}
//	@Test
//	public void testR670() {
//		runR670File("t0-rule-index.txt", 2);
//		runR670File("t0-rule-index2.txt", 1);
//		runR670File("t0-rule-index-migrate1.txt", 2);
//		runR670File("t0-rule-index-migrate2.txt", 2);
//		runR670File("t0-rule-index-migrate3.txt", 2);
//	}
//
	@Test
	public void testR700() {
		runR700File("t0-insert.txt", 6);
		runR700File("t0-insert-date-var.txt", 3);
		runR700File("t0-insert-multi.txt", 2);
		runR700File("t0-insert-serial.txt", 1);
		runR700File("t0-insert-serial-inherit.txt", 1);
		runR700File("t0-insert-parent.txt", 0); //TODO fix
		runR700File("t0-insert-parent2.txt", 0); //TODO fix
		runR700File("t0-insert-int.txt", 2);
		runR700File("t0-insert-mm.txt", 3);
		runR700File("t0-insert-one-field.txt", 1);
	}

	@Test
	public void testR800() {
		runR800File("t0-delete.txt", 4);
		runR800File("t0-delete-mm.txt", 6);
	}

	@Test
	public void testR900() {
		runR900File("t0-update.txt", 7);
		runR900File("t0-update-mm-all.txt", 3); //TODO fix one
		runR900File("t0-update-mm-all-othertbl.txt", 1); //TODO fix one
		runR900File("t0-update-mm-id.txt", 4);
		runR900File("t0-update-mm-id-othertbl.txt", 2);
		runR900File("t0-update-mm-other.txt", 2); //TODO: fix two
		runR900File("t0-update-mm-other-othertbl.txt", 0); //TODO: fix two
		runR900File("t0-update-one-field.txt", 1);

		//TODO: !!!need tests for 1:1 and 1:N especially update Customer[55] { addr:[100,101]}
	}

	@Test
	public void testR950() {
		runR950File("t0-crud-assoc-insert.txt", 6);
	}

	@Test
	public void testR1000() {
		runR1000File("t0-upsert.txt", 4);
		runR1000File("t0-upsert-no-update.txt", 3);
		runR1000File("t0-upsert-mm-id.txt", 4);
		runR1000File("t0-upsert-mm-id-othertbl.txt", 2);
		runR1000File("t0-upsert-mm-all.txt", 1);
		runR1000File("t0-upsert-mm-other.txt", 0); //TODO fix later
		runR1000File("t0-upsert-unique.txt", 1); //TODO fix 3 later
		runR1000File("t0-upsert-mm-null.txt", 2);
		runR1000File("t0-upsert-mm-multiple.txt", 1);
		runR1000File("t0-upsert-one-field.txt", 1);
	}

//	@Test
//	public void testR1100() {
//		runR1100File("t0-userfn.txt", 1);
//	}

	@Test
	public void testR1200() {
		runR1200File("t0-let-scalar.txt", 6);
	}

	@Test
	public void testR1300() {
		runR1300File("t0-let-query.txt", 7);
		runR1300File("t0-let-varref.txt", 4); //TODO fix one
		runR1300File("t0-let-query-implicit-fetch.txt", 0);
	}

	@Test
	public void testR1350() {
		runR1350File("t0-filter-op-int.txt", 13);
		runR1350File("t0-filter-op-long.txt", 13);
		runR1350File("t0-filter-op-number.txt", 13);
		runR1350File("t0-filter-op-boolean.txt", 6);
		runR1350File("t0-filter-op-string.txt", 13);
		runR1350File("t0-filter-op-date.txt", 13);
		runR1350File("t0-filter-op-relation.txt", 13);
		runR1350File("t0-filter-and-or.txt", 7);
		runR1350File("t0-filter-and-or-multi.txt", 5);
		runR1350File("t0-filter-in.txt", 4);
		runR1350File("t0-filter-like.txt", 7);
		runR1350File("t0-filter-ilike.txt", 0);
		runR1350File("t0-filter-rlike.txt", 0);
		//TODO: fix runR1350File("t0-filter-in-twitter.txt", 2); //TODO fix two
	}

	@Test
	public void testR1400() {
		runR1400File("t0-filterfn-date.txt", 12);
	}

	@Test
	public void testR1500() {
		ignoreTest("t0-queryfn-orderby-2span.txt"); //TODO
		runR1500File("t0-queryfn-orderby.txt", 4);
		runR1500File("t0-queryfn-distinct.txt", 3);
		runR1500File("t0-queryfn-distinct-relation.txt", 6);
		runR1500File("t0-queryfn-distinct-relation-one-to-many.txt", 6);

		runR1500File("t0-queryfn-flatten.txt", 0);
		runR1500File("t0-queryfn-count.txt", 2);
		runR1500File("t0-queryfn-exist.txt", 2);
		runR1500File("t0-queryfn-first.txt", 2);
		runR1500File("t0-queryfn-last.txt", 2);
		runR1500File("t0-queryfn-ith.txt", 4);

		runR1500File("t0-queryfn-min.txt", 0);
		runR1500File("t0-queryfn-min-int.txt", 4);
		runR1500File("t0-queryfn-min-long.txt", 4);
		runR1500File("t0-queryfn-min-number.txt", 4);
		runR1500File("t0-queryfn-min-bool.txt", 0);
		runR1500File("t0-queryfn-min-relation.txt", 0);
		runR1500File("t0-queryfn-min-string.txt", 4);
		runR1500File("t0-queryfn-min-date.txt", 4);
		runR1500File("t0-queryfn-max-date.txt", 4);

		runR1500File("t0-queryfn-max.txt", 0);
		runR1500File("t0-queryfn-max-int.txt", 4);
		runR1500File("t0-queryfn-max-long.txt", 4);
		runR1500File("t0-queryfn-max-number.txt", 4);
		runR1500File("t0-queryfn-max-bool.txt", 0);
		runR1500File("t0-queryfn-max-relation.txt", 0);
		runR1500File("t0-queryfn-max-string.txt", 4);
		runR1500File("t0-queryfn-avg.txt", 0);
		runR1500File("t0-queryfn-limit.txt", 5);
		runR1500File("t0-queryfn-offset.txt", 5);
	}
//	@Test
//	public void testR1500a() {
//		enableAllFileCheck = false;
//		runR1500File("t0-queryfn-orderby-2span.txt", 0); //TODO fix two
//	}
//
//	@Test
//	public void testR1550() {
//		runR1550File("t0-queryfn-oneone-parent.txt", 6);
//		runR1550File("t0-queryfn-oneone-parent2.txt", 6);
//		runR1550File("t0-queryfn-oneone-child.txt", 6);
//		runR1550File("t0-queryfn-oneone-childa.txt", 6); //TODO fix one
//		runR1550File("t0-queryfn-onemany-parent.txt", 6);
//		runR1550File("t0-queryfn-onemany-child.txt", 6);
//		runR1550File("t0-queryfn-manymany-left.txt", 6);
//		runR1550File("t0-queryfn-manymany-right.txt", 6);
//	}

	@Test
	public void testR1600() {
		runR1600File("t0-fetch.txt", 5);
		runR1600File("t0-fetch-mm.txt", 4);
		runR1600File("t0-fetch-field.txt", 6);
		runR1600File("t0-fetch-fk.txt", 0);
		runR1600File("t0-fetch-fks.txt", 4);
		runR1600File("t0-fetch-all.txt", 0);
		runR1600File("t0-fetch-case-sensitive.txt", 1);
	}

	@Test
	public void testR1700() {
		runR1700File("t0-let-field-single.txt", 3);
		runR1700File("t0-let-field-func.txt", 3);
		runR1700File("t0-let-field-multiple.txt", 3);
		runR1700File("t0-let-field-relation.txt", 1);
	}

	@Test
	public void testR1800() {
		runR1800File("t0-dollardollar.txt", 2);
	}

//	@Test
//	public void testR1900() {
//		runR1900File("t0-let-return.txt", 0);
//	}

	@Test
	public void testR2000() {
		runR2000File("t0-sprig.txt", 3);
	}

//	@Test
//	public void testR2100() {
//		runR2100File("t0-migration.txt", 2);
//		runR2100File("t0-migration2.txt", 2);
//		runR2100File("t0-migration3.txt", 2);
//		runR2100File("t0-migration3a.txt", 2);
//		runR2100File("t0-migration3b.txt", 2);
//		runR2100File("t0-migration3c.txt", 2); //!!
//		runR2100File("t0-migration4.txt", 2);
//		runR2100File("t0-migration10.txt", 2);
//		runR2100File("t0-migration10a.txt", 2);
//		runR2100File("t0-migration10b.txt", 3);
//		runR2100File("t0-migration11.txt", 0);
//		runR2100File("t0-migration5.txt", 2);
//		runR2100File("t0-migration7.txt", 2);
//		runR2100File("t0-migration12.txt", 2);
//		runR2100File("t0-migration13.txt", 2);
//		runR2100File("t0-migration13a.txt", 2);
//		runR2100File("t0-migration14.txt", 2);
//		runR2100File("t0-migration14a.txt", 2);
//		runR2100File("t0-migration15.txt", 2);
//		runR2100File("t0-migration15a.txt", 2);
//		runR2100File("t0-migration16.txt", 2);
//		runR2100File("t0-migration16a.txt", 2);
//	}
//	@Test
//	public void testR2150() {
//		runR2150File("t0-migrate-one-to-one1.txt", 3);
//		runR2150File("t0-migrate-one-to-one1a.txt", 2);
//		runR2150File("t0-migrate-one-to-one2.txt", 2);
//		runR2150File("t0-migrate-one-to-one2a.txt", 2);
//		runR2150File("t0-migrate-one-to-one3.txt", 2);
//		runR2150File("t0-migrate-one-to-one4.txt", 2);
//		runR2150File("t0-migrate-one-to-one5.txt", 1);
//		runR2150File("t0-migrate-one-to-one6.txt", 2);
//
//		runR2150File("t0-migrate-one-to-many1.txt", 3);
//		runR2150File("t0-migrate-one-to-many2.txt", 2);
//		runR2150File("t0-migrate-one-to-many2a.txt", 2);
//		runR2150File("t0-migrate-one-to-many3.txt", 2);
//		runR2150File("t0-migrate-one-to-many4.txt", 2);
//		//is no test 5 for many-to-one
//		runR2150File("t0-migrate-one-to-many6.txt", 2);
//
//		runR2150File("t0-migrate-many-to-many1.txt", 3);
//		runR2150File("t0-migrate-many-to-many1a.txt", 2);
//		runR2150File("t0-migrate-many-to-many2.txt", 1); //TODO fix one later
//		runR2150File("t0-migrate-many-to-many2a.txt", 2);
//		runR2150File("t0-migrate-many-to-many3.txt", 2);
//		runR2150File("t0-migrate-many-to-many4.txt", 2);
//		runR2150File("t0-migrate-many-to-many6.txt", 2);
//		runR2150File("t0-migrate-many-to-many6a.txt", 2);
//		runR2150File("t0-migrate-many-to-many7.txt", 0);
//		runR2150File("t0-migrate-many-to-many8.txt", 3);
//		runR2150File("t0-migrate-many-to-many8a.txt", 3);
//	}
//
	@Test
	public void testR2200() {
		runR2200File("t0-security-sql-injection.txt", 3);
	}
//	@Test
//	public void testR2300() {
//		runR2300File("t0-multi-relation.txt", 0);
//	}
//
//	//R2400 tests not needed (only in mem tests)
//
	@Test
	public void testR2600() {
		runR2600File("t0-sizeof-int.txt", 2);
		runR2600File("t0-sizeof-int-invalid.txt", 1);
		runR2600File("t0-sizeof-string.txt", 2);
//		runR2600File("t0-sizeof-migration1.txt", 2);
//		runR2600File("t0-sizeof-migration1a.txt", 2);
//		runR2600File("t0-sizeof-migration1b.txt", 2);
//		runR2600File("t0-sizeof-migration1c.txt", 2);
//		runR2600File("t0-sizeof-migration2.txt", 2);
	}

	@Test
	public void testR2650() {
		runR2650File("t0-date-only.txt", 1);
		runR2650File("t0-time-only.txt", 1);
	}

	@Test
	public void testR2700() {
		runR2700Blob("t0-blob.txt", 3);
	}

	@Test
	public void testR2800() {
		runR2800File("t0-schema.txt", 4);
		runR2800File("t0-schema-explicit.txt", 4);
		runR2800File("t0-schema-public.txt", 4);
		runR2800File("t0-schema-off.txt", 1);
		runR2800File("t0-schema-same-name.txt", 1);
		runR2800File("t0-sqltable-name.txt", 1);
	}

	@Test
	public void testDebug() {
		singleTestToRunIndex = 0;
		enableAllFileCheck = false;
		enableSQLLogging = true;
		UnitTestLog.defaultLogLevel = LogLevel.DEBUG;
		log.setLevel(LogLevel.DEBUG);
//		runR700File("t0-insert.txt", 6);

//		runR2800File("t0-schema.txt", 4);
		runR1500File("t0-queryfn-max-string.txt", 4);
	}

	//---
	private DBType dbType = DBType.POSTGRES;
	private boolean cleanTables = true;
	private boolean enableSQLLogging = true;

	@Before
	public void init() {
		disableAllSlowTestsIfNeeded();
//		DbTableCleaner cleaner = new DbTableCleaner();
//		cleaner.cleanDB(dbType);
	}
	@After
	public void shutdown() {
//		chkAllFiles();
//		BDDTesterEx.disableSQLLoggingDuringSchemaMigration = true;
	}

	@Override
	protected DBType getDBType() {
		return dbType;
	}
}
