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
import org.delia.compiler.ast.NumberExp;
import org.delia.compiler.ast.QueryExp;
import org.delia.compiler.ast.QueryFuncExp;
import org.delia.compiler.ast.QueryInExp;
import org.delia.compiler.ast.StringExp;
import org.delia.core.DateFormatService;
import org.delia.core.FactoryService;
import org.delia.runner.FilterEvaluator;
import org.delia.runner.VarEvaluator;
import org.delia.type.DType;
import org.delia.type.DValue;
import org.delia.util.DValueHelper;
import org.delia.util.DeliaExceptionHelper;

public class QueryBuilderServiceImpl implements QueryBuilderService {
	
	private FactoryService factorySvc;
	private DateFormatService fmtSvc;

	public QueryBuilderServiceImpl(FactoryService factorySvc) {
		this.factorySvc = factorySvc;
		this.fmtSvc = factorySvc.getDateFormatService();
	}

	@Override
	public QueryExp createEqQuery(String typeName, String fieldName, DValue targetValue) {
		
		//Address[cust=value]
		IdentExp op1 = new IdentExp(fieldName);
		Exp op2 = createExpFor(targetValue);
		FilterOpExp filterOp0 = new FilterOpExp(99, op1, new StringExp("=="), op2);
		FilterOpFullExp filterOp = new FilterOpFullExp(99, filterOp0);
		
		FilterExp filter = new FilterExp(99, filterOp);
		QueryExp exp = new QueryExp(0, new IdentExp(typeName), filter, null);
		return exp;
	}
	
	private Exp createExpFor(DValue inner) {
		switch(inner.getType().getShape()) {
		case INTEGER:
			return new IntegerExp(inner.asInt());
		case LONG:
			return new LongExp(inner.asLong());
		case NUMBER:
			return new NumberExp(inner.asNumber());
		case STRING:
			return new StringExp(inner.asString());
		case BOOLEAN:
			return new BooleanExp(inner.asBoolean());
		case DATE:
		{
			String s = fmtSvc.format(inner.asDate());
			return new StringExp(s);
		}
		default:
			//err
			return null;
		}
	}

	@Override
	public QueryExp createPrimaryKeyQuery(String typeName, DValue keyValue) {
		switch(keyValue.getType().getShape()) {
		case INTEGER:
		{
			Integer foreignKey = keyValue.asInt();
			//TODO string keys later
			FilterExp filter = new FilterExp(99, new IntegerExp(foreignKey));
			QueryExp exp = new QueryExp(0, new IdentExp(typeName), filter, null);
			return exp;
		}			
		case LONG:
		{
			Long foreignKey = keyValue.asLong();
			//TODO string keys later
			FilterExp filter = new FilterExp(99, new LongExp(foreignKey));
			QueryExp exp = new QueryExp(0, new IdentExp(typeName), filter, null);
			return exp;
		}			
		case STRING:
		{
			String foreignKey = keyValue.asString();
			//TODO string keys later
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
		//TODO fix this. hack hack hack it is WRONG
		String keyFieldName = DValueHelper.findUniqueField(relType);
		QueryInExp inExp = new QueryInExp(99, new IdentExp(keyFieldName), null);
		FilterOpFullExp fullExp = new FilterOpFullExp(99, inExp);
		for(DValue dval: list) {
			//int only for now
			//TODO support string,long later
			Integer foreignKey = dval.asInt();
			IntegerExp exp = new IntegerExp(foreignKey);
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

}
