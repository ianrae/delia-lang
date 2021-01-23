package org.delia.hld;

import org.delia.assoc.DatIdMap;
import org.delia.core.FactoryService;
import org.delia.hld.cud.HLDDsonBuilder;
import org.delia.runner.VarEvaluator;
import org.delia.sprig.SprigService;
import org.delia.type.DTypeRegistry;

public class HLDExtendedServiceBase extends HLDServiceBase {
	private HLDDsonBuilder hldBuilder;
	protected VarEvaluator varEvaluator; //set after ctor
	
	public HLDExtendedServiceBase(DTypeRegistry registry, FactoryService factorySvc, DatIdMap datIdMap,
			SprigService sprigSvc) {
		super(registry, factorySvc, datIdMap, sprigSvc);

	}
	public VarEvaluator getVarEvaluator() {
		return varEvaluator;
	}

	public void setVarEvaluator(VarEvaluator varEvaluator) {
		this.varEvaluator = varEvaluator;
	}
	public HLDDsonBuilder getHldBuilder() {
		if (hldBuilder == null) {
			//TODO: how do we know if varEvaluator set, or even needed??
			this.hldBuilder = new HLDDsonBuilder(registry, factorySvc, sprigSvc, varEvaluator);
		}
		return hldBuilder;
	}

}
