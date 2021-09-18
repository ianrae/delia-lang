package org.delia.bddnew;

import org.delia.base.UnitTestLog;
import org.delia.db.DBType;
import org.delia.log.LogLevel;
import org.delia.log.SimpleLog;
import org.junit.Before;
import org.junit.Test;

/**
 * Logging and error messages are an import part of user experience
 *
 */
public class LoggingBDDTests extends BDDTestBase {



    @Test
    public void testRunDebug() {
//        singleTestToRunIndex = 0;
//        runR1100("t0-relNN-delete-case3.txt", 2);
        seedeLog = new SimpleLog();
        seedeLog.setLevel(LogLevel.ERROR);
        deliaLog = new SimpleLog();
        deliaLog.setLevel(LogLevel.ERROR);
        runR300("t0-bool.txt", 1);
//        runR1800("t0-northwind-medium.txt", 1);
    }

    //---
    @Before
    public void init() {
        UnitTestLog.disableLogging = false;
        UnitTestLog.defaultLogLevel = LogLevel.ERROR;
//        super.init();
        cleanDB(DBType.POSTGRES);
    }

    @Override
    protected DBType getDBType() {
        return DBType.POSTGRES;
    }


}
