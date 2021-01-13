package org.delia.db.newhls;

import java.util.ArrayList;
import java.util.List;

import org.delia.assoc.DatIdMap;
import org.delia.compiler.ast.QueryExp;
import org.delia.core.FactoryService;
import org.delia.db.QueryBuilderService;
import org.delia.db.QuerySpec;
import org.delia.db.newhls.cud.HLDDelete;
import org.delia.db.newhls.cud.HLDDsonBuilder;
import org.delia.db.newhls.cud.HLDInsert;
import org.delia.db.newhls.cud.HLDUpdate;
import org.delia.log.Log;
import org.delia.relation.RelationInfo;
import org.delia.sprig.SprigService;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.type.TypePair;
import org.delia.util.DRuleHelper;
import org.delia.util.DValueHelper;
import org.delia.valuebuilder.ScalarValueBuilder;

/**
 * Generates the lower-level HLD objects such as HLDQuery,HLDInsert,etc
 * The HLD statement classes contain one or more of these lower-level objects
 * 
 * single use!!!
 * 
 * @author ian
 *
 */
public abstract class HLDEngineBase {
	protected DTypeRegistry registry;
	protected FactoryService factorySvc;
	protected DatIdMap datIdMap;
	protected Log log;
	protected SprigService sprigSvc;
	protected QueryBuilderHelper queryBuilderHelper;

	public HLDEngineBase(DTypeRegistry registry, FactoryService factorySvc, Log log, DatIdMap datIdMap, SprigService sprigSvc) {
		this.registry = registry;
		this.factorySvc = factorySvc;
		this.datIdMap = datIdMap;
		this.log = log;
		this.sprigSvc = sprigSvc;
		this.queryBuilderHelper = new QueryBuilderHelper(registry, factorySvc);
	}
	
	public abstract HLDQuery buildQuery(QueryExp queryExp);
	
	/**
	 * if insert statement include values for parent relations we need to add an update
	 * statement.
	 * @param structType - main type being inserted
	 * @param dval - values
	 * @param pkval2 
	 */
	protected List<HLDUpdate> generateParentUpdateIfNeeded(DStructType structType, DValue dval, DValue pkval) {
		List<HLDUpdate> updateL = new ArrayList<>();
		
		pkval = (pkval != null) ? pkval : DValueHelper.findPrimaryKeyValue(dval);
		TypePair pkpair = DValueHelper.findPrimaryKeyFieldPair(structType);
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
						HLDUpdate update = addFkUpdateStatement(relinfo, pkpair.name, pkval, fkval);
						updateL.add(update);
					} else if (relinfo.isOneToMany()) {
						for(DValue fkval: inner.asRelation().getMultipleKeys()) {
							HLDUpdate update = addFkUpdateStatement(relinfo, pkpair.name, pkval, fkval);
							updateL.add(update);
						}
					}
				} 
			}
		}
		return updateL;
	}

	protected HLDUpdate addFkUpdateStatement(RelationInfo relinfo, String pkFieldName, DValue pkval, DValue fkval) {
		HLDDsonBuilder hldBuilder = new HLDDsonBuilder(registry, factorySvc, log, sprigSvc);

		DStructType targetType = relinfo.farType;
		QueryExp queryExp = queryBuilderHelper.buildPKQueryExp(targetType, fkval); //queryBuilderSvc.createPrimaryKeyQuery(targetType.getName(), fkval);
		
		HLDQuery hldquery = buildQuery(queryExp);
		TypePair targetPKPair = DValueHelper.findPrimaryKeyFieldPair(targetType);
		
		HLDUpdate hld = hldBuilder.buildSimpleUpdate(targetType, targetPKPair.name, fkval, relinfo.otherSide.fieldName, pkval);
		hld.hld = hldquery;
		return hld;
	}

	protected HLDUpdate addFkUpdateChildForDeleteParentStatement(RelationInfo relinfo, String pkFieldName, DValue pkval, HLDQuery hldQuery2) {
		HLDDsonBuilder hldBuilder = new HLDDsonBuilder(registry, factorySvc, log, sprigSvc);
		if (hldQuery2.isPKQuery()) {
			DStructType targetType = relinfo.farType;
			QueryExp queryExp = queryBuilderHelper.createEqQuery(targetType, relinfo.otherSide.fieldName, pkval);
			
			HLDQuery hldquery = this.buildQuery(queryExp);
			TypePair targetPKPair = DValueHelper.findPrimaryKeyFieldPair(targetType);
			
			HLDUpdate hld = hldBuilder.buildSimpleUpdate(targetType, targetPKPair.name, pkval, relinfo.otherSide.fieldName, null);
			hld.hld = hldquery;
			return hld;
		} else {
			DStructType targetType = relinfo.farType;
			QueryExp queryExp = hldQuery2.originalQueryExp;
			
			HLDQuery hldquery = this.buildQuery(queryExp);
			TypePair targetPKPair = DValueHelper.findPrimaryKeyFieldPair(targetType);
			
			DValue junk = queryBuilderHelper.buildFakeValue(relinfo.nearType);
			HLDUpdate hld = hldBuilder.buildSimpleUpdate(targetType, targetPKPair.name, junk, relinfo.otherSide.fieldName, null);
			hld.hld = hldquery;
			hld.isSubSelect = true;
//			hld.subSelect
			return hld;
		}
	}
	protected HLDDelete addFkDeleteChildForDeleteParentStatement(RelationInfo relinfo, String pkFieldName, DValue pkval) {
		HLDDsonBuilder hldBuilder = new HLDDsonBuilder(registry, factorySvc, log, sprigSvc);

		DStructType targetType = relinfo.farType;
		QueryExp queryExp = queryBuilderHelper.createEqQuery(targetType, relinfo.otherSide.fieldName, pkval);
		
		HLDQuery hldquery = this.buildQuery(queryExp);
		TypePair targetPKPair = DValueHelper.findPrimaryKeyFieldPair(targetType);
		
		HLDDelete hld = hldBuilder.buildSimpleDeletex(targetType, targetPKPair.name, pkval, relinfo.otherSide.fieldName, null);
		hld.hld = hldquery;
		return hld;
	}

	protected List<HLDInsert> generateAssocInsertsIfNeeded(DStructType structType, DValue dval) {
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
		return insertL;
	}
	protected HLDInsert addAssocInsertStatement(RelationInfo relinfo, String pkFieldName, DValue pkval, DValue fkval) {
		HLDDsonBuilder hldBuilder = new HLDDsonBuilder(registry, factorySvc, log, sprigSvc);

		HLDInsert hld = hldBuilder.buildAssocInsert(relinfo, pkval, fkval, datIdMap);
		return hld;
	}
	
	// --- delete stuff ---
	/**
	 * delete statement. find all 1:1 or 1:N relations where we are parent and far end is optional.
	 * Since we are deleting the parent, update the child to set parent field to null.
	 * @param structType - main type being deleted
	 * @param hldQuery 
	 * @param dval - values
	 * @param pkval2 
	 */
	protected List<HLDUpdate> generateParentUpdateForDelete(DStructType structType, DValue pkval, HLDQuery hldQuery) {
		List<HLDUpdate> updateL = new ArrayList<>();
		
		TypePair pkpair = DValueHelper.findPrimaryKeyFieldPair(structType);
		for(TypePair pair: structType.getAllFields()) {
			if (pair.type.isStructShape()) {
				
				RelationInfo relinfo = DRuleHelper.findMatchingRuleInfo(structType, pair);
				if (relinfo.isParent) {
					boolean childIsOptional = relinfo.farType.fieldIsOptional(relinfo.otherSide.fieldName);
					if (relinfo.isOneToOne() && childIsOptional) {
						HLDUpdate update = addFkUpdateChildForDeleteParentStatement(relinfo, pkpair.name, pkval, hldQuery);
						updateL.add(update);
					} else if (relinfo.isOneToMany()) {
						HLDUpdate update = addFkUpdateChildForDeleteParentStatement(relinfo, pkpair.name, pkval, hldQuery);
						updateL.add(update);
					}
				} 
			}
		}
		return updateL;
	}
	
	/**
	 * delete statement. find all 1:1 or 1:N relations where we are parent and far end is optional.
	 * Since we are deleting the parent, update the child to set parent field to null.
	 * @param structType - main type being deleted
	 * @param dval - values
	 * @param pkval2 
	 */
	protected List<HLDDelete> generateParentDeleteForDelete(DStructType structType, DValue pkval) {
		List<HLDDelete> deleteL = new ArrayList<>();
		
		TypePair pkpair = DValueHelper.findPrimaryKeyFieldPair(structType);
		for(TypePair pair: structType.getAllFields()) {
			if (pair.type.isStructShape()) {
				
				RelationInfo relinfo = DRuleHelper.findMatchingRuleInfo(structType, pair);
				if (relinfo.isParent) {
					boolean childIsOptional = relinfo.farType.fieldIsOptional(relinfo.otherSide.fieldName);
					if (relinfo.isOneToOne() && !childIsOptional) {
						HLDDelete update = addFkDeleteChildForDeleteParentStatement(relinfo, pkpair.name, pkval);
						deleteL.add(update);
					} else if (relinfo.isOneToMany()) {
						HLDDelete update = addFkDeleteChildForDeleteParentStatement(relinfo, pkpair.name, pkval);
						deleteL.add(update);
					}
				} 
			}
		}
		return deleteL;
	}
	
}