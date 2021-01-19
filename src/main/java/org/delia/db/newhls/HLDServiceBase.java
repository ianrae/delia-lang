package org.delia.db.newhls;

import org.delia.assoc.DatIdMap;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.newhls.simple.SimpleSqlBuilder;
import org.delia.sprig.SprigService;
import org.delia.type.DTypeRegistry;

/**
 * Base class for most of the HLD generators.
 * Note. these are not services in the sense of being long-lived. Most only
 * exist only during a single delia execution.
 * 
 * @author ian
 *
 */
public abstract class HLDServiceBase extends ServiceBase {
	protected DTypeRegistry registry;
	protected DatIdMap datIdMap;
	protected SprigService sprigSvc;
	private QueryBuilderHelper queryBuilderHelper;
	private SimpleSqlBuilder simpleBuilder;

	public HLDServiceBase(DTypeRegistry registry, FactoryService factorySvc, DatIdMap datIdMap, SprigService sprigSvc) {
		super(factorySvc);
		this.registry = registry;
		this.factorySvc = factorySvc;
		this.datIdMap = datIdMap;
		this.sprigSvc = sprigSvc;
	}

	public QueryBuilderHelper getQueryBuilderHelper() {
		if (queryBuilderHelper == null) {
			this.queryBuilderHelper = new QueryBuilderHelper(registry, factorySvc);
		}
		return queryBuilderHelper;
	}

	public SimpleSqlBuilder getSimpleBuilder() {
		if (simpleBuilder == null) {
			this.simpleBuilder = new SimpleSqlBuilder();
		}
		return simpleBuilder;
	}
}