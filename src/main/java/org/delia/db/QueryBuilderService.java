package org.delia.db;

import java.util.List;

import org.delia.compiler.ast.QueryExp;
import org.delia.runner.VarEvaluator;
import org.delia.type.DType;
import org.delia.type.DValue;

public interface QueryBuilderService {
	QueryExp createEqQuery(String typeName, String fieldName, DValue targetValue);
	QueryExp createNotEqQuery(String typeName, String fieldName, DValue targetValue);
	QueryExp createPrimaryKeyQuery(String typeName, DValue keyValue);
	QueryExp createInQuery(String typeName, List<DValue> list, DType relType);
	QueryExp createCountQuery(String typeName);
	QueryExp createAllRowsQuery(String typeName);
	QueryExp createAndQuery(String typeName, QueryExp exp1, QueryExp exp2);
	
	QuerySpec buildSpec(QueryExp exp, VarEvaluator varEvaluator);
}
