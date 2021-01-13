package org.delia.db.newhls;

import org.delia.compiler.ast.QueryExp;
import org.delia.core.FactoryService;
import org.delia.db.QueryBuilderService;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;

/**
 * @author ian
 *
 */
public class QueryBuilderHelper {
	private FactoryService factorySvc;
	private DTypeRegistry registry;

	public QueryBuilderHelper(DTypeRegistry registry, FactoryService factorySvc) {
		this.registry = registry;
		this.factorySvc = factorySvc;
	}
	

	public QueryExp buildPKQueryExp(DStructType targetType, DValue fkval) {
		QueryBuilderService queryBuilderSvc = factorySvc.getQueryBuilderService();
		return queryBuilderSvc.createPrimaryKeyQuery(targetType.getName(), fkval);
	}


	public QueryExp createEqQuery(DStructType targetType, String fieldName, DValue pkval) {
		QueryBuilderService queryBuilderSvc = factorySvc.getQueryBuilderService();
		return queryBuilderSvc.createEqQuery(targetType.getName(), fieldName, pkval);
	}

	
}