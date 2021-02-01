package org.delia.db.schema;

import org.delia.assoc.AssocService;
import org.delia.assoc.AssocServiceImpl;
import org.delia.assoc.DatIdMap;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.runner.DoNothingVarEvaluator;
import org.delia.runner.VarEvaluator;
import org.delia.type.DTypeRegistry;
import org.delia.zdb.DBInterfaceFactory;

public class MigrationServiceImpl extends ServiceBase implements MigrationService {
	private DBInterfaceFactory dbInterface;
	private MigrationPolicy policy;

	public MigrationServiceImpl(DBInterfaceFactory dbInterface, FactoryService factorySvc) {
		super(factorySvc);
		this.dbInterface = dbInterface;
		this.factorySvc = factorySvc;
		this.policy = new SafeMigrationPolicy();
	}

	/* (non-Javadoc)
	 * @see org.delia.db.schema.MigrationService#autoMigrateDbIfNeeded(org.delia.type.DTypeRegistry, org.delia.runner.VarEvaluator, org.delia.assoc.DatIdMap)
	 */
	@Override
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
	/* (non-Javadoc)
	 * @see org.delia.db.schema.MigrationService#createMigrationPlan(org.delia.type.DTypeRegistry, org.delia.runner.VarEvaluator, org.delia.assoc.DatIdMap)
	 */
	@Override
	public MigrationPlan createMigrationPlan(DTypeRegistry registry, VarEvaluator varEvaluator, DatIdMap datIdMap) {
		try(SchemaMigrator migrator = factorySvc.createSchemaMigrator(dbInterface, registry, varEvaluator, datIdMap)) {
			migrator.createSchemaTableIfNeeded();
			boolean b = migrator.dbNeedsMigration();
			log.log("MIGRATION PLAN: %b", b);
			MigrationPlan plan = migrator.generateMigrationPlan();
			return plan;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.delia.db.schema.MigrationService#runMigrationPlan(org.delia.type.DTypeRegistry, org.delia.db.schema.MigrationPlan, org.delia.runner.VarEvaluator, org.delia.assoc.DatIdMap)
	 */
	@Override
	public MigrationPlan runMigrationPlan(DTypeRegistry registry, MigrationPlan plan, VarEvaluator varEvaluator, DatIdMap datIdMap) {
		try(SchemaMigrator migrator = factorySvc.createSchemaMigrator(dbInterface, registry, varEvaluator, datIdMap)) {
			migrator.createSchemaTableIfNeeded();
			boolean b = migrator.dbNeedsMigration();
			log.log("RUN MIGRATION PLAN: %b", b);
			plan = migrator.runMigrationPlan(plan);
			return plan;
		}
	}

	/* (non-Javadoc)
	 * @see org.delia.db.schema.MigrationService#initPolicy(boolean, boolean)
	 */
	@Override
	public void initPolicy(boolean useSafeMigrationPolicy, boolean enableAutomaticMigrations) {
		if (! enableAutomaticMigrations) {
			this.policy = new AlwaysNoMigrationPolicy();
		} else if (useSafeMigrationPolicy) {
			this.policy = new SafeMigrationPolicy();
		} else {
			this.policy = new AlwaysYesMigrationPolicy();
		}
	}

	/* (non-Javadoc)
	 * @see org.delia.db.schema.MigrationService#loadDATData(org.delia.type.DTypeRegistry, org.delia.runner.VarEvaluator)
	 */
	@Override
	public DatIdMap loadDATData(DTypeRegistry registry, VarEvaluator varEvaluator) {
		DatIdMap datIdMap = null;
		try(SchemaMigrator migrator = factorySvc.createSchemaMigrator(dbInterface, registry, new DoNothingVarEvaluator(), null)) {
			migrator.createSchemaTableIfNeeded();
			DatMapBuilder datMapBuilder = new DatMapBuilderImpl(registry, factorySvc, migrator.getZDBExecutor(), migrator);
			AssocService assocSvc = new AssocServiceImpl(migrator, datMapBuilder, factorySvc, factorySvc.getErrorTracker());
			assocSvc.assignDATIds(registry);
			datIdMap = assocSvc.getDatIdMap();
			
//			//ok we can init db executor now
//			migrator.getZDBExecutor().init2(datIdMap, varEvaluator);
		}
		return datIdMap;
	}

}