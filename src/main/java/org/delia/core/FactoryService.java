package org.delia.core;

import org.delia.assoc.DatIdMap;
import org.delia.db.QueryBuilderService;
import org.delia.db.schema.SchemaMigrator;
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
import org.delia.zdb.ZDBInterfaceFactory;

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
//	HLSSimpleQueryService createSimpleQueryService(ZDBInterfaceFactory dbInterface, DTypeRegistry registry);
	HLDSimpleQueryService createHLDSimpleQueryService(ZDBInterfaceFactory dbInterface, DTypeRegistry registry);
	SchemaMigrator createSchemaMigrator(ZDBInterfaceFactory dbInterface, DTypeRegistry registry, VarEvaluator varEvaluator, DatIdMap datIdMap);
	ScalarValueBuilder createScalarValueBuilder(DTypeRegistry registry);
	int getNextGeneratedRuleId();
	DValueCompareService getDValueCompareService();
	DiagnosticService getDiagnosticService();
	ValidationRunner createValidationRunner(ZDBInterfaceFactory dbInterface, FetchRunner fetchRunner);
}
