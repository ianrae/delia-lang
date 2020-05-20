package org.delia.zdb.postgres;

import org.delia.core.FactoryService;
import org.delia.type.DTypeRegistry;
import org.delia.zdb.ZInsert;

public class PostgresZInsert extends ZInsert {

	public PostgresZInsert(FactoryService factorySvc, DTypeRegistry registry) {
		super(factorySvc, registry);
	}

}
