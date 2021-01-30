package org.delia.hld;

import org.delia.assoc.DatIdMap;
import org.delia.core.FactoryService;
import org.delia.db.DBType;
import org.delia.hld.cud.HLDToSQLConverter;
import org.delia.hld.cud.HLDToSQLConverterImpl;
import org.delia.sprig.SprigService;
import org.delia.type.DTypeRegistry;

public class HLDFactoryImpl implements HLDFactory {

	@Override
	public HLDBuildService createHLDBuilderService(DTypeRegistry registry, FactoryService factorySvc, DatIdMap datIdMap,
			SprigService sprigSvc, DBType dbType) {
		
		HLDSQLGenerator otherSqlGen = new HLDSQLGenerator(registry, factorySvc, datIdMap);
		HLDToSQLConverter converter = this.createConverter(factorySvc, registry, otherSqlGen, dbType);
		return new HLDBuildServiceImpl(registry, factorySvc, datIdMap, sprigSvc, dbType, converter);
	}

	@Override
	public HLDToSQLConverter createConverter(FactoryService factorySvc, DTypeRegistry registry, HLDSQLGenerator otherSqlGen, DBType dbType) {
		return new HLDToSQLConverterImpl(factorySvc, registry, otherSqlGen, dbType);
	}

}
