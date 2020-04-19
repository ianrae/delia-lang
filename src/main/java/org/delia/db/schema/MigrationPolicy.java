package org.delia.db.schema;

public interface MigrationPolicy {
	boolean shouldMigrationOccur(MigrationPlan plan);
	boolean shouldPerformRiskChecks();
}
