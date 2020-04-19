package org.delia.db.schema;

/**
 * Only allow migrations where there are no Deletions or Renames
 * @author Ian Rae
 *
 */
public class SafeMigrationPolicy implements MigrationPolicy {

	@Override
	public boolean shouldMigrationOccur(MigrationPlan plan) {
		int delCount = 0;
//		for(SchemaType action: plan.diffL) {
//			if (action.isFieldDelete() || action.isTblDelete()) {
//				delCount++;
//			}
//		}
		
		return delCount == 0;
	}
	@Override
	public boolean shouldPerformRiskChecks() {
		return true;
	}

}
