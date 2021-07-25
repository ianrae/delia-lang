package org.delia.db.schema.modify;

import org.delia.assoc.AssocService;
import org.delia.assoc.AssocServiceImpl;
import org.delia.assoc.DatIdMap;
import org.delia.compiler.ast.BooleanExp;
import org.delia.compiler.ast.FilterExp;
import org.delia.compiler.ast.IdentExp;
import org.delia.compiler.ast.QueryExp;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.DBType;
import org.delia.db.QuerySpec;
import org.delia.db.schema.AlwaysNoMigrationPolicy;
import org.delia.db.schema.AlwaysYesMigrationPolicy;
import org.delia.db.schema.DatMapBuilder;
import org.delia.db.schema.MigrationPlan;
import org.delia.db.schema.MigrationPolicy;
import org.delia.db.schema.MigrationService;
import org.delia.db.schema.SafeMigrationPolicy;
import org.delia.db.schema.SchemaMigrator;
import org.delia.hld.HLDSimpleQueryService;
import org.delia.runner.DoNothingVarEvaluator;
import org.delia.runner.QueryResponse;
import org.delia.runner.VarEvaluator;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.util.render.ObjectRendererImpl;
import org.delia.zdb.DBExecutor;
import org.delia.zdb.DBInterfaceFactory;

public class SxMigrationServiceImpl extends ServiceBase implements MigrationService {
	private DBInterfaceFactory dbInterface;
	private MigrationPolicy policy;
	private ObjectRendererImpl ori = new ObjectRendererImpl(); //TODO: careful we never change this. it would break all stored db fingerprint
	private String currentFingerprint;
	private String dbFingerprint;
	private SchemaDefinition currentSchema;
	private String defaultSchema;

	public SxMigrationServiceImpl(DBInterfaceFactory dbInterface, FactoryService factorySvc) {
		super(factorySvc);
		this.dbInterface = dbInterface;
		this.factorySvc = factorySvc;
		this.policy = new SafeMigrationPolicy();
	}

	@Override
	public void setDefaultSchema(String defaultSchema) {
		this.defaultSchema = defaultSchema;
	}

	/* (non-Javadoc)
	 * @see org.delia.db.schema.MigrationService#autoMigrateDbIfNeeded(org.delia.type.DTypeRegistry, org.delia.runner.VarEvaluator, org.delia.assoc.DatIdMap)
	 */
	@Override
	public boolean autoMigrateDbIfNeeded(DTypeRegistry registry, VarEvaluator varEvaluator, DatIdMap datIdMap) {
		
		try(SchemaMigrator migrator = factorySvc.createSchemaMigrator(dbInterface, registry, varEvaluator, datIdMap, defaultSchema)) {
			migrator.createSchemaTableIfNeeded();
			
			boolean b = doNeedsMigration(registry, migrator);
			log.logDebug("sxMIGRATION needed: %b", b);
			if (b) {
				SxMigrationPlan sxplan = generateMigrationPlan(registry, migrator, datIdMap);
//				MigrationPlan plan = migrator.generateMigrationPlan();
				MigrationPlan plan = new MigrationPlan(); //TODO fix later
				
				if (policy.shouldMigrationOccur(plan)) {
					boolean performRiskChecks = policy.shouldPerformRiskChecks();
					//TODO: implement checks later
//					b = migrator.performMigrations(performRiskChecks);
					try(SxPreRunChecker preRunChecker = new SxPreRunChecker(factorySvc, dbInterface, registry, varEvaluator, datIdMap)) {
						b = preRunChecker.preRunCheck(sxplan.delta, performRiskChecks);
					} catch(Exception e) {
						log.logError("SxPreRunChecker failed!");
					}
					if (!b) {
						return false;
					}
					
					log.log("OP(%d): %s", sxplan.opList.size(), ori.render(sxplan.opList));
					b = migrator.sxPerformMigrations(currentFingerprint, sxplan.opList);
					if (! b) {
						return false;
					}
				} else {
					log.logError("MIGRATION rejected due to policy : %s", policy.getClass().getSimpleName());
					log.log("=== MIGRATION PLAN ===");
					for(SchemaChangeOperation op: sxplan.opList) {
						String s = ori.render(op);
						log.log(s);
					}
					log.log("=== END MIGRATION PLAN ===");
					return false;
				}
			}
		}
		return true;
	}
	private SxMigrationPlan generateMigrationPlan(DTypeRegistry registry, SchemaMigrator migrator, DatIdMap datIdMap) {
		if (currentSchema == null) {
			SchemaDefinitionGenerator schemaDefGen = new SchemaDefinitionGenerator(registry, factorySvc);
			currentSchema = schemaDefGen.generate();
		}
		
		SxDatMapBuilderImpl datMapBuilder = new SxDatMapBuilderImpl(registry, factorySvc, migrator.getZDBExecutor());
		SchemaDefinition prevSchema = datMapBuilder.parseJson(dbFingerprint);
		SchemaDeltaGenerator deltaGen = new SchemaDeltaGenerator(registry, factorySvc);
		SchemaDelta delta = deltaGen.generate(prevSchema, currentSchema);
		
		DBType dbType = dbInterface.getDBType();
		SchemaDeltaOptimizer optimizer = new SchemaDeltaOptimizer(registry, factorySvc, dbType, datIdMap);
		delta = optimizer.optimize(delta);
		
		SchemaMigrationPlanGenerator plangen = new SchemaMigrationPlanGenerator(registry, factorySvc, dbType);
		SxMigrationPlan sxplan = new SxMigrationPlan();
		sxplan.opList = plangen.generate(delta);
		sxplan.delta = delta;
		return sxplan;
	}

	private boolean doNeedsMigration(DTypeRegistry registry, SchemaMigrator migrator) {
		SchemaDefinitionGenerator schemaDefGen = new SchemaDefinitionGenerator(registry, factorySvc);
		SchemaDefinition schema = schemaDefGen.generate();
		this.currentSchema = schema;
		this.currentFingerprint = ori.render(schema);
		this.dbFingerprint = calcDBFingerprint(registry, migrator.getZDBExecutor());
		
		log.log("ZZZ DB is: %s", dbFingerprint);

		return !currentFingerprint.equals(dbFingerprint);
	}
	public String calcDBFingerprint(DTypeRegistry registry, DBExecutor zexec) {
		//TODO: query just single record (most recent);
		FilterExp filter = new FilterExp(99, new BooleanExp(true)); //query all
		QuerySpec spec = new QuerySpec();
		spec.queryExp = new QueryExp(99, new IdentExp(SchemaMigrator.SCHEMA_TABLE), filter, null);
		HLDSimpleQueryService querySvc = factorySvc.createHLDSimpleQueryService(zexec.getDbInterface(), registry);
		QueryResponse qresp = querySvc.execQuery(spec.queryExp, zexec);
		//TODO: should specify orderby id!!
		
		if (qresp.emptyResults()) {
			return "";
		}

		//there may be multiple rows
		int n = qresp.dvalList.size();
		DValue dval = qresp.dvalList.get(n - 1); //last one
		return dval.asStruct().getField("fingerprint").asString();
	}

	/* (non-Javadoc)
	 * @see org.delia.db.schema.MigrationService#createMigrationPlan(org.delia.type.DTypeRegistry, org.delia.runner.VarEvaluator, org.delia.assoc.DatIdMap)
	 */
	@Override
	public MigrationPlan createMigrationPlan(DTypeRegistry registry, VarEvaluator varEvaluator, DatIdMap datIdMap) {
		try(SchemaMigrator migrator = factorySvc.createSchemaMigrator(dbInterface, registry, varEvaluator, datIdMap, defaultSchema)) {
			migrator.createSchemaTableIfNeeded();
//			boolean b = migrator.dbNeedsMigration();
			boolean b = this.doNeedsMigration(registry, migrator);
			log.log("MIGRATION PLAN: %b", b);
			MigrationPlan plan = migrator.generateMigrationPlan(); //TODO change to opsList later
			return plan;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.delia.db.schema.MigrationService#runMigrationPlan(org.delia.type.DTypeRegistry, org.delia.db.schema.MigrationPlan, org.delia.runner.VarEvaluator, org.delia.assoc.DatIdMap)
	 */
	@Override
	public MigrationPlan runMigrationPlan(DTypeRegistry registry, MigrationPlan plan, VarEvaluator varEvaluator, DatIdMap datIdMap) {
		try(SchemaMigrator migrator = factorySvc.createSchemaMigrator(dbInterface, registry, varEvaluator, datIdMap, defaultSchema)) {
			migrator.createSchemaTableIfNeeded();
//			boolean b = migrator.dbNeedsMigration();
			boolean b = this.doNeedsMigration(registry, migrator);
			log.log("RUN MIGRATION PLAN: %b", b);
//			plan = migrator.runMigrationPlan(plan);
			SxMigrationPlan sxplan = generateMigrationPlan(registry, migrator, datIdMap);
			b = migrator.sxPerformMigrations(currentFingerprint, sxplan.opList);
			return new MigrationPlan(); //TODO fix later to opsList
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
		try(SchemaMigrator migrator = factorySvc.createSchemaMigrator(dbInterface, registry, new DoNothingVarEvaluator(), null, defaultSchema)) {
			migrator.createSchemaTableIfNeeded();
			
			DatMapBuilder datMapBuilder = new SxDatMapBuilderImpl(registry, factorySvc, migrator.getZDBExecutor());
			AssocService assocSvc = new AssocServiceImpl(migrator, datMapBuilder, factorySvc, factorySvc.getErrorTracker());
			assocSvc.assignDATIds(registry);
			datIdMap = assocSvc.getDatIdMap();
			
//			//ok we can init db executor now
//			migrator.getZDBExecutor().init2(datIdMap, varEvaluator);
		}
		return datIdMap;
	}

}