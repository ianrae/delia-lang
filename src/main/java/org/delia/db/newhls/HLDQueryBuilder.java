package org.delia.db.newhls;

import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.QueryExp;
import org.delia.compiler.ast.QueryFieldExp;
import org.delia.compiler.ast.QueryFuncExp;
import org.delia.db.newhls.cond.FilterCondBuilder;
import org.delia.db.newhls.cond.FilterFunc;
import org.delia.relation.RelationCardinality;
import org.delia.relation.RelationInfo;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.TypePair;
import org.delia.util.DRuleHelper;
import org.delia.util.DValueHelper;

/**
 * Creates HLDQuery from a QueryExp
 * @author ian
 *
 */
public class HLDQueryBuilder {
	private DTypeRegistry registry;

	public HLDQueryBuilder(DTypeRegistry registry) {
		this.registry = registry;
	}

	public HLDQuery build(QueryExp queryExp) {
		HLDQuery hld = new HLDQuery();
		hld.fromType = (DStructType) registry.getType(queryExp.typeName);
		hld.mainStructType = hld.fromType; //TODO fix
		hld.resultType = hld.fromType; //TODO fix

		FilterCondBuilder builder = new FilterCondBuilder(registry, hld.fromType);
		hld.filter = builder.build(queryExp);
		buildThroughChain(queryExp, hld);//			public List<StructField> throughChain = new ArrayList<>();
		buildFinalField(queryExp, hld); //			public StructField finalField; //eg Customer.addr
		//			public List<FetchSpec> fetchL = new ArrayList<>(); //order matters: eg. .addr.fetch('country')
		//			public List<QueryFnSpec> funcL = new ArrayList<>(); //list and calc fns. order matters: eg. .addr.first().city
		buildFns(queryExp, hld);
		return hld;
	}

	private void buildThroughChain(QueryExp queryExp, HLDQuery hld) {
		// TODO Auto-generated method stub
		
	}
	private void buildFinalField(QueryExp queryExp, HLDQuery hld) {
		DStructType currentScope = hld.fromType;
		for(QueryFuncExp qfnexp: queryExp.qfelist) {
			if (qfnexp instanceof QueryFieldExp) {
				QueryFieldExp qfe = (QueryFieldExp) qfnexp;
				DType type = DValueHelper.findFieldType(currentScope, qfe.funcName);
				StructField sf = new StructField(currentScope, qfe.funcName, type);
				hld.finalField = sf;
			}
		}
	}


	private void buildFns(QueryExp queryExp, HLDQuery hld) {
		DStructType currentScope = hld.fromType; //TODO implement scope changes when see .addr
		
		for(QueryFuncExp fnexp: queryExp.qfelist) {
			if (fnexp instanceof QueryFieldExp) {
				continue;
			}
			
			if (fnexp.funcName.equals("fks")) {
				addFKS(currentScope, hld);
			} else if (fnexp.funcName.equals("fetch")) {
				addFetch(fnexp, currentScope, hld);
			} else {
				QueryFnSpec spec = new QueryFnSpec();
				spec.structField = new StructFieldOpt(currentScope, null, null); //?? correct?
				spec.filterFn = new FilterFunc();
				spec.filterFn.fnName = fnexp.funcName;
				addArgs(spec, fnexp);
				hld.funcL.add(spec);
			}
		}
	}

	private void addArgs(QueryFnSpec spec, QueryFuncExp fnexp) {
		if (spec.isFn("orderBy")) {
			String fieldName = fnexp.argL.get(0).strValue();
			spec.structField.fieldName = fieldName;
		}
		// TODO add more later
		
	}

	private void addFetch(QueryFuncExp fnexp, DStructType currentScope, HLDQuery hld) {
		for(Exp exp: fnexp.argL) {
			String fieldToFetch = exp.strValue();
			FetchSpec spec = new FetchSpec(currentScope, fieldToFetch);
			spec.isFK = false;
			hld.fetchL.add(spec);
		}
	}

	private void addFKS(DStructType currentScope, HLDQuery hld) {
		for(TypePair pair: currentScope.getAllFields()) {
			if (pair.type.isStructShape()) {
				RelationInfo relinfo = DRuleHelper.findMatchingRuleInfo(currentScope, pair);
				if (RelationCardinality.MANY_TO_MANY.equals(relinfo.cardinality)) {
					//TODO: doManyToManyAddFKofJoins(fieldL, pair, relinfo, null, hld);
				} else if (relinfo.isParent) {
					FetchSpec spec = new FetchSpec(currentScope, pair.name);
					spec.isFK = true;
					hld.fetchL.add(spec);
				}
			}
		}
	}
	
	
}