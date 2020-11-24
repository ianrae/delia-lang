package org.delia.db.hls.manager;

import org.delia.assoc.DatIdMap;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.FilterOpFullExp;
import org.delia.compiler.ast.IdentExp;
import org.delia.compiler.ast.QueryExp;
import org.delia.compiler.ast.QueryInExp;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.hls.HLSQuerySpan;
import org.delia.db.hls.HLSQueryStatement;
import org.delia.relation.RelationInfo;
import org.delia.type.DStructType;
import org.delia.type.TypePair;
import org.delia.util.DRuleHelper;
import org.delia.util.DValueHelper;

public class InQueryStep extends ServiceBase implements HLSPipelineStep {

	public InQueryStep(FactoryService factorySvc) {
		super(factorySvc);
	}

	@Override
	public HLSQueryStatement execute(HLSQueryStatement hls, QueryExp queryExp, DatIdMap datIdMap) {
		
		for(int i = 0; i < hls.hlspanL.size(); i++) {
			HLSQuerySpan hlspan = hls.hlspanL.get(i);	
			HLSQuerySpan newspan = processSpan(hlspan, datIdMap);
			if (newspan != null) {
				hls.hlspanL.set(i, newspan);
			}
		}
		return hls;
	}
	
	private HLSQuerySpan processSpan(HLSQuerySpan hlspan, DatIdMap datIdMap) {
		if (hlspan.filEl == null) {
			return null;
		}
		QueryExp queryExp = hlspan.filEl.queryExp;
		if (queryExp.filter != null && queryExp.filter.cond instanceof FilterOpFullExp) {
			FilterOpFullExp fexp = (FilterOpFullExp) queryExp.filter.cond;
			if (fexp.opexp1 instanceof QueryInExp) {
				DStructType structType = hlspan.fromType;
				QueryInExp inexp = (QueryInExp) fexp.opexp1;
				for(Exp exp: inexp.listExp.valueL) {
					if (exp instanceof IdentExp) {
						TypePair pair = DValueHelper.findField(structType, exp.strValue());
						if (pair != null) {
							RelationInfo info = DRuleHelper.findMatchingRuleInfo(structType, pair);
							if (info != null) {
//								OpFragment opFrag = new OpFragment("=");
//								opFrag.left = FragmentHelper.buildAliasedFrag(null, op1);
							}
						}
					}
				}
			}
		}
		
		return null;
	}
}
