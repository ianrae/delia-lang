package org.delia;

import org.delia.core.FactoryService;
import org.delia.log.DeliaLog;
import org.delia.runner.ResultValue;
import org.delia.type.DValue;
import org.delia.db.DBInterfaceFactory;

import java.io.BufferedReader;

/**
 * An instance of the Delia compiler and runtime.
 * Represents a long-term connection to a single database.
 * Most applications will have a single Delia object, unless
 * they use multiple databases.
 * <p>
 * Thread-safe.
 *
 * @author Ian Rae
 */
public interface Delia {
    ResultValue execute(String src);

    DeliaSession beginSession(String src);

    ResultValue continueExecution(String src, DeliaSession dbsess);

    //    DeliaSession executeMigrationPlan(String src, MigrationPlan plan);
    DeliaLog getLog();

    FactoryService getFactoryService();

    //    DeliaCompiler createCompiler();
    DeliaOptions getOptions();

    DBInterfaceFactory getDBInterface();
//    HLDFactory getHLDFactory();

    ResultValue execute(BufferedReader reader);

    DeliaSession beginSession(BufferedReader reader);

    ResultValue continueExecution(BufferedReader reader, DeliaSession dbsess);
//    DeliaSession executeMigrationPlan(BufferedReader reader, MigrationPlan plan);


    void injectVar(String varName, DValue dval); //only single values for now
}