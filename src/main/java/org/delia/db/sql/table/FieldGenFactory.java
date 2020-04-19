package org.delia.db.sql.table;

import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.TypePair;

public class FieldGenFactory extends ServiceBase {

	public FieldGenFactory(FactoryService factorySvc) {
		super(factorySvc);
	}

	public FieldGen createFieldGen(DTypeRegistry registry, TypePair pair, DStructType dtype) {
		return new FieldGen(factorySvc, registry, pair, dtype);
	}
	
	public ConstraintGen generateFKConstraint(DTypeRegistry registry, TypePair pair, DStructType dtype) {
		return new ConstraintGen(factorySvc, registry, pair, dtype);
	}

}
