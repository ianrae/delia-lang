package org.delia.db.newhls;

import org.delia.assoc.DatIdMap;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.newhls.cond.SingleFilterCond;
import org.delia.db.newhls.simple.SimpleSqlBuilder;
import org.delia.sprig.SprigService;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;

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

	protected QueryBuilderHelper getQueryBuilderHelper() {
		if (queryBuilderHelper == null) {
			this.queryBuilderHelper = new QueryBuilderHelper(registry, factorySvc);
		}
		return queryBuilderHelper;
	}

	protected SimpleSqlBuilder getSimpleBuilder() {
		if (simpleBuilder == null) {
			this.simpleBuilder = new SimpleSqlBuilder();
		}
		return simpleBuilder;
	}
	
	protected DValue getUpdatePK(HLDQuery hld) {
		//Note. the dson body of update doesn't have pk, so we need to get it from the filter
		SqlParamGenerator pgen = new SqlParamGenerator(registry, factorySvc);
		if (hld.filter instanceof SingleFilterCond) {
			SingleFilterCond sfc = (SingleFilterCond) hld.filter;
			DValue pkval = pgen.convert(sfc.val1);
			return pkval;
		} else {
			return null;
		}
	}
}