package org.delia.db.schema;

/**
 * Always allow automatic migrations. Useful during development but risky because
 * no all migration steps are low-risk. 
 * @author Ian Rae
 *
 */
public class AlwaysYesMigrationPolicy implements MigrationPolicy {

	@Override
	public boolean shouldMigrationOccur(MigrationPlan plan) {
		return true;
	}
	@Override
	public boolean shouldPerformRiskChecks() {
		return false;
	}

}
