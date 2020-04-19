package org.delia.api;

/**
 * What type of migration to do at Delia session start time.
 * @author Ian Rae
 *
 */
public enum MigrationAction {
	MIGRATE,
	GENERATE_MIGRATION_PLAN,
	RUN_MIGRATION_PLAN,
	DO_NOTHING
}
