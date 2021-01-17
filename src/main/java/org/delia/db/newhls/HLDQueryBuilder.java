package org.delia.db.newhls;

import java.util.ArrayList;
import java.util.List;

import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.QueryExp;
import org.delia.compiler.ast.QueryFieldExp;
import org.delia.compiler.ast.QueryFuncExp;
import org.delia.db.newhls.cond.FilterCondBuilder;
import org.delia.db.newhls.cond.FilterFunc;
import org.delia.db.newhls.cond.FilterVal;
import org.delia.db.newhls.cond.FilterValHelper;
import org.delia.relation.RelationInfo;
import org.delia.runner.VarEvaluator;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.TypePair;
import org.delia.util.DRuleHelper;
import org.delia.util.DValueHelper;
import org.delia.util.DeliaExceptionHelper;

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

	public HLDQuery build(QueryExp queryExp, VarEvaluator varEvaluator) {
		return build(queryExp, null, varEvaluator);
	}
	public HLDQuery build(QueryExp queryExp, DStructType structTypeEx, VarEvaluator varEvaluator) {
		HLDQuery hld = new HLDQuery();
		hld.fromType = structTypeEx != null ? structTypeEx : (DStructType) registry.getType(queryExp.typeName);
		hld.mainStructType = hld.fromType; //TODO fix
		hld.resultType = hld.fromType; //TODO fix

		FilterCondBuilder builder = new FilterCondBuilder(registry, hld.fromType, varEvaluator);
		hld.filter = builder.build(queryExp);
		
		List<QScope> scopeL = prepareScopeList(queryExp);
		buildFinalFieldAndThroughChain(queryExp, hld, scopeL); 
		buildFns(queryExp, hld, scopeL);
		hld.scopeL = scopeL;
		hld.originalQueryExp = queryExp; //may be needed later
		return hld;
	}

	private List<QScope> prepareScopeList(QueryExp queryExp) {
		List<QScope> list =  new ArrayList<>();
		int i = 0;
		for(QueryFuncExp qfnexp: queryExp.qfelist) {
			QScope scope = new QScope(qfnexp);
			scope.index = i++;
			list.add(scope);
		}
		return list;
	}

	private void buildFinalFieldAndThroughChain(QueryExp queryExp, HLDQuery hld, List<QScope> scopeL) {
		DStructType currentScope = hld.fromType;
		RelationField currentRF = null;
		List<RelationField> pendingL = new ArrayList<>();
		
		for(QueryFuncExp qfnexp: queryExp.qfelist) {
			QScope scope = scopeL.stream().filter(x -> x.qfnexp == qfnexp).findAny().get();
			if (qfnexp instanceof QueryFieldExp) {
				QueryFieldExp qfe = (QueryFieldExp) qfnexp;
				DType type = DValueHelper.findFieldType(currentScope, qfe.funcName);
				if (type == null) {
					DeliaExceptionHelper.throwError("unknown-field", "Type '%s', field '%s' doesn't exist", currentScope.getName(), qfe.funcName);
				}
				FinalField ff = new FinalField();
				ff.structField = new StructField(currentScope, qfe.funcName, type);
				ff.rf = currentRF;
				hld.finalField = ff;
				if (type.isStructShape()) {
					RelationField rf = new RelationField(currentScope, qfnexp.funcName, (DStructType) type);
					currentRF = rf;
					pendingL.add(rf);
					currentScope = (DStructType) type;
					scope.setDetails(currentScope, rf.fieldName, rf);
				} else {
					//only add to throughchain if we ref a field in it. eg. addr.y
					for(RelationField rf: pendingL) {
//						scope.setDetails(currentScope, rf.fieldName, rf);
						hld.throughChain.add(rf);
					}
					pendingL.clear();
					scope.setDetails(currentScope, ff.structField.fieldName, ff);
				}
			}
		}
	}


	private void buildFns(QueryExp queryExp, HLDQuery hld, List<QScope> scopeL) {
		DStructType currentScope = hld.fromType; //TODO implement scope changes when see .addr
		
		for(QueryFuncExp fnexp: queryExp.qfelist) {
			if (fnexp instanceof QueryFieldExp) {
				continue;
			}
			QScope scope = scopeL.stream().filter(x -> x.qfnexp == fnexp).findAny().get();

			if (fnexp.funcName.equals("fks")) {
				addFKS(currentScope, hld, scope);
			} else if (fnexp.funcName.equals("fetch")) {
				addFetch(fnexp, currentScope, hld, scope);
			} else {
				QueryFnSpec spec = new QueryFnSpec();
				spec.structField = new StructFieldOpt(currentScope, null, null); //?? correct?
				spec.filterFn = new FilterFunc();
				spec.filterFn.fnName = fnexp.funcName;
				addArgs(spec, fnexp);
				hld.funcL.add(spec);
				scope.setDetails(currentScope, fnexp.funcName, spec);
			}
		}
	}

	private void addArgs(QueryFnSpec spec, QueryFuncExp fnexp) {
		for(int i = 0; i < fnexp.argL.size(); i++) {
			Exp exp = fnexp.argL.get(i);
			FilterVal fval = FilterValHelper.createFromExp(exp); //new FilterVal(ValType.STRING, exp);
			spec.filterFn.argL.add(fval);
		}
		
		if (spec.isFn("orderBy")) {
			String fieldName = fnexp.argL.get(0).strValue();
			spec.structField.fieldName = fieldName;
		}		
	}

	private void addFetch(QueryFuncExp fnexp, DStructType currentScope, HLDQuery hld, QScope scope) {
		for(Exp exp: fnexp.argL) {
			String fieldToFetch = exp.strValue();
			FetchSpec spec = new FetchSpec(currentScope, fieldToFetch);
			spec.isFK = false;
			hld.fetchL.add(spec);
			scope.setDetails(currentScope, fieldToFetch, spec);
		}
	}

	private void addFKS(DStructType currentScope, HLDQuery hld, QScope scope) {
		for(TypePair pair: currentScope.getAllFields()) {
			if (pair.type.isStructShape()) {
				RelationInfo relinfo = DRuleHelper.findMatchingRuleInfo(currentScope, pair);
				if (relinfo.notContainsFK()) {
					FetchSpec spec = new FetchSpec(currentScope, pair.name);
					spec.isFK = true;
					hld.fetchL.add(spec);
					scope.setDetails(currentScope, pair.name, spec);
				}
			}
		}
	}
	
	
}