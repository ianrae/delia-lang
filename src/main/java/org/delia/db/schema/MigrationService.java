package org.delia.db.schema;

import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.DBInterface;
import org.delia.runner.VarEvaluator;
import org.delia.type.DTypeRegistry;

public class MigrationService extends ServiceBase {
	private DBInterface dbInterface;
	private MigrationPolicy policy;

	public MigrationService(DBInterface dbInterface, FactoryService factorySvc) {
		super(factorySvc);
		this.dbInterface = dbInterface;
		this.factorySvc = factorySvc;
		this.policy = new SafeMigrationPolicy();
	}

	/**
	 * Now that we know the types, compare against db schema and
	 * perform schema migration if needed.
	 * @param registry - type registry
	 * @param varEvaluator - for evaluating delia var references
	 * @return success flag
	 */
	public boolean autoMigrateDbIfNeeded(DTypeRegistry registry, VarEvaluator varEvaluator) {
		try(SchemaMigrator migrator = factorySvc.createSchemaMigrator(dbInterface, registry, varEvaluator)) {
			migrator.createSchemaTableIfNeeded();
			boolean b = migrator.dbNeedsMigration();
			log.logDebug("MIGRATION needed: %b", b);
			if (b) {
				MigrationPlan plan = migrator.generateMigrationPlan();
				if (policy.shouldMigrationOccur(plan)) {
					boolean performRiskChecks = policy.shouldPerformRiskChecks();
					b = migrator.performMigrations(performRiskChecks);
					if (! b) {
						return false;
					}
				} else {
					log.logError("MIGRATION rejected due to policy : %s", policy.getClass().getSimpleName());
					log.log("=== MIGRATION PLAN ===");
					for(SchemaType ss: plan.diffL) {
						log.log(ss.getSummary());
					}
					log.log("=== END MIGRATION PLAN ===");
					migrator.close();
					return false;
				}
			}
		}
		return true;
	}
	public MigrationPlan createMigrationPlan(DTypeRegistry registry, VarEvaluator varEvaluator) {
		try(SchemaMigrator migrator = factorySvc.createSchemaMigrator(dbInterface, registry, varEvaluator)) {
			migrator.createSchemaTableIfNeeded();
			boolean b = migrator.dbNeedsMigration();
			log.log("MIGRATION PLAN: %b", b);
			MigrationPlan plan = migrator.generateMigrationPlan();
			return plan;
		}
	}
	
	/**
	 * ignore policy and do the migration.
	 * @param registry type registry
	 * @param plan migration plan
	 * @param varEvaluator variable evaluator
	 * @return plan
	 */
	public MigrationPlan runMigrationPlan(DTypeRegistry registry, MigrationPlan plan, VarEvaluator varEvaluator) {
		try(SchemaMigrator migrator = factorySvc.createSchemaMigrator(dbInterface, registry, varEvaluator)) {
			migrator.createSchemaTableIfNeeded();
			boolean b = migrator.dbNeedsMigration();
			log.log("RUN MIGRATION PLAN: %b", b);
			plan = migrator.runMigrationPlan(plan);
			return plan;
		}
	}

	public void initPolicy(boolean useSafeMigrationPolicy, boolean enableAutomaticMigrations) {
		if (! enableAutomaticMigrations) {
			this.policy = new AlwaysNoMigrationPolicy();
		} else if (useSafeMigrationPolicy) {
			this.policy = new SafeMigrationPolicy();
		} else {
			this.policy = new AlwaysYesMigrationPolicy();
		}
	}


}