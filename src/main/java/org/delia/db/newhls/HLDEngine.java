package org.delia.db.newhls;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.delia.assoc.DatIdMap;
import org.delia.compiler.ast.InsertStatementExp;
import org.delia.compiler.ast.QueryExp;
import org.delia.compiler.ast.UpdateStatementExp;
import org.delia.core.FactoryService;
import org.delia.db.QueryBuilderService;
import org.delia.db.QuerySpec;
import org.delia.db.newhls.cud.HLDDelete;
import org.delia.db.newhls.cud.HLDDsonBuilder;
import org.delia.db.newhls.cud.HLDInsert;
import org.delia.db.newhls.cud.HLDInsertStatement;
import org.delia.db.newhls.cud.HLDUpdate;
import org.delia.db.newhls.cud.HLDUpdateStatement;
import org.delia.db.sql.fragment.InsertStatementFragment;
import org.delia.log.Log;
import org.delia.relation.RelationInfo;
import org.delia.rule.rules.RelationManyRule;
import org.delia.sprig.SprigService;
import org.delia.type.DRelation;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.type.TypePair;
import org.delia.util.DRuleHelper;
import org.delia.util.DValueHelper;
import org.delia.util.DeliaExceptionHelper;

/**
 * Generates the lower-level HLD objects such as HLDQuery,HLDInsert,etc
 * The HLD statement classes contain one or more of these lower-level objects
 * 
 * single use!!!
 * 
 * @author ian
 *
 */
public class HLDEngine {
	private DTypeRegistry registry;
	private FactoryService factorySvc;
	private DatIdMap datIdMap;
	private Log log;
	private SprigService sprigSvc;
	private HLDAliasManager aliasMgr;

	public HLDEngine(DTypeRegistry registry, FactoryService factorySvc, Log log, DatIdMap datIdMap, SprigService sprigSvc) {
		this.registry = registry;
		this.factorySvc = factorySvc;
		this.datIdMap = datIdMap;
		this.log = log;
		this.sprigSvc = sprigSvc;
		
		this.aliasMgr = new HLDAliasManager(factorySvc, datIdMap);
	}
	
	public HLDQuery buildQuery(QueryExp queryExp) {
		HLDAliasManager aliasMgr = new HLDAliasManager(factorySvc, datIdMap);
		return buildQuery(queryExp, aliasMgr);
	}
	public HLDQuery buildQuery(QueryExp queryExp, HLDAliasManager aliasMgr) {
		HLDQueryBuilder hldBuilder = new HLDQueryBuilder(registry);

		HLDQuery hld = hldBuilder.build(queryExp);

		JoinTreeBuilder joinBuilder = new JoinTreeBuilder();
		joinBuilder.generateJoinTree(hld);

		HLDFieldBuilder fieldBuilder = new HLDFieldBuilder();
		fieldBuilder.generateFields(hld);
		
		HLDAliasBuilder aliasBuilder = new HLDAliasBuilder(aliasMgr);
		aliasBuilder.assignAliases(hld);
		
		return hld;
	}
	
	public HLDDelete buildDelete(QueryExp queryExp) {
		HLDQuery hld = buildQuery(queryExp);
		HLDDelete hlddel = new HLDDelete(hld);
		return hlddel;
	}
	
	public HLDInsert buildInsert(InsertStatementExp insertExp) {
		HLDDsonBuilder hldBuilder = new HLDDsonBuilder(registry, factorySvc, log, sprigSvc);
		HLDInsert hld = hldBuilder.buildInsert(insertExp);
		return hld;
	}
	public List<HLDUpdate> addParentUpdates(HLDInsert hld) {
		DStructType structType = hld.getStructType();
		List<HLDUpdate> parentUpdates = generateParentUpdateIfNeeded(structType, hld, hld.cres.dval);
		return parentUpdates;
	}
	public List<HLDInsert> addAssocInserts(HLDInsert hld) {
		DStructType structType = hld.getStructType();
		List<HLDInsert> parentUpdates = generateAssocInsertsIfNeeded(structType, hld, hld.cres.dval);
		return parentUpdates;
	}

	public HLDUpdate buildUpdate(UpdateStatementExp updateExp) {
		HLDDsonBuilder hldBuilder = new HLDDsonBuilder(registry, factorySvc, log, sprigSvc);
		HLDUpdate hld = hldBuilder.buildUpdate(updateExp);
		return doBuildUpdate(hld, updateExp.queryExp);
	}
	
	private HLDUpdate doBuildUpdate(HLDUpdate hld, QueryExp queryExp) {
		hld.hld = buildQuery(queryExp);
		hld.querySpec = new QuerySpec();
		hld.querySpec.evaluator = null; //TOOD fix
		hld.querySpec.queryExp = queryExp;
		return hld;
	}

	/**
	 * if insert statement include values for parent relations we need to add an update
	 * statement.
	 * @param structType - main type being inserted
	 * @param dval - values
	 */
	private List<HLDUpdate> generateParentUpdateIfNeeded(DStructType structType, HLDInsert hld, DValue dval) {
		List<HLDUpdate> updateL = new ArrayList<>();
		
		DValue pkval = DValueHelper.findPrimaryKeyValue(dval);
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

	private HLDUpdate addFkUpdateStatement(RelationInfo relinfo, String pkFieldName, DValue pkval, DValue fkval) {
		HLDDsonBuilder hldBuilder = new HLDDsonBuilder(registry, factorySvc, log, sprigSvc);

		DStructType targetType = relinfo.farType;
		TypePair targetPKPair = DValueHelper.findPrimaryKeyFieldPair(targetType);
		
		HLDUpdate hld = hldBuilder.buildSimpleUpdate(targetType, targetPKPair.name, fkval, relinfo.otherSide.fieldName, pkval);
		
		QueryBuilderService queryBuilderSvc = factorySvc.getQueryBuilderService();
		hld.querySpec = new QuerySpec();
		hld.querySpec.evaluator = null; //TODO fix
		hld.querySpec.queryExp = queryBuilderSvc.createPrimaryKeyQuery(targetType.getName(), fkval);
		
		hld.hld = this.buildQuery(hld.querySpec.queryExp);
		return hld;
	}

	private List<HLDInsert> generateAssocInsertsIfNeeded(DStructType structType, HLDInsert hld, DValue dval) {
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
	private HLDInsert addAssocInsertStatement(RelationInfo relinfo, String pkFieldName, DValue pkval, DValue fkval) {
		HLDDsonBuilder hldBuilder = new HLDDsonBuilder(registry, factorySvc, log, sprigSvc);

//		DStructType targetType = relinfo.farType;
//		TypePair targetPKPair = DValueHelper.findPrimaryKeyFieldPair(targetType);
		//getSchemaVersionType
//		HLDInsert hld = hldBuilder.buildSimpleInsert(targetType, targetPKPair.name, fkval, relinfo.otherSide.fieldName, pkval);
		HLDInsert hld = hldBuilder.buildAssocInsert(relinfo, pkval, fkval, datIdMap);
		return hld;
	}
	
	// -- aliases --
	public void assignAliases(HLDInsertStatement stmt) {
		HLDAliasBuilder aliasBuilder = new HLDAliasBuilder(aliasMgr);
		aliasBuilder.assignAliases(stmt.hldinsert);
		for(HLDUpdate hld: stmt.updateL) {
			aliasBuilder.assignAliases(hld);
		}
		for(HLDInsert hld: stmt.assocInsertL) {
			aliasBuilder.assignAliasesAssoc(hld);
		}
	}
	public void assignAliases(HLDUpdateStatement stmt) {
		HLDAliasBuilder aliasBuilder = new HLDAliasBuilder(aliasMgr);
		aliasBuilder.assignAliases(stmt.hldupdate);
//		for(HLDUpdate hld: stmt.updateL) {
//			aliasBuilder.assignAliases(hld);
//		}
	}

	public void assignAliases(HLDQueryStatement stmt) {
		HLDAliasBuilder aliasBuilder = new HLDAliasBuilder(aliasMgr);
		aliasBuilder.assignAliases(stmt.hldquery);
	}
	
}