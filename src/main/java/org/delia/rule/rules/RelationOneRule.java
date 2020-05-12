package org.delia.rule.rules;

import java.util.Map;

import org.delia.error.DetailedError;
import org.delia.relation.RelationInfo;
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

public class RelationOneRule extends DRuleBase {
	private RuleOperand oper1;
	private DStructType owningType;
	private DTypeRegistry registry;
	private boolean isParent;
	public RelationInfo relInfo;
	private String relationName; //either user-defined or delia assigns a name
	public boolean nameIsExplicit;

	public RelationOneRule(RuleGuard guard, RuleOperand oper1, 
			DStructType owningType, DTypeRegistry registry, boolean isParent, String relationName) {
		super("relationOne", guard);
		this.oper1 = oper1;
		this.owningType = owningType;
		this.registry = registry;
		this.isParent = isParent;
		this.relationName = relationName;
	}
	@Override
	protected boolean onValidate(DValue dval, DRuleContext ctx) {
//		if (ctx.isPlanModeFlg()) {
//			return true;
//		}
		DRelation drel = oper1.asRelation(dval);
		if (drel == null) {
			if (mustHaveFK()) {
				String key = oper1.getSubject();
				String msg = String.format("relation field '%s' one -  a foreign key value must be specified.", key);
				addDetailedError(ctx, msg, getSubject());
				return false;
			}
			return true; 
		}

		if (ctx.getDBCapabilities().supportsReferentialIntegrity()) {
			return true; //let the db do it
		}
		
		//first ensure foreign key points to existing record
//		QueryResponse qrespFetch = ctx.getFetchRunner().load(drel);
		boolean fkObjectExists = ctx.getFetchRunner().queryFKExists(drel);
		boolean otherSideIsMany = false;
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
			
			if (ctx.isEnableRelationModifierFlag()) {
				//Note: we use queryFKExists above (for perf during import)
				//then if needed use load here to get entire object
				QueryResponse qrespFetch = ctx.getFetchRunner().load(drel);
				otherSideIsMany = populateOtherSideOfRelation(dval, ctx, qrespFetch, otherSideIsMany);
			}
		}
		
		//next ensure this is only foreign key of that value
		if (!otherSideIsMany) {
			boolean exists = ctx.getFetchRunner().queryFKExists(owningType, oper1.getSubject(), drel);
			if (!exists) {
//			qresResult.err = qrespFetch.err;
			} else {
				String key = drel.getForeignKey().asString();
				String msg = String.format("relation field '%s' one - foreign key '%s' already used -- type %s", getSubject(), key, owningType.getName());
				addDetailedError(ctx, msg, getSubject());
				return false;
			}
		}

		return true;
	}
	private void addDetailedError(DRuleContext ctx, String msg, String fieldName) {
		DetailedError err = ctx.addError(this, msg);
		err.setFieldName(fieldName);
	}
	//TODO: should we save results in del.setFetchedItems ??
	//TODO: the following mutates a DValue. is this ok for multi-threading?
	private boolean populateOtherSideOfRelation(DValue dval, DRuleContext ctx, QueryResponse qrespFetch, boolean otherSideIsMany) {
		//TODO: should we save results in del.setFetchedItems ??
		//TODO: the following mutates a DValue. is this ok for multi-threading?
		DValue otherSide = qrespFetch.dvalList.get(0);
		TypePair otherRelPair = findMatchingRel(otherSide, dval.getType());
		if (otherRelPair != null) { //one-side relations won't have otherRelPair

			otherSideIsMany = isOtherSideMany(otherSide, otherRelPair);

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
		return otherSideIsMany; //may have changed
	}
	
	private boolean isOtherSideMany(DValue otherSide, TypePair otherRelPair) {
		return DRuleHelper.isOtherSideMany(otherSide.getType(), otherRelPair);
	}
	private boolean mustHaveFK() {
		String fieldName = oper1.getSubject();
		boolean optional = owningType.fieldIsOptional(fieldName);
		if (optional || isParent) {
			return false;
		}
		return true;
	}
	private String getPrimaryKey(DValue dval) {
		TypePair pair = DValueHelper.findPrimaryKeyFieldPair(dval.getType());
		return dval.asStruct().getField(pair.name).asString();
	}
	private TypePair findMatchingRel(DValue otherSide, DType targetType) {
		//TODO: later also use named relations
		DStructType dtype = (DStructType) otherSide.getType();
		return DRuleHelper.findMatchingRelByType(dtype, targetType);
	}
	@Override
	public boolean dependsOn(String fieldName) {
		return oper1.dependsOn(fieldName);
	}
	@Override
	public String getSubject() {
		return oper1.getSubject();
	}
	public boolean isParent() {
		return isParent;
	}
	public void forceParentFlag(boolean b) {
		isParent = b;
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
		if (farInfo == null) {
			return;
		}
//		QueryResponse qresp = fetchRunner.load(info.farType.getName(), farInfo.fieldName, keyVal);
		QueryResponse qresp = fetchRunner.loadFKOnly(info.farType.getName(), farInfo.fieldName, keyVal);
		if (!qresp.ok) {
			return; //!!
		}
		if (qresp.emptyResults()) {
			return;
		}
		
		DValue otherSideKeyVal = qresp.getOne();
		
		DType relType = this.registry.getType(BuiltInTypes.RELATION_SHAPE);
		String typeName = info.farType.getName();
		RelationValueBuilder builder = new RelationValueBuilder(relType, typeName, registry);
		builder.buildFromString(otherSideKeyVal.asString());
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