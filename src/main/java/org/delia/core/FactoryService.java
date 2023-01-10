package org.delia.core;

import org.delia.dval.compare.DValueCompareService;
import org.delia.error.ErrorTracker;
import org.delia.log.DeliaLog;
import org.delia.log.LogFactory;
import org.delia.rule.RuleFunctionFactory;
import org.delia.transaction.TransactionProvider;
import org.delia.type.DTypeRegistry;
import org.delia.validation.ValidationRunner;
import org.delia.valuebuilder.ScalarValueBuilder;
import org.delia.db.DBInterfaceFactory;
import org.delia.runner.DeliaRunner;

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
	DeliaLog getLog();
	LogFactory getLogFactory(); //may return null if not set in DeliaBuilder
	ErrorTracker getErrorTracker();
//	QueryBuilderService getQueryBuilderService();
//	HLDSimpleQueryService createHLDSimpleQueryService(DBInterfaceFactory dbInterface, DTypeRegistry registry);
//	SchemaMigrator createSchemaMigrator(DBInterfaceFactory dbInterface, DTypeRegistry registry, VarEvaluator varEvaluator, DatIdMap datIdMap, String defaultSchema);
	ScalarValueBuilder createScalarValueBuilder(DTypeRegistry registry);
//	int getNextGeneratedRuleId();
	DValueCompareService getDValueCompareService();
//	DiagnosticService getDiagnosticService();
	ValidationRunner createValidationRunner(DBInterfaceFactory dbInterface, DTypeRegistry registry, DeliaRunner deliaRunner);
//	boolean getEnableMEMSqlGenerationFlag();
//	void setEnableMEMSqlGenerationFlag(boolean flag);
	RuleFunctionFactory createRuleFunctionFactory();
	TransactionProvider createTransactionProvider(DBInterfaceFactory dbInterface);
//	MigrationService createMigrationService(DBInterfaceFactory dbInterface);
}
