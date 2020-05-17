package org.delia.core;

import org.delia.assoc.DatIdMap;
import org.delia.db.DBInterface;
import org.delia.db.DBType;
import org.delia.db.QueryBuilderService;
import org.delia.db.QueryBuilderServiceImpl;
import org.delia.db.schema.SchemaMigrator;
import org.delia.db.sql.ConnectionFactory;
import org.delia.db.sql.ConnectionFactoryImpl;
import org.delia.error.ErrorTracker;
import org.delia.h2.H2ConnectionHelper;
import org.delia.log.Log;
import org.delia.runner.VarEvaluator;
import org.delia.type.DTypeRegistry;
import org.delia.valuebuilder.ScalarValueBuilder;
import org.delia.zdb.ZDBExecutor;
import org.delia.zdb.ZDBInterfaceFactory;
import org.delia.zdb.h2.H2ZDBConnection;
import org.delia.zdb.h2.H2ZDBExecutor;
import org.delia.zdb.h2.H2ZDBInterfaceFactory;
import org.delia.zdb.mem.MemZDBExecutor;
import org.delia.zdb.mem.MemZDBInterfaceFactory;

public class FactoryServiceImpl implements FactoryService {
	protected Log log;
	protected ErrorTracker et;
	protected TimeZoneService tzSvc;
	private ConfigureService configSvc;
	private DateFormatServiceImpl fmtSvc;
	private QueryBuilderServiceImpl queryBuilderSvc;
	private int nextGeneratedRuleId = 1;
	
	public FactoryServiceImpl(Log log, ErrorTracker et) {
		this.log = log;
		this.et = et;
		this.tzSvc = new TimeZoneServiceImpl();
		this.configSvc = new ConfigureServiceImpl(this);
		this.fmtSvc = new DateFormatServiceImpl(tzSvc);
		this.queryBuilderSvc = new QueryBuilderServiceImpl(this);
	}

	@Override
	public DateFormatService getDateFormatService() {
		return fmtSvc;
	}

	@Override
	public Log getLog() {
		return log;
	}

	@Override
	public ErrorTracker getErrorTracker() {
		return et;
	}

	@Override
	public TimeZoneService getTimeZoneService() {
		return tzSvc;
	}

	@Override
	public ConfigureService getConfigureService() {
		return configSvc;
	}

	@Override
	public QueryBuilderService getQueryBuilderService() {
		return queryBuilderSvc;
	}

	@Override
	public SchemaMigrator createSchemaMigrator(ZDBInterfaceFactory dbInterface, DTypeRegistry registry, VarEvaluator varEvaluator, DatIdMap datIdMap) {
		SchemaMigrator migrator = new SchemaMigrator(this, dbInterface, registry, varEvaluator, datIdMap);
		return migrator;
	}

	@Override
	public ScalarValueBuilder createScalarValueBuilder(DTypeRegistry registry) {
		return new ScalarValueBuilder(this, registry);
	}

	@Override
	public int getNextGeneratedRuleId() {
		return this.nextGeneratedRuleId++;
	}

	private ZDBInterfaceFactory zdbFactory = null; //just one
	public static ZDBInterfaceFactory retainedZDBFactory = null; //for bdd
	public static ZDBInterfaceFactory nextZDBToUse = null; //for bdd

	@Override
	public ZDBExecutor hackGetZDB(DTypeRegistry registry, DBType dbType) {
		if (DBType.MEM.equals(dbType)) {
			if (zdbFactory == null) {
				if (nextZDBToUse != null) {
					zdbFactory = nextZDBToUse;
					nextZDBToUse = null;
				} else {
					zdbFactory = new MemZDBInterfaceFactory(this);
				}
				retainedZDBFactory = zdbFactory;
			}
			MemZDBExecutor dbexec = new MemZDBExecutor(this, (MemZDBInterfaceFactory) zdbFactory);
			dbexec.init1(registry);
			return dbexec;
		} else if (DBType.H2.equals(dbType)) {
			ConnectionFactory connFact = new ConnectionFactoryImpl(H2ConnectionHelper.getTestDB(), log);
			H2ZDBInterfaceFactory dbFactory = new H2ZDBInterfaceFactory(this, connFact);
			
			H2ZDBConnection conn = (H2ZDBConnection) dbFactory.openConnection();
			ZDBExecutor dbexec = new H2ZDBExecutor(this, log, dbFactory, conn);
			dbexec.init1(registry);
			return dbexec;
		} else {
			return null; //not yet supported
		}
	}

	@Override
	public ZDBInterfaceFactory getHackZdbFactory() {
		return zdbFactory;
	}
}
