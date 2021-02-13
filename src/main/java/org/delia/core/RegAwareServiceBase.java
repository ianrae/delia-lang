package org.delia.core;

import org.delia.type.DTypeRegistry;

public class RegAwareServiceBase extends ServiceBase {
	protected DTypeRegistry registry;

	public RegAwareServiceBase(DTypeRegistry registry, FactoryService factorySvc) {
		super(factorySvc);
		this.registry = registry;
	}
}