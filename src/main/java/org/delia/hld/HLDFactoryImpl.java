package org.delia.hld;

import org.delia.assoc.DatIdMap;
import org.delia.core.FactoryService;
import org.delia.db.DBType;
import org.delia.db.postgres.PostgresSqlGeneratorFactory;
import org.delia.db.sqlgen.SqlGeneratorFactory;
import org.delia.db.sqlgen.SqlGeneratorFactoryImpl;
import org.delia.hld.cud.HLDToSQLConverter;
import org.delia.hld.cud.HLDToSQLConverterImpl;
import org.delia.sprig.SprigService;
import org.delia.type.DTypeRegistry;

public class HLDFactoryImpl implements HLDFactory {

	@Override
	public HLDBuildService createHLDBuilderService(DTypeRegistry registry, FactoryService factorySvc, DatIdMap datIdMap,
			SprigService sprigSvc, DBType dbType, String defaultSchema) {
		
		HLDToSQLConverter converter = this.createConverter(factorySvc, registry, dbType, defaultSchema);
		return new HLDBuildServiceImpl(registry, factorySvc, datIdMap, sprigSvc, dbType, converter);
	}

	@Override
	public HLDToSQLConverter createConverter(FactoryService factorySvc, DTypeRegistry registry, DBType dbType, String defaultSchema) {
		SqlGeneratorFactory sqlgen = this.createSqlFactory(dbType, factorySvc, registry, defaultSchema);
		return new HLDToSQLConverterImpl(factorySvc, registry, dbType, sqlgen);
	}
	
	@Override
	public SqlGeneratorFactory createSqlFactory(DBType dbtype, FactoryService factorySvc, DTypeRegistry registry, String defaultSchema) {
		if (DBType.POSTGRES.equals(dbtype)) {
			return new PostgresSqlGeneratorFactory(registry, factorySvc, defaultSchema);
		}
		return new SqlGeneratorFactoryImpl(registry, factorySvc, defaultSchema);
	}

}
