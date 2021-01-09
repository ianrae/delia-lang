package org.delia.rule.rules;

import java.util.List;
import java.util.Map;

import org.delia.error.DetailedError;
import org.delia.relation.RelationInfo;
import org.delia.rule.DRule;
import org.delia.rule.DRuleContext;
import org.delia.rule.RuleGuard;
import org.delia.rule.RuleOperand;
import org.delia.runner.FetchRunner;
import org.delia.runner.QueryResponse;
import org.delia.type.BuiltInTypes;
import org.delia.type.DRelation;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.type.TypePair;
import org.delia.util.DValueHelper;
import org.delia.valuebuilder.RelationValueBuilder;

public class RelationManyRule extends RelationRuleBase {

	public RelationManyRule(RuleGuard guard, RuleOperand oper1, 
			DStructType owningType, DTypeRegistry registry, String relationName) {
		super("relationMany", guard, oper1, owningType, registry, relationName);
	}
	@Override
	protected boolean onValidate(DValue dval, DRuleContext ctx) {
		DRelation drel = oper1.asRelation(dval);
		if (drel == null) {
			if (isMandatoryFK()) {
				String key = oper1.getSubject();
				String msg = String.format("relation field '%s' many -  a foreign key value must be specified.", key);
				addDetailedError(ctx, msg, getSubject(), dval.getType().getName());
				return false;
			}
			return true; 
		}
		
		if (ctx.getDBCapabilities().supportsReferentialIntegrity()) {
			return true; //let the db do it
		}

		//first ensure foreign key points to existing record
		boolean fkObjectExists = ctx.getFetchRunner().queryFKExists(drel);
		if (! fkObjectExists) {
			String key = drel.getForeignKey().asString();
			String msg = String.format("relation field '%s' one - no value found for foreign key '%s'", getSubject(), key);
			addDetailedError(ctx, msg, getSubject(), dval.getType().getName());
			return false;
		} else {
			boolean bb = ctx.isPopulateFKsFlag();
			//add 5dec2020 as part of hls/part2
			if (relInfo.isParent && ctx.isInsertFlag()) {
				bb = true; 
			}
			if (!bb) {
				return true;
			}
			
			//FUTURE: the following mutates a DValue. is this ok for multi-threading?
			if (ctx.isEnableRelationModifierFlag()) {
				//Note: we use queryFKExists above (for perf during import)
				//then if needed use load here to get entire object
				QueryResponse qrespFetch = ctx.getFetchRunner().load(drel);
				populateOtherSideOfRelation(dval, ctx, qrespFetch);
			}
		}

		return true;
	}
	private void addDetailedError(DRuleContext ctx, String msg, String fieldName, String typeName) {
		DetailedError err = ctx.addError(this, msg);
		err.setFieldName(fieldName);
		err.setTypeName(typeName);
	}

	private void populateOtherSideOfRelation(DValue dval, DRuleContext ctx, QueryResponse qrespFetch) {
		DValue otherSide = qrespFetch.dvalList.get(0);
		TypePair otherRelPair = findMatchingRel(otherSide, dval.getType());
		if (otherRelPair != null) { //one-side relations won't have otherRelPair
			for(DValue other: qrespFetch.dvalList) {
				populateAllFKs(dval, other, otherRelPair);
			}
		}
	}
	private void populateAllFKs(DValue dval, DValue otherSide, TypePair otherRelPair) {
		DValue otherRel = otherSide.asStruct().getField(otherRelPair.name);
		if (otherRel == null) {
			DType relType = this.registry.getType(BuiltInTypes.RELATION_SHAPE);
			String typeName = dval.getType().getName();
			RelationValueBuilder builder = new RelationValueBuilder(relType, typeName, registry);
			String keyValString = getPrimaryKey(dval);
			builder.buildFromString(keyValString);
			boolean b = builder.finish();
			if (!b) {
				//err
			} else {
				Map<String,DValue> map = otherSide.asMap();
				map.put(otherRelPair.name, builder.getDValue());
			}
		} else {
			DRelation drelx = otherRel.asRelation();
			TypePair pair = DValueHelper.findPrimaryKeyFieldPair(dval.getType());
			String keyValString = pair.name;
			DValue primaryKeyVal = dval.asStruct().getField(keyValString);
			drelx.addKey(primaryKeyVal);
		}
	}
	private boolean isMandatoryFK() {
		String fieldName = oper1.getSubject();
		boolean optional = owningType.fieldIsOptional(fieldName);
		if (optional) {
			return false;
		} else {
			DStructType relType = (DStructType) DValueHelper.findFieldType(owningType, fieldName);
			if (relType.fieldIsOptional(fieldName)) {
				return false;
			}
			DRule someRule = findRuleFor(relType, fieldName);
			if (someRule instanceof RelationOneRule) {
				return false;
			}
		}
		return true;
	}
	private DRule findRuleFor(DStructType relType, String x) {
		for(DRule rule: relType.getRawRules()) {
			if (x.equals(rule.getSubject())) {
				return rule;
			}
		}
		return null;
	}
	private String getPrimaryKey(DValue dval) {
		TypePair pair = DValueHelper.findPrimaryKeyFieldPair(dval.getType());
		return dval.asStruct().getField(pair.name).asString();
	}
	private TypePair findMatchingRel(DValue otherSide, DType targetType) {
		//.otherSide never null for many relations
		String otherSideFieldName = relInfo.otherSide.fieldName;
		return DValueHelper.findField(relInfo.farType, otherSideFieldName);
	}
	
	//Customer. find address
	public void populateFK(DValue dval, FetchRunner fetchRunner) {
		RelationInfo info = this.relInfo;
		
		DValue existing = dval.asStruct().getField(info.fieldName);
		if (existing != null) {
			return;
		}
		
		TypePair pair = DValueHelper.findPrimaryKeyFieldPair(dval.getType());
		DValue keyVal = dval.asStruct().getField(pair.name);
		
		RelationInfo farInfo = info.otherSide;// DRuleHelper.findOtherSideOneOrMany(info.farType, info.nearType);
		
//		QueryResponse qresp = fetchRunner.load(info.farType.getName(), farInfo.fieldName, keyVal);
		QueryResponse qresp = fetchRunner.loadFKOnly(info.farType.getName(), farInfo.fieldName, keyVal);
		if (!qresp.ok) {
			return; //!!
		}
		if (qresp.emptyResults()) {
			return;
		}
		
		List<DValue> keylist = qresp.dvalList;
		DType relType = this.registry.getType(BuiltInTypes.RELATION_SHAPE);
		String typeName = info.farType.getName();
		RelationValueBuilder builder = new RelationValueBuilder(relType, typeName, registry);
		builder.buildFromList(keylist);
		boolean b = builder.finish();
		if (!b) {
			//err
		} else {
			Map<String,DValue> map = dval.asMap();
			map.put(relInfo.fieldName, builder.getDValue());
		}
	}
}