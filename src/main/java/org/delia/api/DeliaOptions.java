package org.delia.api;

import org.delia.runner.DValueIterator;

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
	public DValueIterator insertPrebuiltValueIterator = null;
}
