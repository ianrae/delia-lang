package org.delia.core;

import org.delia.db.DBInterface;
import org.delia.db.QueryBuilderService;
import org.delia.db.QueryBuilderServiceImpl;
import org.delia.db.schema.SchemaMigrator;
import org.delia.error.ErrorTracker;
import org.delia.log.Log;
import org.delia.runner.VarEvaluator;
import org.delia.type.DTypeRegistry;
import org.delia.valuebuilder.ScalarValueBuilder;

public class FactoryServiceImpl implements FactoryService {
	protected Log log;
	protected ErrorTracker et;
	protected TimeZoneService tzSvc;
	private ConfigureService configSvc;
	private DateFormatServiceImpl fmtSvc;
	private QueryBuilderServiceImpl queryBuilderSvc;

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
	public SchemaMigrator createSchemaMigrator(DBInterface dbInterface, DTypeRegistry registry, VarEvaluator varEvaluator) {
		SchemaMigrator migrator = new SchemaMigrator(this, dbInterface, registry, varEvaluator);
		return migrator;
	}

	@Override
	public ScalarValueBuilder createScalarValueBuilder(DTypeRegistry registry) {
		return new ScalarValueBuilder(this, registry);
	}
}
