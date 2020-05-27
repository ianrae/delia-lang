package org.delia.rule.rules;

import java.util.List;
import java.util.Map;

import org.delia.error.DetailedError;
import org.delia.relation.RelationCardinality;
import org.delia.relation.RelationInfo;
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
import org.delia.util.DRuleHelper;
import org.delia.util.DValueHelper;
import org.delia.valuebuilder.RelationValueBuilder;

public class RelationOneRule extends RelationRuleBase {
	private boolean isParent;

	public RelationOneRule(RuleGuard guard, RuleOperand oper1, 
			DStructType owningType, DTypeRegistry registry, boolean isParent, String relationName) {
		super("relationOne", guard, oper1, owningType, registry, relationName);
		this.isParent = isParent;
	}
	@Override
	protected boolean onValidate(DValue dval, DRuleContext ctx) {
		DRelation drel = oper1.asRelation(dval);
		if (drel == null) {
			if (isMandatoryFK()) {
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
				if (relInfo.cardinality.equals(RelationCardinality.ONE_TO_ONE)) {
					bb = chkRelationUniqueness(ctx, drel, dval);
					if (! bb) {
						return false;
					}
				}
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
			boolean ok = this.chkRelationUniqueness(ctx, drel, dval);
			if (!ok) {
				return false;
			}
		}

		return true;
	}
	
	private boolean chkRelationUniqueness(DRuleContext ctx, DRelation drel, DValue dvalBeingValidated) {
		List<DValue> dvalL = ctx.getFetchRunner().queryFKs(owningType, oper1.getSubject(), drel);
		if (dvalL.isEmpty()) {
//		qresResult.err = qrespFetch.err;
		} else {
			if (dvalL.size() == 1) { //detect and ignore self-join
				DValue tmp = dvalL.get(0);
				DValue pk1 = DValueHelper.findPrimaryKeyValue(tmp);
				DValue pk2 = DValueHelper.findPrimaryKeyValue(dvalBeingValidated);
				if (pk1 != null && pk1.asString().equals(pk2.asString())) {
					return true; //ok
				}
			}
			
			String key = drel.getForeignKey().asString();
			String msg = String.format("relation field '%s' one - foreign key '%s' already used -- type %s", getSubject(), key, owningType.getName());
			addDetailedError(ctx, msg, getSubject());
			return false;
		}
		return true;
	}
	
	
	private void addDetailedError(DRuleContext ctx, String msg, String fieldName) {
		DetailedError err = ctx.addError(this, msg);
		err.setFieldName(fieldName);
	}
	//FUTURE: the following mutates a DValue. is this ok for multi-threading?
	private boolean populateOtherSideOfRelation(DValue dval, DRuleContext ctx, QueryResponse qrespFetch, boolean otherSideIsMany) {
		DValue otherSide = qrespFetch.dvalList.get(0);
		TypePair otherRelPair = findMatchingRel();
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
	private boolean isMandatoryFK() {
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
	private TypePair findMatchingRel() {
		if (relInfo.otherSide == null) {
			return null; //one-sided relation
		}
		String otherSideFieldName = relInfo.otherSide.fieldName;
		return DValueHelper.findField(relInfo.farType, otherSideFieldName);
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

		RelationInfo farInfo = info.otherSide;
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
}