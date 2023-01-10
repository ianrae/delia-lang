//package org.delia.bddnew;
//
//import org.delia.db.DBType;
//import org.delia.log.LogLevel;
//import org.delia.seede.base.UnitTestLog;
//import org.junit.Before;
//import org.junit.Test;
//
//public class PostgresBDDTests extends BDDTestBase {
//
//
//    @Test
//    public void testSamples() {
//    }
//
//    @Test
//    public void testR200() {
//        runR200( "t0-exist.txt", 1);
//        runR200( "t0-exist-one-to-one.txt", 1);
//        runR200( "t0-exist-one-to-one-fail-unique.txt", 1);
//        runR200( "t0-exist-one-to-one-optional.txt", 1);
//        runR200( "t0-exist-unknown-field.txt", 1);
//        runR200( "t0-exist-unknown-table.txt", 1);
//        runR200( "t0-exist-case-sensitivity.txt", 1);
//        runR200( "t0-exist-key.txt", 1);
//        runR200( "t0-exist-empty.txt", 2);
//    }
//    @Test
//    public void testR250() {
//        runR250("t0-not-exist.txt", 3);
//        runR250("t0-not-exist-key.txt", 2);
//        runR250("t0-not-exist-key-string.txt", 2);
//        runR250("t0-not-exist-partial.txt", 2);
//    }
//    @Test
//    public void testR300() {
//        runR300("t0-exist-only.txt", 3);
//        runR300("t0-exist-only-key.txt", 3);
//        runR300("t0-exist-only-empty.txt", 2);
//    }
//    @Test
//    public void testR400() {
//        runR400("t0-insert.txt", 3);
//    }
//    @Test
//    public void testR500() {
//        runR500("t0-update.txt", 3);
//        runR500("t0-update-key.txt", 2);
//        runR500("t0-update-no-pk.txt", 2);
//        runR500("t0-update-where-clause.txt", 2);
//        runR500("t0-update-where-clause-pk.txt", 2);
//        runR500("t0-update-where-clause-2rows.txt", 2);
//    }
//    @Test
//    public void testR600() {
//        runR600("t0-delete.txt", 3);
//        runR600("t0-delete-key.txt", 3);
//        runR600("t0-delete-key-missing.txt", 2);
//        runR600("t0-delete-where-clause.txt", 3);
//    }
//    @Test
//    public void testR650() {
//        runR650("t0-delete-all.txt", 2);
//        runR650("t0-delete-all-with-data.txt", 2);
//        runR650("t0-delete-all-2types.txt", 2);
//    }
//
//    @Test
//    public void testR800() {
//        runR800("t0-boolean.txt", 2);
//        runR800("t0-boolean-not-null.txt", 2);
//
//        runR800("t0-int.txt", 2);
//        runR800("t0-int-max-min.txt", 2);
//        runR800("t0-int-not-null.txt", 2);
//
//        //number
//        runR800("t0-number.txt", 2);
//        runR800("t0-number-not-null.txt", 2);
//        runR800("t0-number-max-min.txt", 2);
//        runR800("t0-number-decimal.txt", 2);
//
//        //string
//        runR800("t0-string.txt", 2);
//        runR800("t0-string-not-null.txt", 2);
//        runR800("t0-string-max-min.txt", 2);
//
//        //date
//        /* https://www.postgresql.org/docs/9.5/datatype-datetime.html
//         */
//        runR800("t0-date.txt", 2);
//        runR800("t0-date-timezone.txt", 2);
//        runR800("t0-date-without-timezone.txt", 2);
//
//        //enum
//    }
//    @Test
//    public void testR850() {
//        runR850("t0-blob.txt", 2);
//        runR850("t0-blob-not-null.txt", 2);
//        runR850("t0-blob-png.txt", 1);
//    }
//    @Test
//    public void testR900() {
//        runR900("t0-rel11.txt", 1);
//        runR900("t0-rel11-too-many.txt", 1);
//        runR900("t0-rel11-backward.txt", 1);
//        runR900("t0-rel11-OO.txt", 2);
//    }
//    @Test
//    public void testR1000() {
//        runR1000("t0-relN1.txt", 1);
//        runR1000("t0-relN1-many.txt", 1);
//        runR1000("t0-relN1-backward.txt", 1);
//        runR1000("t0-relN1-OO.txt", 2);
//        runR1000("t0-relN1-OO-no-uppify.txt", 2);
//        runR1000("t0-relN1-other-table.txt", 1);
//    }
//    @Test
//    public void testR1100() {
//       //TODO fix runR1100("t0-relNN-case1.txt", 1);
//        runR1100("t0-relNN-case2.txt", 2);
//        runR1100("t0-relNN-case3.txt", 1);
//        runR1100("t0-relNN-case3a.txt", 1);
//        runR1100("t0-relNN-case3b.txt", 1);
//        runR1100("t0-relNN-insert-case3.txt", 1);
//        runR1100("t0-relNN-update-case3.txt", 2);
//        runR1100("t0-relNN-delete-case3.txt", 2);
//        runR1100("t0-relNN-case2-OO.txt", 2);
//        runR1100("t0-relNN-case3-OO.txt", 1);
//    }
//    @Test
//    public void testR1800() {
//        runR1800("t0-northwind-categories.txt", 1);
//        runR1800("t0-northwind-medium.txt", 1);
//        runR1800("t0-northwind-medium-with-errors.txt", 1);
//        runR1800("t0-northwind-full.txt", 1);
//        runR1800("t0-northwind-full-hidden-serial-pk.txt", 1);
//        runR1800("t0-northwind-full-all-relation.txt", 1);
//    }
//    @Test
//    public void testR1900() {
//        runR1900("t0-schema.txt", 2);
//    }
//
//    @Test
//    public void testRunDebug() {
////        singleTestToRunIndex = 0;
////        seedeLog = new SimpleLog();
////        seedeLog.setLevel(LogLevel.ERROR);
////        deliaLog = new SimpleLog();
////        deliaLog.setLevel(LogLevel.ERROR);
////        runR1800("t0-northwind-medium.txt", 1);
////        runR1800("t0-northwind-full-all-relation.txt", 1);
//        runR1900("t0-schema.txt", 2);
//    }
//
//    //---
//    @Before
//    public void init() {
//        UnitTestLog.disableLogging = false;
//        UnitTestLog.defaultLogLevel = LogLevel.DEBUG;
//        super.init();
//        cleanDB(DBType.POSTGRES);
//    }
//
//    @Override
//    protected DBType getDBType() {
//        return DBType.POSTGRES;
//    }
//
//
//}
