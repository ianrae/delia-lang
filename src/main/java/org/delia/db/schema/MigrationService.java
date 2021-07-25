package org.delia.db.schema;

import org.delia.assoc.DatIdMap;
import org.delia.runner.VarEvaluator;
import org.delia.type.DTypeRegistry;

public interface MigrationService {

	/**
	 * Now that we know the types, compare against db schema and
	 * perform schema migration if needed.
	 * @param registry - type registry
	 * @param varEvaluator - for evaluating delia var references
	 * @param datIdMap 
	 * @return success flag
	 */
	boolean autoMigrateDbIfNeeded(DTypeRegistry registry, VarEvaluator varEvaluator, DatIdMap datIdMap);

	MigrationPlan createMigrationPlan(DTypeRegistry registry, VarEvaluator varEvaluator, DatIdMap datIdMap);

	/**
	 * ignore policy and do the migration.
	 * @param registry type registry
	 * @param plan migration plan
	 * @param varEvaluator variable evaluator
	 * @param datIdMap 
	 * @return plan
	 */
	MigrationPlan runMigrationPlan(DTypeRegistry registry, MigrationPlan plan, VarEvaluator varEvaluator,
			DatIdMap datIdMap);

	void initPolicy(boolean useSafeMigrationPolicy, boolean enableAutomaticMigrations);

	DatIdMap loadDATData(DTypeRegistry registry, VarEvaluator varEvaluator, String defaultSchema);

}