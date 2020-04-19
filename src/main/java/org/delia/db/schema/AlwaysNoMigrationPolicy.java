package org.delia.db.schema;

/**
 * Never allow automatic migrations. Often used on production systems where
 * we want a human to review all migration plans.
 * @author Ian Rae
 *
 */
public class AlwaysNoMigrationPolicy implements MigrationPolicy {

	@Override
	public boolean shouldMigrationOccur(MigrationPlan plan) {
		return false;
	}

	@Override
	public boolean shouldPerformRiskChecks() {
		return true;
	}

}
