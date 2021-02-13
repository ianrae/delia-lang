package org.delia.zdb.mem;

import org.delia.relation.RelationInfo;
import org.delia.rule.rules.RelationManyRule;
import org.delia.rule.rules.RelationOneRule;
import org.delia.runner.FetchRunner;
import org.delia.type.BuiltInTypes;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.util.DRuleHelper;
import org.delia.util.DValueHelper;
import org.delia.util.DeliaExceptionHelper;
import org.delia.valuebuilder.RelationValueBuilder;

public class RelationFetchHelper {
	private FetchRunner fetchRunner;
	private DTypeRegistry registry;

	public RelationFetchHelper(DTypeRegistry registry, FetchRunner fetchRunner) {
		this.registry = registry;
		this.fetchRunner = fetchRunner;
	}

	public boolean fetchParentSide(DValue dval, String targetFieldName) {
		DStructType structType = (DStructType) dval.getType();
		RelationInfo relinfo = DRuleHelper.findMatchingRuleInfo(structType, targetFieldName);
		if (relinfo != null && relinfo.isParent && ! relinfo.isManyToMany()) {
			DValue inner = this.createRelation(dval, targetFieldName);
			dval.asMap().put(targetFieldName, inner);
			RelationOneRule oneRule = DRuleHelper.findOneRule(structType, targetFieldName);
			if (oneRule != null) {
				oneRule.populateFK(dval, fetchRunner);
				inner = dval.asStruct().getField(targetFieldName); //new relation obj
			} else {
				RelationManyRule manyRule = DRuleHelper.findManyRule(structType, targetFieldName);
				manyRule.populateFK(dval, fetchRunner);
				inner = dval.asStruct().getField(targetFieldName);
			}
			
			//it may be that its an empty relation. if so, don't fetch
			if (inner.asRelation().getMultipleKeys().isEmpty()) {
				inner = null;
				dval.asMap().put(targetFieldName, inner);
				return true;
			}
		}
		return false;
	}

	//create empty relation
	private DValue createRelation(DValue dval, String fieldName) {
		DType relType = registry.getType(BuiltInTypes.RELATION_SHAPE);
		DType farEndType = DValueHelper.findFieldType(dval.getType(), fieldName);
		String typeName = farEndType.getName();
		RelationValueBuilder builder = new RelationValueBuilder(relType, typeName, registry);
		builder.buildEmptyRelation();
		boolean b = builder.finish();
		if (!b) {
			DeliaExceptionHelper.throwError("relation-create-failed-assocCrud", "Type '%s': Failed to create empty relation", typeName);
			return null;
		} else {
			DValue dvalx = builder.getDValue();
			return dvalx;
		}
	}

}