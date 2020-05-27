package org.delia.runner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.delia.compiler.ast.QueryExp;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.QueryBuilderService;
import org.delia.db.QueryContext;
import org.delia.db.QuerySpec;
import org.delia.type.DRelation;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.type.TypePair;
import org.delia.util.DValueHelper;
import org.delia.zdb.ZDBExecutor;

public class ZFetchRunnerImpl extends ServiceBase implements FetchRunner {

	private DTypeRegistry registry;
	private VarEvaluator varEvaluator;
	private ZDBExecutor dbexecutor;

	public ZFetchRunnerImpl(FactoryService factorySvc, ZDBExecutor dbexecutor, DTypeRegistry registry, VarEvaluator eval) {
		super(factorySvc);
		this.dbexecutor = dbexecutor;
		this.registry = registry;
		this.varEvaluator = eval;
	}
	
	@Override
	public QueryResponse load(DRelation drel) {
		QueryExp queryExp = buildQuery(drel);
		//TODO resolve vars such as foo(id)
		QuerySpec spec = new QuerySpec();
		spec.queryExp = queryExp;
		spec.evaluator = new FilterEvaluator(factorySvc, varEvaluator);
		spec.evaluator.init(spec.queryExp);
		QueryContext qtx = new QueryContext();
		QueryResponse qresp = dbexecutor.rawQuery(spec, qtx);
		return qresp;
	}

	@Override
	public boolean queryFKExists(DRelation drel) {
		QueryResponse qresp = load(drel);
		if (!qresp.ok) {
			return false;
		} else if (CollectionUtils.isEmpty(qresp.dvalList)) {
			return false;
		} else {
			return true;
		}
	}

	private QueryExp buildQuery(DRelation drel) {
		if (drel.isMultipleKey()) {
			DType relType = registry.getType(drel.getTypeName());
			QueryBuilderService builderSvc = factorySvc.getQueryBuilderService();
			QueryExp exp = builderSvc.createInQuery(drel.getTypeName(), drel.getMultipleKeys(), relType);
			return exp;
		} else {
			QueryBuilderService builderSvc = factorySvc.getQueryBuilderService();
			QueryExp exp = builderSvc.createPrimaryKeyQuery(drel.getTypeName(), drel.getForeignKey());
			return exp;
		}
	}
	private QueryExp buildOwningTypeQuery(DStructType owningType, String fieldName, DRelation drel) {
		QueryBuilderService builderSvc = factorySvc.getQueryBuilderService();
		//query Address[cust==1]
		QueryExp exp = builderSvc.createEqQuery(owningType.getName(), fieldName, drel.getForeignKey());
		
		return exp;
	}

	@Override
	public boolean queryFKExists(DStructType owningType, String fieldName, DRelation drel) {
		List<DValue> dvalList = queryFKs(owningType, fieldName, drel);
		return !CollectionUtils.isEmpty(dvalList);
	}

	@Override
	public List<DValue> queryFKs(DStructType owningType, String fieldName, DRelation drel) {
		QueryExp queryExp = buildOwningTypeQuery(owningType, fieldName, drel);
		//TODO resolve vars such as foo(id)
		QuerySpec spec = new QuerySpec();
		spec.queryExp = queryExp;
		spec.evaluator = new FilterEvaluator(factorySvc, varEvaluator);
		spec.evaluator.init(spec.queryExp);
		QueryContext qtx = new QueryContext();
		QueryResponse qresp = dbexecutor.rawQuery(spec, qtx);
		
		if (!qresp.ok) {
			return Collections.emptyList();
		} else {
			return qresp.dvalList;
		}
	}

	private QueryResponse load(String typeName, String fieldName, DValue keyVal) {
		QueryBuilderService builderSvc = factorySvc.getQueryBuilderService();
		QueryExp queryExp = builderSvc.createEqQuery(typeName, fieldName, keyVal);
		
		QuerySpec spec = new QuerySpec();
		spec.queryExp = queryExp;
		spec.evaluator = new FilterEvaluator(factorySvc, varEvaluator);
		spec.evaluator.init(spec.queryExp);
		QueryContext qtx = new QueryContext();
		QueryResponse qresp = dbexecutor.rawQuery(spec, qtx);
		return qresp;
	}

	@Override
	public QueryResponse loadFKOnly(String typeName, String fieldName, DValue keyVal) {
		QueryResponse qresp = load(typeName, fieldName, keyVal);
		if (!qresp.ok) {
			return qresp; //!!
		}
		if (qresp.emptyResults()) {
			return qresp;
		}
		
		List<DValue> newList = new ArrayList<>();
		TypePair pair = null;
		for(DValue otherSide: qresp.dvalList) {
			if (pair == null) {
				pair = DValueHelper.findPrimaryKeyFieldPair(otherSide.getType());
			}
			DValue otherSideKeyVal = otherSide.asStruct().getField(pair.name);
			newList.add(otherSideKeyVal);
		}
		
		qresp = new QueryResponse();
		qresp.ok = true;
		qresp.dvalList = newList;
		return qresp;
	}
}
