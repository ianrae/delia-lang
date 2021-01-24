package org.delia.api;

import java.io.BufferedReader;

import org.delia.compiler.DeliaCompiler;
import org.delia.core.FactoryService;
import org.delia.db.schema.MigrationPlan;
import org.delia.log.Log;
import org.delia.runner.ResultValue;
import org.delia.zdb.DBInterfaceFactory;

/**
 * An instance of the Delia compiler and runtime.
 * Represents a long-term connection to a single database.
 * Most applications will have a single Delia object, unless
 * they use multiple databases.
 * 
 * Thread-safe.
 * 
 * @author Ian Rae
 *
 */
public interface Delia {
	ResultValue execute(String src);
	DeliaSession beginSession(String src);
	ResultValue continueExecution(String src, DeliaSession dbsess);
	DeliaSession executeMigrationPlan(String src, MigrationPlan plan);
	Log getLog();
	FactoryService getFactoryService();
	DeliaCompiler createCompiler();
	DeliaOptions getOptions();
	DBInterfaceFactory getDBInterface();
	
	ResultValue execute(BufferedReader reader);
	DeliaSession beginSession(BufferedReader reader);
	ResultValue continueExecution(BufferedReader reader, DeliaSession dbsess);
	DeliaSession executeMigrationPlan(BufferedReader reader, MigrationPlan plan);
}