package org.delia.hld;

import org.delia.assoc.DatIdMap;
import org.delia.core.FactoryService;
import org.delia.db.DBType;
import org.delia.sprig.SprigService;
import org.delia.type.DTypeRegistry;

public class HLDFactoryImpl implements HLDFactory {

	@Override
	public HLDBuildService createHLDBuilderService(DTypeRegistry registry, FactoryService factorySvc, DatIdMap datIdMap,
			SprigService sprigSvc, DBType dbType) {
		return new HLDBuildServiceImpl(registry, factorySvc, datIdMap, sprigSvc, dbType);
	}

}
