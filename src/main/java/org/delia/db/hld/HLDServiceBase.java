package org.delia.db.hld;

import org.delia.assoc.DatIdMap;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.hld.cond.FilterVal;
import org.delia.db.hld.cond.OpFilterCond;
import org.delia.db.hld.cond.SingleFilterCond;
import org.delia.db.hld.simple.SimpleSqlBuilder;
import org.delia.sprig.SprigService;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.type.TypePair;
import org.delia.util.DValueHelper;

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
		} else if (hld.filter instanceof OpFilterCond) {
			OpFilterCond ofc = (OpFilterCond) hld.filter;
			FilterVal fval = findForPK(hld.fromType, ofc);
			if (fval == null) {
				return null;
			}
			if (ofc.val1.isScalar()) {
				DValue pkval = pgen.convert(ofc.val1);
				return pkval;
			} else if (ofc.val2.isScalar()) {
				DValue pkval = pgen.convert(ofc.val2);
				return pkval;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	private FilterVal findForPK(DStructType fromType, OpFilterCond ofc) {
		TypePair pkpair = DValueHelper.findPrimaryKeyFieldPair(fromType);
		if (pkpair != null) {
			if (ofc.val1.isSymbol() && ofc.val1.asString().equals(pkpair.name)) {
				return ofc.val1;
			}
			if (ofc.val2.isSymbol() && ofc.val2.asString().equals(pkpair.name)) {
				return ofc.val2;
			}
		}
		return null;
	}
}