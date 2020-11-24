package org.delia.core;

import org.delia.assoc.DatIdMap;
import org.delia.db.QueryBuilderService;
import org.delia.db.QueryBuilderServiceImpl;
import org.delia.db.schema.SchemaMigrator;
import org.delia.dval.compare.DValueCompareService;
import org.delia.error.ErrorTracker;
import org.delia.log.Log;
import org.delia.log.LogFactory;
import org.delia.runner.VarEvaluator;
import org.delia.type.DTypeRegistry;
import org.delia.valuebuilder.ScalarValueBuilder;
import org.delia.zdb.ZDBInterfaceFactory;

public class FactoryServiceImpl implements FactoryService {
	protected Log log;
	protected ErrorTracker et;
	protected TimeZoneService tzSvc;
	private ConfigureService configSvc;
	private DateFormatServiceImpl fmtSvc;
	private QueryBuilderServiceImpl queryBuilderSvc;
	private int nextGeneratedRuleId = 1;
	private DValueCompareService compareSvc;
	private DiagnosticServiceImpl diagnosticSvc;
	private LogFactory logFactory;
	
	public FactoryServiceImpl(Log log, ErrorTracker et) {
		this(log, et, null);
	}
	public FactoryServiceImpl(Log log, ErrorTracker et, LogFactory logFactory) {
		this.log = log;
		this.et = et;
		this.tzSvc = new TimeZoneServiceImpl();
		this.configSvc = new ConfigureServiceImpl(this);
		this.fmtSvc = new DateFormatServiceImpl(tzSvc);
		this.queryBuilderSvc = new QueryBuilderServiceImpl(this);
		this.compareSvc = new DValueCompareService(this);
		this.diagnosticSvc = new DiagnosticServiceImpl(this);
		this.logFactory = logFactory;
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

	@Override
	public DValueCompareService getDValueCompareService() {
		return compareSvc;
	}

	@Override
	public DiagnosticService getDiagnosticService() {
		return diagnosticSvc;
	}
	@Override
	public LogFactory getLogFactory() {
		return logFactory;
	}

}
