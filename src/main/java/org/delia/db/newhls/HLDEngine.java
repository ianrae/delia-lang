package org.delia.db.newhls;

import java.util.List;

import org.delia.assoc.DatIdMap;
import org.delia.compiler.ast.InsertStatementExp;
import org.delia.compiler.ast.QueryExp;
import org.delia.compiler.ast.UpdateStatementExp;
import org.delia.core.FactoryService;
import org.delia.db.QuerySpec;
import org.delia.db.newhls.cond.SingleFilterCond;
import org.delia.db.newhls.cud.AssocBundle;
import org.delia.db.newhls.cud.HLDDelete;
import org.delia.db.newhls.cud.HLDDeleteStatement;
import org.delia.db.newhls.cud.HLDDsonBuilder;
import org.delia.db.newhls.cud.HLDInsert;
import org.delia.db.newhls.cud.HLDInsertStatement;
import org.delia.db.newhls.cud.HLDUpdate;
import org.delia.db.newhls.cud.HLDUpdateStatement;
import org.delia.log.Log;
import org.delia.sprig.SprigService;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;

/**
 * Generates the lower-level HLD objects such as HLDQuery,HLDInsert,etc
 * The HLD statement classes contain one or more of these lower-level objects
 * 
 * single use!!!
 * 
 * @author ian
 *
 */
public class HLDEngine extends HLDEngineBase implements HLDQueryBuilderAdapter {
	private HLDAliasManager aliasMgr;

	public HLDEngine(DTypeRegistry registry, FactoryService factorySvc, Log log, DatIdMap datIdMap, SprigService sprigSvc) {
		super(registry, factorySvc, log,datIdMap, sprigSvc);
		this.aliasMgr = new HLDAliasManager(factorySvc, datIdMap);
	}
	
	@Override
	public HLDQuery buildQuery(QueryExp queryExp) {
		HLDAliasManager aliasMgr = new HLDAliasManager(factorySvc, datIdMap);
		return buildQuery(queryExp, aliasMgr, false, null);
	}
	@Override
	public HLDQuery buildQueryEx(QueryExp queryExp, DStructType structType) {
		HLDAliasManager aliasMgr = new HLDAliasManager(factorySvc, datIdMap);
		return buildQuery(queryExp, aliasMgr, false, structType);
	}
	public HLDQuery buildQuery(QueryExp queryExp, HLDAliasManager aliasMgr) {
		return buildQuery(queryExp, aliasMgr, true, null);
	}
	public HLDQuery buildQuery(QueryExp queryExp, HLDAliasManager aliasMgr, boolean doEverything, DStructType structType) {
		HLDQueryBuilder hldBuilder = new HLDQueryBuilder(registry);

		HLDQuery hld = hldBuilder.build(queryExp, structType);

		if (doEverything) {
			JoinTreeBuilder joinBuilder = new JoinTreeBuilder();
			joinBuilder.generateJoinTree(hld);

			HLDFieldBuilder fieldBuilder = new HLDFieldBuilder();
			fieldBuilder.generateFields(hld);
			
			HLDAliasBuilder aliasBuilder = new HLDAliasBuilder(aliasMgr);
			aliasBuilder.assignAliases(hld);
		}
		
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
		List<HLDUpdate> parentUpdates = generateParentUpdateIfNeeded(structType, hld.cres.dval, null);
		return parentUpdates;
	}
	public List<HLDUpdate> addParentUpdatesForUpdate(HLDUpdate hld) {
		DStructType structType = hld.getStructType();
		
		//Note. the dson body of update doesn't have pk, so we need to get it from the filter
		DValue pkval = getUpdatePK(hld); 
		
		List<HLDUpdate> parentUpdates = generateParentUpdateIfNeeded(structType, hld.cres.dval, pkval);
		return parentUpdates;
	}
	private DValue getUpdatePK(HLDUpdate hld) {
		//Note. the dson body of update doesn't have pk, so we need to get it from the filter
		SqlParamGenerator pgen = new SqlParamGenerator(registry, factorySvc);
		SingleFilterCond sfc = (SingleFilterCond) hld.hld.filter;
		DValue pkval = pgen.convert(sfc.val1);
		return pkval;
	}

	public List<HLDInsert> addAssocInserts(HLDInsert hld) {
		DStructType structType = hld.getStructType();
		List<HLDInsert> parentUpdates = generateAssocInsertsIfNeeded(structType, hld.cres.dval);
		return parentUpdates;
	}
	public List<HLDInsert> addAssocInserts(HLDUpdate hld) {
		DStructType structType = hld.getStructType();
		List<HLDInsert> parentUpdates = generateAssocInsertsIfNeeded(structType, hld.cres.dval);
		return parentUpdates;
	}
	public List<AssocBundle> addMoreAssoc(HLDUpdate hld, HLDEngineAssoc engineAssoc, QueryExp queryExp) {
		DStructType structType = hld.getStructType();
		//Note. the dson body of update doesn't have pk, so we need to get it from the filter
		DValue pkval = getUpdatePK(hld); 
		List<AssocBundle> parentUpdates = engineAssoc.xgenAssocField(hld.hld, queryExp, structType, hld.cres.dval, pkval, this); 
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
		for(HLDUpdate hld: stmt.updateL) {
			aliasBuilder.assignAliases(hld);
		}
//		for(HLDInsert hld: stmt.assocInsertL) {
//			aliasBuilder.assignAliasesAssoc(hld);
//		}
		for(AssocBundle bundle: stmt.assocBundleL) {
			if (bundle.hlddelete != null) {
				aliasBuilder.assignAliasesAssoc(bundle.hlddelete);
			}
			if (bundle.hldupdate != null) {
				aliasBuilder.assignAliasesAssoc(bundle.hldupdate);
			}
		}
	}
	public void assignAliases(HLDQueryStatement stmt) {
		HLDAliasBuilder aliasBuilder = new HLDAliasBuilder(aliasMgr);
		aliasBuilder.assignAliases(stmt.hldquery);
	}
	public void assignAliases(HLDDeleteStatement stmt) {
		HLDAliasBuilder aliasBuilder = new HLDAliasBuilder(aliasMgr);
		aliasBuilder.assignAliases(stmt.hlddelete);
	}
	
}