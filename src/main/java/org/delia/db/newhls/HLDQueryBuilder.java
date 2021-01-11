package org.delia.db.newhls;

import java.util.ArrayList;
import java.util.List;

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
		return build(queryExp, null);
	}
	public HLDQuery build(QueryExp queryExp, DStructType structTypeEx) {
		HLDQuery hld = new HLDQuery();
		hld.fromType = structTypeEx != null ? structTypeEx : (DStructType) registry.getType(queryExp.typeName);
		hld.mainStructType = hld.fromType; //TODO fix
		hld.resultType = hld.fromType; //TODO fix

		FilterCondBuilder builder = new FilterCondBuilder(registry, hld.fromType);
		hld.filter = builder.build(queryExp);
		buildFinalFieldAndThroughChain(queryExp, hld); 
		buildFns(queryExp, hld);
		return hld;
	}

	private void buildFinalFieldAndThroughChain(QueryExp queryExp, HLDQuery hld) {
		DStructType currentScope = hld.fromType;
		RelationField currentRF = null;
		List<RelationField> pendingL = new ArrayList<>();
		
		for(QueryFuncExp qfnexp: queryExp.qfelist) {
			if (qfnexp instanceof QueryFieldExp) {
				QueryFieldExp qfe = (QueryFieldExp) qfnexp;
				DType type = DValueHelper.findFieldType(currentScope, qfe.funcName);
				FinalField ff = new FinalField();
				ff.structField = new StructField(currentScope, qfe.funcName, type);
				ff.rf = currentRF;
				hld.finalField = ff;
				if (type.isStructShape()) {
					RelationField rf = new RelationField(currentScope, qfnexp.funcName, (DStructType) type);
					currentRF = rf;
					pendingL.add(rf);
					currentScope = (DStructType) type;
				} else {
					//only add to throughchain if we ref a field in it. eg. addr.y
					for(RelationField rf: pendingL) {
						hld.throughChain.add(rf);
					}
					pendingL.clear();
				}
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
				if (relinfo.notContainsFK()) {
					FetchSpec spec = new FetchSpec(currentScope, pair.name);
					spec.isFK = true;
					hld.fetchL.add(spec);
				}
			}
		}
	}
	
	
}