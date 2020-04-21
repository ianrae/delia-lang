package org.delia.db.sql.table;

import java.util.List;

import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.sql.StrCreator;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.TypePair;

public abstract class SqlElement extends ServiceBase {
	protected DTypeRegistry registry;
	protected TypePair pair;
	protected DStructType dtype;

	public SqlElement(FactoryService factorySvc, DTypeRegistry registry, TypePair pair, DStructType dtype) {
		super(factorySvc);
		this.registry = registry;
		this.pair = pair;
		this.dtype = dtype;
	}
	
	public abstract void generateField(StrCreator sc);

}