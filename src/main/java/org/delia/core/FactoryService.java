package org.delia.core;

import org.delia.assoc.DatIdMap;
import org.delia.db.DBType;
import org.delia.db.QueryBuilderService;
import org.delia.db.schema.SchemaMigrator;
import org.delia.db.sqlgen.SqlGeneratorFactory;
import org.delia.dval.compare.DValueCompareService;
import org.delia.error.ErrorTracker;
import org.delia.hld.HLDSimpleQueryService;
import org.delia.log.Log;
import org.delia.log.LogFactory;
import org.delia.runner.FetchRunner;
import org.delia.runner.VarEvaluator;
import org.delia.type.DTypeRegistry;
import org.delia.validation.ValidationRunner;
import org.delia.valuebuilder.ScalarValueBuilder;
import org.delia.zdb.DBInterfaceFactory;

/**
 * To allow multiple clients of Delia to each
 * run concurrently with their own customizations, we
 * need a central factory service.
 * 
 * Each Delia instance has its own factory service object.
 * 
 * @author Ian Rae
 *
 */
public interface FactoryService {

	ConfigureService getConfigureService();
	TimeZoneService getTimeZoneService();
	DateFormatService getDateFormatService();
	Log getLog();
	LogFactory getLogFactory(); //may return null if not set in DeliaBuilder
	ErrorTracker getErrorTracker();
	QueryBuilderService getQueryBuilderService();
	HLDSimpleQueryService createHLDSimpleQueryService(DBInterfaceFactory dbInterface, DTypeRegistry registry);
	SchemaMigrator createSchemaMigrator(DBInterfaceFactory dbInterface, DTypeRegistry registry, VarEvaluator varEvaluator, DatIdMap datIdMap);
	ScalarValueBuilder createScalarValueBuilder(DTypeRegistry registry);
	int getNextGeneratedRuleId();
	DValueCompareService getDValueCompareService();
	DiagnosticService getDiagnosticService();
	ValidationRunner createValidationRunner(DBInterfaceFactory dbInterface, FetchRunner fetchRunner);
	boolean getEnableMEMSqlGenerationFlag();
	void setEnableMEMSqlGenerationFlag(boolean flag);
	SqlGeneratorFactory createSqlFactory(DBType dbtype, DTypeRegistry registry);
}
