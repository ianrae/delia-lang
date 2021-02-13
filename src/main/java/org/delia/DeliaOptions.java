package org.delia;

import org.delia.api.MigrationAction;
import org.delia.zdb.DBObserverFactory;

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
	public boolean observeHLDSQLOnly = true;
	public boolean executeInTransaction = false; //use when want execute()/continueExeuction() in transaction
}
