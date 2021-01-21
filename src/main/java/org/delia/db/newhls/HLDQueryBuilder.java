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
import org.delia.type.BuiltInTypes;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
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
	
	public boolean canBuildStatement(QueryExp queryExp, DStructType structTypeEx, VarEvaluator varEvaluator) {
		HLDQuery hld = new HLDQuery();
		hld.fromType = structTypeEx != null ? structTypeEx : (DStructType) registry.getType(queryExp.typeName);
		if (hld.fromType == null) {
			List<DValue> referencedVarValList = varEvaluator.lookupVar(queryExp.typeName);
			DValue dval = referencedVarValList == null ? null : (referencedVarValList.isEmpty() ? null : referencedVarValList.get(0)); 
			if (dval == null) { //was not a var ref
				DeliaExceptionHelper.throwUnknownTypeError(queryExp.typeName);
			}
			
			if (! dval.getType().isStructShape()) {
				return false;
			}
		}
		return true;
	}

	public HLDQuery build(QueryExp queryExp, VarEvaluator varEvaluator) {
		return build(queryExp, null, varEvaluator);
	}
	public HLDQuery build(QueryExp queryExp, DStructType structTypeEx, VarEvaluator varEvaluator) {
		HLDQuery hld = new HLDQuery();
		hld.fromType = structTypeEx != null ? structTypeEx : (DStructType) registry.getType(queryExp.typeName);
		if (hld.fromType == null) {
			List<DValue> referencedVarValList = varEvaluator.lookupVar(queryExp.typeName);
			DValue dval = referencedVarValList == null ? null : (referencedVarValList.isEmpty() ? null : referencedVarValList.get(0)); 
			if (dval == null) { //was not a var ref
				DeliaExceptionHelper.throwUnknownTypeError(queryExp.typeName);
			}
			
			if (! dval.getType().isStructShape()) {
				DeliaExceptionHelper.throwError("unexpected-scalar-value", "%s: expected struct but got '%s'", queryExp.typeName, dval.getType().getName());
			}
			
			hld.fromType = (DStructType) dval.getType();
			hld.isVarRef = true;
		}
		hld.mainStructType = hld.fromType; 
		hld.resultType = hld.fromType; //set at end in setResultAndMainType

		FilterCondBuilder builder = new FilterCondBuilder(registry, hld.fromType, varEvaluator);
		hld.filter = builder.build(queryExp, hld.isVarRef);
		
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
					DeliaExceptionHelper.throwUnknownFieldError(currentScope.getName(), qfe.funcName);
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
		DStructType currentScope = hld.fromType; 
		String currentFieldName = null;
		DType currentFieldType = null;
		
		for(QueryFuncExp fnexp: queryExp.qfelist) {
			if (fnexp instanceof QueryFieldExp) {
				TypePair pair = DValueHelper.findField(currentScope, fnexp.funcName);
				if (pair != null) {
					if (pair.type.isStructShape()) {
						currentScope = (DStructType) pair.type;
					} else {
						currentFieldType = pair.type;
					}
					currentFieldName = pair.name;
				} else {
					DeliaExceptionHelper.throwUnknownFieldError(currentScope.getName(), fnexp.funcName);
				}
				continue;
			}
			QScope scope = scopeL.stream().filter(x -> x.qfnexp == fnexp).findAny().get();

			if (fnexp.funcName.equals("fks")) {
				addFKS(currentScope, hld, scope);
			} else if (fnexp.funcName.equals("fetch")) {
				addFetch(fnexp, currentScope, hld, scope);
			} else {
				QueryFnSpec spec = new QueryFnSpec();
				spec.structField = new StructFieldOpt(currentScope, currentFieldName, currentFieldType); //?? correct?
				spec.filterFn = new FilterFunc();
				spec.filterFn.fnName = fnexp.funcName;
				
				addArgs(spec, fnexp, currentScope);
				hld.funcL.add(spec);
				scope.setDetails(currentScope, fnexp.funcName, spec);
			}
		}
	}

	private void addArgs(QueryFnSpec spec, QueryFuncExp fnexp, DStructType currentScope) {
		for(int i = 0; i < fnexp.argL.size(); i++) {
			Exp exp = fnexp.argL.get(i);
			
			//fns that take fieldName as arg: orderByp 
			//fns that don't: count, exists, min, max, distinct, orderBy, limit, offset, first,last,ith
			if (spec.isFn("orderBy") && i == 0) {
				TypePair pair = DValueHelper.findField(currentScope, exp.strValue());
				if (pair == null) {
					DeliaExceptionHelper.throwUnknownFieldError(currentScope.getName(), exp.strValue());
				}
			}
			
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

	//done at the end, once we have the fields
	public void setResultAndMainType(HLDQuery hld) {
		DType currentType = hld.fromType;
		
		for(QScope scope: hld.scopeL) {
			if (scope.thing instanceof FetchSpec) {
				
			} else if (scope.thing instanceof RelationField) {
				RelationField rf = (RelationField) scope.thing;
				currentType = rf.fieldType;
			} else if (scope.thing instanceof QueryFnSpec) {
				//fns that take type of field: min, max
				//fns that change type: count, exists
				//fns that do nothing: distinc, orderBy, limit, offset, first,last,ith
				QueryFnSpec qfn = (QueryFnSpec) scope.thing;
				if (qfn.isFn("min") || qfn.isFn("max")) {
					if (qfn.structField.fieldType == null) {
						TypePair pair = DValueHelper.findField(qfn.structField.dtype, qfn.structField.fieldName);
						if (pair != null) {
							qfn.structField.fieldType = pair.type;
							currentType = pair.type;
						}
					}
				} else if (qfn.isFn("exists")) {
					currentType = registry.getType(BuiltInTypes.BOOLEAN_SHAPE);
				} else if (qfn.isFn("count")) {
					currentType = registry.getType(BuiltInTypes.LONG_SHAPE);
				}
			} else if (scope.thing instanceof FinalField) {
				FinalField ff = (FinalField) scope.thing;
				currentType = ff.structField.fieldType;
			}
		}
		hld.resultType = currentType;
	}


	
}