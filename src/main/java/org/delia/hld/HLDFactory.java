package org.delia.hld;

import org.delia.assoc.DatIdMap;
import org.delia.core.FactoryService;
import org.delia.db.DBType;
import org.delia.db.sqlgen.SqlGeneratorFactory;
import org.delia.hld.cud.HLDToSQLConverter;
import org.delia.sprig.SprigService;
import org.delia.type.DTypeRegistry;

/**
 * Creates various HLD services, to allow support of different databases, and sql generation strategies
 * @author irae
 *
 */
public interface HLDFactory {
	HLDBuildService createHLDBuilderService(DTypeRegistry registry, FactoryService factorySvc, DatIdMap datIdMap, SprigService sprigSvc, DBType dbType, String defaultSchema);
	HLDToSQLConverter createConverter(FactoryService factorySvc, DTypeRegistry registry, DBType dbType, String defaultSchema);
	SqlGeneratorFactory createSqlFactory(DBType dbtype, FactoryService factorySvc, DTypeRegistry registry, String defaultSchema);
	
}
