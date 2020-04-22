package org.delia.db.postgres;

import org.delia.core.FactoryService;
import org.delia.db.sql.table.FieldGen;
import org.delia.db.sql.table.FieldGenFactory;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.TypePair;

public class PostgresFieldgenFactory extends FieldGenFactory {

	public PostgresFieldgenFactory(FactoryService factorySvc) {
		super(factorySvc);
	}

	@Override
	public FieldGen createFieldGen(DTypeRegistry registry, TypePair pair, DStructType dtype, boolean isAlter) {
		return new PostgresFieldGen(factorySvc, registry, pair, dtype, isAlter);
	}

}
