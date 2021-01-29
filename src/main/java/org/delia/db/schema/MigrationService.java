package org.delia.db.schema;

import org.delia.assoc.AssocService;
import org.delia.assoc.AssocServiceImpl;
import org.delia.assoc.DatIdMap;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.hld.HLDFactory;
import org.delia.runner.DoNothingVarEvaluator;
import org.delia.runner.VarEvaluator;
import org.delia.type.DTypeRegistry;
import org.delia.zdb.DBInterfaceFactory;

public class MigrationService extends ServiceBase {
	private DBInterfaceFactory dbInterface;
	private MigrationPolicy policy;
	private HLDFactory hldFactory;

	public MigrationService(DBInterfaceFactory dbInterface, HLDFactory hldFactory, FactoryService factorySvc) {
		super(factorySvc);
		this.dbInterface = dbInterface;
		this.hldFactory = hldFactory;
		this.factorySvc = factorySvc;
		this.policy = new SafeMigrationPolicy();
	}

	/**
	 * Now that we know the types, compare against db schema and
	 * perform schema migration if needed.
	 * @param registry - type registry
	 * @param varEvaluator - for evaluating delia var references
	 * @param datIdMap 
	 * @return success flag
	 */
	public boolean autoMigrateDbIfNeeded(DTypeRegistry registry, VarEvaluator varEvaluator, DatIdMap datIdMap) {
		try(SchemaMigrator migrator = factorySvc.createSchemaMigrator(dbInterface, registry, varEvaluator, datIdMap)) {
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
					return false;
				}
			}
		}
		return true;
	}
	public MigrationPlan createMigrationPlan(DTypeRegistry registry, VarEvaluator varEvaluator, DatIdMap datIdMap) {
		try(SchemaMigrator migrator = factorySvc.createSchemaMigrator(dbInterface, registry, varEvaluator, datIdMap)) {
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
	 * @param datIdMap 
	 * @return plan
	 */
	public MigrationPlan runMigrationPlan(DTypeRegistry registry, MigrationPlan plan, VarEvaluator varEvaluator, DatIdMap datIdMap) {
		try(SchemaMigrator migrator = factorySvc.createSchemaMigrator(dbInterface, registry, varEvaluator, datIdMap)) {
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

	public DatIdMap loadDATData(DTypeRegistry registry, VarEvaluator varEvaluator) {
		DatIdMap datIdMap = null;
		try(SchemaMigrator migrator = factorySvc.createSchemaMigrator(dbInterface, registry, new DoNothingVarEvaluator(), null)) {
			migrator.createSchemaTableIfNeeded();
			AssocService assocSvc = new AssocServiceImpl(migrator, factorySvc, factorySvc.getErrorTracker());
			assocSvc.assignDATIds(registry);
			datIdMap = assocSvc.getDatIdMap();
			
//			//ok we can init db executor now
//			migrator.getZDBExecutor().init2(datIdMap, varEvaluator);
		}
		return datIdMap;
	}

}