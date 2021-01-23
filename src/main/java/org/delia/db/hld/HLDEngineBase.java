package org.delia.db.hld;

import java.util.ArrayList;
import java.util.List;

import org.delia.assoc.DatIdMap;
import org.delia.compiler.ast.QueryExp;
import org.delia.core.FactoryService;
import org.delia.db.hld.cond.OpFilterCond;
import org.delia.db.hld.cud.HLDDelete;
import org.delia.db.hld.cud.HLDDsonBuilder;
import org.delia.db.hld.cud.HLDInsert;
import org.delia.db.hld.cud.HLDUpdate;
import org.delia.db.hld.simple.AssocOneSideSelectRenderer;
import org.delia.db.hld.simple.HavingOneSubSelectRenderer;
import org.delia.db.hld.simple.SimpleBase;
import org.delia.db.hld.simple.SimpleDelete;
import org.delia.db.hld.simple.SimpleSelect;
import org.delia.db.hld.simple.SimpleSqlBuilder;
import org.delia.db.hld.simple.SimpleUpdate;
import org.delia.db.hld.simple.SubSelectRenderer;
import org.delia.relation.RelationInfo;
import org.delia.runner.VarEvaluator;
import org.delia.sprig.SprigService;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.type.TypePair;
import org.delia.util.DRuleHelper;
import org.delia.util.DValueHelper;

/**
 * Generates the lower-level HLD objects such as HLDQuery,HLDInsert,etc
 * The HLD statement classes contain one or more of these lower-level objects
 * 
 * single use!!!
 * 
 * @author ian
 *
 */
public abstract class HLDEngineBase extends HLDExtendedServiceBase {
	protected QueryBuilderHelper queryBuilderHelper;
	private SimpleSqlBuilder simpleBuilder;

	public HLDEngineBase(DTypeRegistry registry, FactoryService factorySvc, DatIdMap datIdMap, SprigService sprigSvc) {
		super(registry, factorySvc, datIdMap, sprigSvc);
		this.queryBuilderHelper = new QueryBuilderHelper(registry, factorySvc);
		this.simpleBuilder = new SimpleSqlBuilder();
	}
	
	public abstract HLDQuery buildQuery(QueryExp queryExp);
	
	/**
	 * if insert statement include values for parent relations we need to add an update
	 * statement.
	 * @param structType - main type being inserted
	 * @param dval - values
	 * @param moreL 
	 * @param pkval2 
	 */
	protected List<HLDUpdate> generateParentUpdateIfNeeded(DStructType structType, DValue dval, DValue pkval, List<SimpleBase> moreL) {
		List<HLDUpdate> updateL = new ArrayList<>();

		pkval = (pkval != null) ? pkval : DValueHelper.findPrimaryKeyValue(dval);
		for(TypePair pair: structType.getAllFields()) {
			if (pair.type.isStructShape()) {
				DValue inner = dval.asStruct().getField(pair.name);
				if (inner == null) {
					continue;
				}
				
				RelationInfo relinfo = DRuleHelper.findMatchingRuleInfo(structType, pair);
				if (relinfo.isParent) {
					if (relinfo.isOneToOne()) {
						DValue fkval = inner.asRelation().getForeignKey();
						HLDUpdate update = addFkUpdateStatement(relinfo, pkval, fkval);
						updateL.add(update);
					} else if (relinfo.isOneToMany()) {
						for(DValue fkval: inner.asRelation().getMultipleKeys()) {
							HLDUpdate update = addFkUpdateStatement(relinfo, pkval, fkval);
							updateL.add(update);
						}
					}
				} 
			}
		}
		for(HLDUpdate hld: updateL) {
			SimpleUpdate simple = simpleBuilder.buildFrom(hld);
			moreL.add(simple);
		}
		return updateL;
	}

	protected HLDUpdate addFkUpdateStatement(RelationInfo relinfo, DValue pkval, DValue fkval) {
		HLDDsonBuilder hldBuilder = getHldBuilder();

		DStructType targetType = relinfo.farType;
		QueryExp queryExp = queryBuilderHelper.buildPKQueryExp(targetType, fkval); //queryBuilderSvc.createPrimaryKeyQuery(targetType.getName(), fkval);
		
		HLDQuery hldquery = buildQuery(queryExp);
		TypePair targetPKPair = DValueHelper.findPrimaryKeyFieldPair(targetType);
		
		HLDUpdate hld = hldBuilder.buildSimpleUpdate(targetType, targetPKPair.name, fkval, relinfo.otherSide.fieldName, pkval);
		hld.hld = hldquery;
		return hld;
	}

	protected void addFkUpdateChildForDeleteParentStatement(RelationInfo relinfo, DValue pkval, HLDQuery hldQuery2, List<SimpleBase> moreL) {
		HLDDsonBuilder hldBuilder = getHldBuilder();
		if (hldQuery2.isPKQuery()) {
			DStructType targetType = relinfo.farType;
			QueryExp queryExp = queryBuilderHelper.createEqQuery(targetType, relinfo.otherSide.fieldName, pkval);
			TypePair targetPKPair = DValueHelper.findPrimaryKeyFieldPair(targetType);
			
			HLDUpdate hld = hldBuilder.buildSimpleUpdate(targetType, targetPKPair.name, pkval, relinfo.otherSide.fieldName, null);
			hld.hld = buildQuery(queryExp);
			SimpleUpdate simple = simpleBuilder.buildFrom(hld);
			moreL.add(simple);
		} else {
			DStructType targetType = relinfo.farType;
			TypePair targetPKPair = DValueHelper.findPrimaryKeyFieldPair(targetType);
			
			DValue junk = queryBuilderHelper.buildFakeValue(relinfo.nearType);
			HLDUpdate hld = hldBuilder.buildSimpleUpdate(targetType, targetPKPair.name, junk, relinfo.otherSide.fieldName, null);
			hld.hld = buildRelQuery(relinfo, true);
			hld.isSubSelect = true;
			
			SimpleUpdate simple = simpleBuilder.buildFrom(hld);
			moreL.add(simple);
			
			targetPKPair = DValueHelper.findPrimaryKeyFieldPair(relinfo.nearType);
			attachSubSelect(hld.hld, hldQuery2.originalQueryExp, targetPKPair.name);
		}
	}

	private void attachSubSelect(HLDQuery hld, QueryExp queryExp, String targetFieldName) {
//		WHERE t1.cust IN (SELECT t2.cid FROM Customer as t2 WHERE t2.x > ?", "10");
		HLDQuery hldquery2 = buildQuery(queryExp);
		removeAllButTargetField(hldquery2, targetFieldName);
		
		SimpleSelect simpleSel = simpleBuilder.buildFrom(hldquery2);
		OpFilterCond ofc = (OpFilterCond) hld.filter;
		ofc.customRenderer = new SubSelectRenderer(factorySvc, registry, simpleSel);
	}

	private HLDQuery buildRelQuery(RelationInfo relinfo, boolean isFlipped) {
		if (isFlipped) {
			DStructType targetType = relinfo.farType;
			DValue junk = queryBuilderHelper.buildFakeValue(relinfo.nearType);
			QueryExp queryExp = queryBuilderHelper.createEqQuery(targetType, relinfo.otherSide.fieldName, junk);
			return buildQuery(queryExp);
		} else {
			DStructType targetType = relinfo.nearType;
			DValue junk = queryBuilderHelper.buildFakeValue(relinfo.farType);
			QueryExp queryExp = queryBuilderHelper.createEqQuery(targetType, relinfo.fieldName, junk);
			return buildQuery(queryExp);
		}
	}
	private HLDQuery buildPKQuery(RelationInfo relinfo, boolean isFlipped) {
		if (isFlipped) {
			DStructType targetType = relinfo.farType;
			TypePair pkpair = DValueHelper.findPrimaryKeyFieldPair(targetType);
			DValue junk = queryBuilderHelper.buildFakeValue(relinfo.nearType);
			QueryExp queryExp = queryBuilderHelper.createEqQuery(targetType, pkpair.name, junk);
			return buildQuery(queryExp);
		} else {
			DStructType targetType = relinfo.nearType;
			TypePair pkpair = DValueHelper.findPrimaryKeyFieldPair(targetType);
			DValue junk = queryBuilderHelper.buildFakeValue(relinfo.farType);
			QueryExp queryExp = queryBuilderHelper.createEqQuery(targetType, pkpair.name, junk);
			return buildQuery(queryExp);
		}
	}

	private void removeAllButTargetField(HLDQuery hldquery, String fieldName) {
		HLDField fld = hldquery.fieldL.stream().filter(x -> x.fieldName.equals(fieldName)).findAny().get();
		hldquery.fieldL.clear();
		hldquery.fieldL.add(fld);
	}

	protected void addFkDeleteChildForDeleteParentStatement(RelationInfo relinfo, DValue pkval, List<SimpleBase> moreL) {
		HLDDsonBuilder hldBuilder = getHldBuilder();

		DStructType targetType = relinfo.farType;
		QueryExp queryExp = queryBuilderHelper.createEqQuery(targetType, relinfo.otherSide.fieldName, pkval);
		
		HLDQuery hldquery = this.buildQuery(queryExp);
		HLDDelete hld = hldBuilder.buildSimpleDelete(targetType);
		hld.hld = hldquery;
		
		SimpleDelete simple = simpleBuilder.buildFrom(hld);
		moreL.add(simple);
	}
	protected void xaddFkDeleteParentStatement(RelationInfo relinfo, HLDQuery hldquery2, List<SimpleBase> moreL) {
		HLDDsonBuilder hldBuilder = getHldBuilder();
		DStructType targetType = relinfo.nearType;
		HLDDelete hld = hldBuilder.buildSimpleDelete(targetType);
		hld.hld = buildPKQuery(relinfo, false);
		
		SimpleDelete simple = simpleBuilder.buildFrom(hld);
		moreL.add(simple);
		attachSubSelect(hld.hld, hldquery2.originalQueryExp, relinfo.otherSide.fieldName);
	}
	protected void xxaddFkDeleteChildForDeleteParentStatement(RelationInfo relinfo, HLDQuery hldquery2, DValue pkval, List<SimpleBase> moreL) {
		HLDDsonBuilder hldBuilder = getHldBuilder();

		boolean areSame = relinfo.nearType == hldquery2.fromType;
		DStructType targetType;
		boolean isParent;
		HLDDelete hld;
		if (areSame) {
			targetType = relinfo.farType;
			QueryExp queryExp = queryBuilderHelper.createEqQuery(targetType, relinfo.otherSide.fieldName, pkval);
			
			HLDQuery hldquery = this.buildQuery(queryExp);
			hld = hldBuilder.buildSimpleDelete(targetType);
			hld.hld = hldquery;
			isParent = relinfo.otherSide.isParent;
		} else {
			targetType = relinfo.nearType;
			QueryExp queryExp = queryBuilderHelper.createEqQuery(targetType, relinfo.fieldName, pkval);
			
			HLDQuery hldquery = this.buildQuery(queryExp);
			hld = hldBuilder.buildSimpleDelete(targetType);
			hld.hld = hldquery;
			isParent = relinfo.isParent;
		}
		
		SimpleDelete simple = simpleBuilder.buildFrom(hld);
		moreL.add(simple);
		//(select a.cid from customer as a inner join address as b on a.cid=b.cust group by a.cid having count(a.cid)=1);
		SimpleSelect simpleSel = simpleBuilder.buildFrom(buildQuery(hldquery2.originalQueryExp));
		boolean flipped = simpleSel.hld.fromType != targetType;
		OpFilterCond ofc = (OpFilterCond) hld.hld.filter;
		ofc.customRenderer = new HavingOneSubSelectRenderer(factorySvc, registry, simpleSel, relinfo, flipped, isParent);
	}
	protected void mmaddFkDeleteChildForDeleteParentStatement(RelationInfo relinfo, HLDQuery hldquery2, DValue pkval, List<SimpleBase> moreL, HLDQueryBuilderAdapter builderAdapter) {
		HLDDsonBuilder hldBuilder = getHldBuilder();

		boolean areSame = relinfo.nearType == hldquery2.fromType;
		if (! areSame) {
			boolean parentIsOptional = relinfo.nearType.fieldIsOptional(relinfo.fieldName);
			boolean childIsOptional = relinfo.farType.fieldIsOptional(relinfo.otherSide.fieldName);
			DStructType targetType;
			HLDDelete hld;
			targetType = relinfo.farType;
			hld = hldBuilder.buildAssocDeleteOne(builderAdapter, relinfo, pkval, datIdMap);
			SimpleDelete simple1 = simpleBuilder.buildFrom(hld);
			
			if (!childIsOptional || !parentIsOptional) {
				QueryExp queryExp = queryBuilderHelper.createEqQuery(targetType, relinfo.fieldName, pkval);
				hld = hldBuilder.buildSimpleDelete(targetType);
				hld.hld = buildQuery(queryExp);
				SimpleDelete simple = simpleBuilder.buildFrom(hld);
				moreL.add(simple);
				SimpleSelect simpleSel = simpleBuilder.buildFrom(buildQuery(hldquery2.originalQueryExp));
				boolean flipped = simpleSel.hld.fromType != targetType;
				OpFilterCond ofc = (OpFilterCond) hld.hld.filter;
				ofc.customRenderer = new AssocOneSideSelectRenderer(factorySvc, registry, simpleSel, relinfo, flipped, datIdMap);
			}
			moreL.add(simple1); //want this one last
		} else {
			boolean parentIsOptional = relinfo.farType.fieldIsOptional(relinfo.otherSide.fieldName);
			boolean childIsOptional = relinfo.nearType.fieldIsOptional(relinfo.fieldName);
			DStructType targetType;
			HLDDelete hld;
			targetType = relinfo.farType;
			hld = hldBuilder.buildAssocDeleteOne(builderAdapter, relinfo, pkval, datIdMap);
			SimpleDelete simple1 = simpleBuilder.buildFrom(hld);
			
			if (!childIsOptional || !parentIsOptional) {
				QueryExp queryExp = queryBuilderHelper.createEqQuery(targetType, relinfo.fieldName, pkval);
				hld = hldBuilder.buildSimpleDelete(targetType);
				hld.hld = buildQuery(queryExp);
				SimpleDelete simple = simpleBuilder.buildFrom(hld);
				moreL.add(simple);
				SimpleSelect simpleSel = simpleBuilder.buildFrom(buildQuery(hldquery2.originalQueryExp));
				boolean flipped = simpleSel.hld.fromType != targetType;
				OpFilterCond ofc = (OpFilterCond) hld.hld.filter;
				ofc.customRenderer = new AssocOneSideSelectRenderer(factorySvc, registry, simpleSel, relinfo, flipped, datIdMap);
			}
			moreL.add(simple1); //want this one last
		}
	}

	protected List<HLDInsert> generateAssocInsertsIfNeeded(DStructType structType, DValue dval, List<SimpleBase> moreL) {
		List<HLDInsert> insertL = new ArrayList<>();
		
		DValue pkval = DValueHelper.findPrimaryKeyValue(dval);
		TypePair pkpair = DValueHelper.findPrimaryKeyFieldPair(structType);
		for(TypePair pair: structType.getAllFields()) {
			if (pair.type.isStructShape()) {
				DValue inner = dval.asStruct().getField(pair.name);
				if (inner == null) {
					continue;
				}
				
				RelationInfo relinfo = DRuleHelper.findMatchingRuleInfo(structType, pair);
				if (relinfo.isManyToMany()) {
					for(DValue fkval: inner.asRelation().getMultipleKeys()) {
						HLDInsert insert = addAssocInsertStatement(relinfo, pkpair.name, pkval, fkval);
						insert.assocRelInfo = relinfo;
						insertL.add(insert);
					}
				}
			}
		}
		
		for(HLDInsert hld: insertL) {
			moreL.add(simpleBuilder.buildFrom(hld));
		}
		return insertL;
	}
	protected HLDInsert addAssocInsertStatement(RelationInfo relinfo, String pkFieldName, DValue pkval, DValue fkval) {
		HLDDsonBuilder hldBuilder = getHldBuilder();
		HLDInsert hld = hldBuilder.buildAssocInsert(relinfo, pkval, fkval, datIdMap);
		return hld;
	}
	
	// --- delete stuff ---
	/**
	 * delete statement. find all 1:1 or 1:N relations where we are parent and far end is optional.
	 * Since we are deleting the parent, update the child to set parent field to null.
	 * @param structType - main type being deleted
	 * @param hldQuery 
	 * @param moreL 
	 * @param dval - values
	 * @param pkval2 
	 */
	protected void generateParentUpdateForDelete(DStructType structType, DValue pkval, HLDQuery hldQuery, List<SimpleBase> moreL) {
		for(TypePair pair: structType.getAllFields()) {
			if (pair.type.isStructShape()) {
				RelationInfo relinfo = DRuleHelper.findMatchingRuleInfo(structType, pair);
				if (relinfo.isParent) {
					boolean childIsOptional = relinfo.farType.fieldIsOptional(relinfo.otherSide.fieldName);
					if (childIsOptional) {
						if (relinfo.isOneToOne() || relinfo.isOneToMany()) {
							addFkUpdateChildForDeleteParentStatement(relinfo, pkval, hldQuery, moreL);
						}
					}
				} 
			}
		}
	}
	
	/**
	 * delete statement. find all 1:1 or 1:N relations where we are parent and far end is optional.
	 * Since we are deleting the parent, update the child to set parent field to null.
	 * @param structType - main type being deleted
	 * @param hldquery 
	 * @param moreL 
	 * @param hldEngine 
	 * @param dval - values
	 * @param pkval2 
	 */
	protected void generateParentDeleteForDelete(DStructType structType, DValue pkval, HLDQuery hldquery, List<SimpleBase> moreL, HLDQueryBuilderAdapter builderAdapter) {
		for(TypePair pair: structType.getAllFields()) {
			if (pair.type.isStructShape()) {
				RelationInfo relinfo = DRuleHelper.findMatchingRuleInfo(structType, pair);
				if (relinfo.isManyToMany()) {
					mmaddFkDeleteChildForDeleteParentStatement(relinfo, hldquery, pkval, moreL, builderAdapter);
					continue;
				} else if (relinfo.isParent) {
					boolean childIsOptional = relinfo.farType.fieldIsOptional(relinfo.otherSide.fieldName);
					if (! childIsOptional) {
						if (relinfo.isOneToOne()) {
							addFkDeleteChildForDeleteParentStatement(relinfo, pkval, moreL);
						} else if (relinfo.isOneToMany()) {
							xxaddFkDeleteChildForDeleteParentStatement(relinfo, hldquery, pkval, moreL);
						}
					}
				} else {
					boolean childIsOptional = relinfo.nearType.fieldIsOptional(relinfo.fieldName);
					if (!childIsOptional) { //we deleting child. 
						if (relinfo.isOneToOne()) {
							xaddFkDeleteParentStatement(relinfo.otherSide, hldquery, moreL);
						} else if (relinfo.isOneToMany()) {
							xxaddFkDeleteChildForDeleteParentStatement(relinfo.otherSide, hldquery, pkval, moreL);
						}
					} 
				}
			}
		}
	}
	
}