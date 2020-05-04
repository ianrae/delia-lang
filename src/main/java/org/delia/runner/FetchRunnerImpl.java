package org.delia.runner;

import java.util.Collections;
import java.util.Map;

import org.delia.compiler.ast.QueryExp;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.DBExecutor;
import org.delia.db.QueryBuilderService;
import org.delia.db.QueryContext;
import org.delia.db.QuerySpec;
import org.delia.type.BuiltInTypes;
import org.delia.type.DRelation;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.type.TypePair;
import org.delia.util.DValueHelper;
import org.delia.valuebuilder.RelationValueBuilder;

public class FetchRunnerImpl extends ServiceBase implements FetchRunner {

	private DTypeRegistry registry;
	private VarEvaluator varEvaluator;
	private DBExecutor dbexecutor;

	public FetchRunnerImpl(FactoryService factorySvc, DBExecutor dbexecutor, DTypeRegistry registry, VarEvaluator eval) {
		super(factorySvc);
		this.dbexecutor = dbexecutor;
		this.registry = registry;
		this.varEvaluator = eval;
	}
	
	@Override
	public QueryResponse load(DRelation drel, String targetFieldName) {
		QueryExp queryExp = buildQuery(drel);
		//TODO resolve vars such as foo(id)
		QuerySpec spec = new QuerySpec();
		spec.queryExp = queryExp;
		spec.evaluator = new FilterEvaluator(factorySvc, varEvaluator);
		spec.evaluator.init(spec.queryExp);
		QueryContext qtx = new QueryContext();
		QueryResponse qresp = dbexecutor.executeQuery(spec, qtx);
		return qresp;
	}

	private QueryExp buildQuery(DRelation drel) {
		if (drel.isMultipleKey()) {
			//TODO fix this. hack hack hack it is WRONG
			DType relType = registry.getType(drel.getTypeName());
//			String keyFieldName = DValueHelper.findUniqueField(relType);
//			QueryInExp inExp = new QueryInExp(99, new IdentExp(keyFieldName), null);
//			FilterOpFullExp fullExp = new FilterOpFullExp(99, inExp);
//			for(DValue dval: drel.getMultipleKeys()) {
//				//int only for now
//				//TODO support string,long later
//				Integer foreignKey = dval.asInt();
//				IntegerExp exp = new IntegerExp(foreignKey);
//				inExp.listExp.valueL.add(exp);
//			}
//			FilterExp filter = new FilterExp(99, fullExp); 
//			QueryExp exp = new QueryExp(0, new IdentExp(drel.getTypeName()), filter, null);
			QueryBuilderService builderSvc = factorySvc.getQueryBuilderService();
			QueryExp exp = builderSvc.createInQuery(drel.getTypeName(), drel.getMultipleKeys(), relType);
			return exp;
		} else {
//			Integer foreignKey = drel.getForeignKey().asInt();
//			//TODO string keys later
//			FilterExp filter = new FilterExp(99, new IntegerExp(foreignKey));
			QueryBuilderService builderSvc = factorySvc.getQueryBuilderService();
			QueryExp exp = builderSvc.createPrimaryKeyQuery(drel.getTypeName(), drel.getForeignKey());
			return exp;
		}
	}
	private QueryExp buildOwningTypeQuery(DStructType owningType, String fieldName, DRelation drel) {
//		Integer foreignKey = drel.getForeignKey().asInt();
//		
//		//Address[cust=value]
//		IdentExp op1 = new IdentExp(fieldName);
//		IntegerExp op2 = new IntegerExp(foreignKey);
//		FilterOpExp filterOp0 = new FilterOpExp(99, op1, new StringExp("=="), op2);
//		FilterOpFullExp filterOp = new FilterOpFullExp(99, filterOp0);
//		
//		//TODO string keys later
//		FilterExp filter = new FilterExp(99, filterOp);
//		QueryExp exp = new QueryExp(0, new IdentExp(owningType.getName()), filter, null);
		
		QueryBuilderService builderSvc = factorySvc.getQueryBuilderService();
		QueryExp exp = builderSvc.createEqQuery(owningType.getName(), fieldName, drel.getForeignKey());
		
		return exp;
	}

	@Override
	public QueryResponse queryOwningType(DStructType owningType, String fieldName, DRelation drel) {
		QueryExp queryExp = buildOwningTypeQuery(owningType, fieldName, drel);
		//TODO resolve vars such as foo(id)
		QuerySpec spec = new QuerySpec();
		spec.queryExp = queryExp;
		spec.evaluator = new FilterEvaluator(factorySvc, varEvaluator);
		spec.evaluator.init(spec.queryExp);
		QueryContext qtx = new QueryContext();
		QueryResponse qresp = dbexecutor.executeQuery(spec, qtx);
		return qresp;
	}

	@Override
	public QueryResponse load(String typeName, String fieldName, DValue keyVal) {
		QueryBuilderService builderSvc = factorySvc.getQueryBuilderService();
		QueryExp queryExp = builderSvc.createEqQuery(typeName, fieldName, keyVal);
		
		QuerySpec spec = new QuerySpec();
		spec.queryExp = queryExp;
		spec.evaluator = new FilterEvaluator(factorySvc, varEvaluator);
		spec.evaluator.init(spec.queryExp);
		QueryContext qtx = new QueryContext();
		QueryResponse qresp = dbexecutor.executeQuery(spec, qtx);
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
		
		DValue otherSide = qresp.getOne();
		TypePair pair = DValueHelper.findPrimaryKeyFieldPair(otherSide.getType());
		DValue otherSideKeyVal = otherSide.asStruct().getField(pair.name);
		
		qresp = new QueryResponse();
		qresp.ok = true;
		qresp.dvalList = Collections.singletonList(otherSideKeyVal);
		return qresp;
	}
}
