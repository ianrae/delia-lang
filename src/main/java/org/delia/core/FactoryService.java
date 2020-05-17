package org.delia.core;

import org.delia.assoc.DatIdMap;
import org.delia.db.DBInterface;
import org.delia.db.DBType;
import org.delia.db.QueryBuilderService;
import org.delia.db.schema.SchemaMigrator;
import org.delia.error.ErrorTracker;
import org.delia.log.Log;
import org.delia.runner.VarEvaluator;
import org.delia.type.DTypeRegistry;
import org.delia.valuebuilder.ScalarValueBuilder;
import org.delia.zdb.ZDBExecutor;
import org.delia.zdb.ZDBInterfaceFactory;
import org.delia.zdb.mem.MemZDBInterfaceFactory;

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
	ErrorTracker getErrorTracker();
	QueryBuilderService getQueryBuilderService();
	SchemaMigrator createSchemaMigrator(ZDBInterfaceFactory dbInterface, DTypeRegistry registry, VarEvaluator varEvaluator, DatIdMap datIdMap);
	ScalarValueBuilder createScalarValueBuilder(DTypeRegistry registry);
	int getNextGeneratedRuleId();
	
	ZDBExecutor hackGetZDB(DTypeRegistry registry, DBType dbType); //TODO remove later!!
	ZDBInterfaceFactory getHackZdbFactory();
}
