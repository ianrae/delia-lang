package org.delia.db;

import java.util.List;

import org.delia.compiler.ast.BooleanExp;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.FilterExp;
import org.delia.compiler.ast.FilterOpExp;
import org.delia.compiler.ast.FilterOpFullExp;
import org.delia.compiler.ast.IdentExp;
import org.delia.compiler.ast.IntegerExp;
import org.delia.compiler.ast.LongExp;
import org.delia.compiler.ast.QueryExp;
import org.delia.compiler.ast.QueryFuncExp;
import org.delia.compiler.ast.QueryInExp;
import org.delia.compiler.ast.StringExp;
import org.delia.compiler.astx.XNAFMultiExp;
import org.delia.compiler.astx.XNAFNameExp;
import org.delia.core.FactoryService;
import org.delia.dval.DValueConverterService;
import org.delia.runner.FilterEvaluator;
import org.delia.runner.VarEvaluator;
import org.delia.type.DType;
import org.delia.type.DValue;
import org.delia.type.TypePair;
import org.delia.util.DValueHelper;
import org.delia.util.DeliaExceptionHelper;

public class QueryBuilderServiceImpl implements QueryBuilderService {
	
	private FactoryService factorySvc;
	private DValueConverterService dvalConverter;

	public QueryBuilderServiceImpl(FactoryService factorySvc) {
		this.factorySvc = factorySvc;
		this.dvalConverter = new DValueConverterService(factorySvc);
	}

	@Override
	public QueryExp createEqQuery(String typeName, String fieldName, DValue targetValue) {
		
		//Address[cust=value]
		XNAFMultiExp op1 = buildXNAFExp(fieldName);
		Exp op2 = createExpFor(targetValue);
		FilterOpExp filterOp0 = new FilterOpExp(99, op1, new StringExp("=="), op2);
		FilterOpFullExp filterOp = new FilterOpFullExp(99, filterOp0);
		
		FilterExp filter = new FilterExp(99, filterOp);
		QueryExp exp = new QueryExp(0, new IdentExp(typeName), filter, null);
		return exp;
	}
	@Override
	public QueryExp createNotEqQuery(String typeName, String fieldName, DValue targetValue) {
		
		//Address[cust=value]
		XNAFMultiExp op1 = buildXNAFExp(fieldName);
		Exp op2 = createExpFor(targetValue);
		FilterOpExp filterOp0 = new FilterOpExp(99, op1, new StringExp("!="), op2);
		FilterOpFullExp filterOp = new FilterOpFullExp(99, filterOp0);
		
		FilterExp filter = new FilterExp(99, filterOp);
		QueryExp exp = new QueryExp(0, new IdentExp(typeName), filter, null);
		return exp;
	}
	
	private XNAFMultiExp buildXNAFExp(String fieldName) {
		XNAFMultiExp exp = new XNAFMultiExp(99, false, null);
		XNAFNameExp nameExp = new XNAFNameExp(99, new IdentExp(fieldName));
		exp.qfeL.add(nameExp);
		return exp;
	}

	private Exp createExpFor(DValue inner) {
		return dvalConverter.createExpFor(inner);
	}

	@Override
	public QueryExp createPrimaryKeyQuery(String typeName, DValue keyValue) {
		switch(keyValue.getType().getShape()) {
		case INTEGER:
		{
			Integer foreignKey = keyValue.asInt();
			FilterExp filter = new FilterExp(99, new IntegerExp(foreignKey));
			QueryExp exp = new QueryExp(0, new IdentExp(typeName), filter, null);
			return exp;
		}			
		case LONG:
		{
			Long foreignKey = keyValue.asLong();
			FilterExp filter = new FilterExp(99, new LongExp(foreignKey));
			QueryExp exp = new QueryExp(0, new IdentExp(typeName), filter, null);
			return exp;
		}			
		case STRING:
		{
			String foreignKey = keyValue.asString();
			FilterExp filter = new FilterExp(99, new StringExp(foreignKey));
			QueryExp exp = new QueryExp(0, new IdentExp(typeName), filter, null);
			return exp;
		}			
		default:
			DeliaExceptionHelper.throwError("cant-build-primary-key-query", "createPrimaryKeyQuery on shape %s", keyValue.getType().getShape().name());
			return null;
		}
	}

	@Override
	public QueryExp createInQuery(String typeName, List<DValue> list, DType relType) {
		TypePair keyPair = DValueHelper.findPrimaryKeyFieldPair(relType);
		QueryInExp inExp = new QueryInExp(99, new IdentExp(keyPair.name), null);
		FilterOpFullExp fullExp = new FilterOpFullExp(99, inExp);
		for(DValue dval: list) {
			Exp exp = dvalConverter.createExpFor(dval);
			inExp.listExp.valueL.add(exp);
		}
		FilterExp filter = new FilterExp(99, fullExp); 
		QueryExp exp = new QueryExp(0, new IdentExp(typeName), filter, null);
		return exp;
	}

	@Override
	public QueryExp createCountQuery(String typeName) {
		FilterExp filter = new FilterExp(99, new BooleanExp(true));
		QueryExp exp = new QueryExp(0, new IdentExp(typeName), filter, null);
		QueryFuncExp qfe = new QueryFuncExp(99, new IdentExp("count"), null, false);
		exp.qfelist.add(qfe);
		return exp;
	}

	@Override
	public QuerySpec buildSpec(QueryExp queryExp, VarEvaluator varEvaluator) {
		QuerySpec spec = new QuerySpec();
		spec.queryExp = queryExp;
		spec.evaluator = new FilterEvaluator(factorySvc, varEvaluator);
		spec.evaluator.init(spec.queryExp);
		return spec;
	}

	@Override
	public QueryExp createAllRowsQuery(String typeName) {
		FilterExp filter = new FilterExp(99, new BooleanExp(true));
		QueryExp exp = new QueryExp(0, new IdentExp(typeName), filter, null);
		return exp;
	}

	@Override
	public QueryExp createAndQuery(String typeName, QueryExp exp1, QueryExp exp2) {
		FilterOpFullExp andExp = new FilterOpFullExp(0, false, exp1.filter, true, exp2.filter);
		QueryExp exp3 = new QueryExp(0, new IdentExp(typeName), new FilterExp(0, andExp), null);
		return exp3;
	}

}
