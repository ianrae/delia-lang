package org.delia.db.newhls;

import org.delia.assoc.DatIdMap;
import org.delia.compiler.ast.QueryExp;
import org.delia.core.FactoryService;
import org.delia.db.QueryBuilderService;
import org.delia.relation.RelationInfo;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.type.TypePair;
import org.delia.util.DValueHelper;
import org.delia.util.DeliaExceptionHelper;
import org.delia.valuebuilder.ScalarValueBuilder;

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

	public DValue buildFakeValue(RelationInfo relinfo, DatIdMap datIdMap) {
		DStructType entityType = datIdMap.isFlipped(relinfo) ? relinfo.farType : relinfo.nearType;
		return buildFakeValue(entityType);
	}
	public DValue buildFakeValue(DStructType entityType) {
		DValue dval1 = null;
		ScalarValueBuilder valBuilder = factorySvc.createScalarValueBuilder(registry);
		TypePair pkpair = DValueHelper.findPrimaryKeyFieldPair(entityType);
		switch(pkpair.type.getShape()) {
		case INTEGER:
			dval1 = valBuilder.buildInt("999");
			break;
		case LONG:
			dval1 = valBuilder.buildInt("999");
			break;
		case DATE:
		case STRING:
			dval1 = valBuilder.buildInt("999");
			break;
		default:
			DeliaExceptionHelper.throwError("unknown-pk-type", "%s: %s can't be pk", pkpair.name, pkpair.type);
		}
		return dval1;
	}
	
}