package org.delia.rule.rules;

import java.util.List;
import java.util.Map;

import org.delia.error.DetailedError;
import org.delia.relation.RelationInfo;
import org.delia.rule.DRule;
import org.delia.rule.DRuleBase;
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
import org.delia.type.TypeReplaceSpec;
import org.delia.util.DRuleHelper;
import org.delia.util.DValueHelper;
import org.delia.valuebuilder.RelationValueBuilder;

public class RelationManyRule extends DRuleBase {
	private RuleOperand oper1;
	private DStructType owningType;
	private DTypeRegistry registry;
	public RelationInfo relInfo;
	private String relationName; //either user-defined or delia assigns a name
	public boolean nameIsExplicit;

	public RelationManyRule(RuleGuard guard, RuleOperand oper1, 
			DStructType owningType, DTypeRegistry registry, String relationName) {
		super("relationMany", guard);
		this.oper1 = oper1;
		this.owningType = owningType;
		this.registry = registry;
		this.relationName = relationName;
	}
	@Override
	protected boolean onValidate(DValue dval, DRuleContext ctx) {
		DRelation drel = oper1.asRelation(dval);
		if (drel == null) {
			if (mustHaveFK()) {
				String key = oper1.getSubject();
				String msg = String.format("relation field '%s' many -  a foreign key value must be specified.", key);
				addDetailedError(ctx, msg, getSubject());
				return false;
			}
			return true; //TODO: fix later.
		}
		
		if (ctx.getDBCapabilities().supportsReferentialIntegrity()) {
			return true; //let the db do it
		}

		//first ensure foreign key points to existing record
//		QueryResponse qrespFetch = ctx.getFetchRunner().load(drel);
		boolean fkObjectExists = ctx.getFetchRunner().queryFKExists(drel);
		if (! fkObjectExists) {
			String key = drel.getForeignKey().asString();
			String msg = String.format("relation field '%s' one - no value found for foreign key '%s'", getSubject(), key);
			addDetailedError(ctx, msg, getSubject());
			return false;
		} else {
			boolean bb = ctx.isPopulateFKsFlag();
			if (!bb) {
				return true;
			}
			
			//TODO: should we save results in del.setFetchedItems ??
			//TODO: the following mutates a DValue. is this ok for multi-threading?
			if (ctx.isEnableRelationModifierFlag()) {
				//Note: we use queryFKExists above (for perf during import)
				//then if needed use load here to get entire object
				QueryResponse qrespFetch = ctx.getFetchRunner().load(drel);
				populateOtherSideOfRelation(dval, ctx, qrespFetch);
			}
		}

		return true;
	}
	private void addDetailedError(DRuleContext ctx, String msg, String fieldName) {
		DetailedError err = ctx.addError(this, msg);
		err.setFieldName(fieldName);
	}

	private void populateOtherSideOfRelation(DValue dval, DRuleContext ctx, QueryResponse qrespFetch) {
		DValue otherSide = qrespFetch.dvalList.get(0);
		TypePair otherRelPair = findMatchingRel(otherSide, dval.getType());
		if (otherRelPair != null) { //one-side relations won't have otherRelPair
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
	}
	private boolean mustHaveFK() {
		String fieldName = oper1.getSubject();
		boolean optional = owningType.fieldIsOptional(fieldName);
		if (optional) {
			return false;
		} else {
			DStructType relType = (DStructType) DValueHelper.findFieldType(owningType, fieldName);
			String x = DValueHelper.findMatchingRelation(relType, owningType);
			System.out.println("sss " + x);
			if (relType.fieldIsOptional(x)) {
				return false;
			}
			DRule someRule = findRuleFor(relType, x);
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
		return findMatchingRel(otherSide.getType(), targetType);
	}
	public TypePair findMatchingRel(DType otherSide, DType targetType) {
		return DRuleHelper.findMatchingRelByType((DStructType) otherSide, targetType);
	}
	@Override
	public boolean dependsOn(String fieldName) {
		return oper1.dependsOn(fieldName);
	}
	@Override
	public String getSubject() {
		return oper1.getSubject();
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
		
		RelationInfo farInfo = DRuleHelper.findOtherSideOneOrMany(info.farType, info.nearType);
		
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
	@Override
	public void performTypeReplacement(TypeReplaceSpec spec) {
		if (spec.needsReplacement(this, owningType)) {
			owningType = (DStructType) spec.newType;
		}
		
		if (relInfo != null) {
			relInfo.performTypeReplacement(spec);
		}
	}
	public String getRelationName() {
		return relationName;
	}
}