package org.delia.api;

import org.delia.zdb.DBObserverFactory;
import org.delia.zdb.ZDBExecutor;

/**
 * Options that can be changed during the lifetime of a Delia object.
 * 
 * @author Ian Rae
 *
 */
public class DeliaOptions {
	public boolean disableSQLLoggingDuringSchemaMigration = true;
	public MigrationAction migrationAction = MigrationAction.MIGRATE;
	public boolean enableExecution = true;
	public boolean useSafeMigrationPolicy = true;
	public boolean enableAutomaticMigrations = true;
	public boolean saveParseExpObjectsInSession = true; //can be helpful for troubleshooting. not needed by Delia
	public boolean logSourceBeforeCompile; //log all delia source before it is compiled
	public DBObserverFactory dbObserverFactory;
}
