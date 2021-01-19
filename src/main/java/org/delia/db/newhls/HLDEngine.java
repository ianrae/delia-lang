package org.delia.db.newhls;

import java.util.List;

import org.delia.assoc.DatIdMap;
import org.delia.compiler.ast.InsertStatementExp;
import org.delia.compiler.ast.QueryExp;
import org.delia.compiler.ast.UpdateStatementExp;
import org.delia.compiler.ast.UpsertStatementExp;
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
import org.delia.db.newhls.cud.HLDUpsert;
import org.delia.db.newhls.simple.SimpleBase;
import org.delia.log.Log;
import org.delia.runner.DValueIterator;
import org.delia.runner.VarEvaluator;
import org.delia.sprig.SprigService;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
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
public class HLDEngine extends HLDEngineBase implements HLDQueryBuilderAdapter {
	private HLDAliasManager aliasMgr;
	private DValueIterator insertPrebuiltValueIterator;

	public HLDEngine(DTypeRegistry registry, FactoryService factorySvc, Log log, DatIdMap datIdMap, SprigService sprigSvc) {
		super(registry, factorySvc, log,datIdMap, sprigSvc);
		this.aliasMgr = new HLDAliasManager(factorySvc, datIdMap);
	}
	
	@Override
	public HLDQuery buildQuery(QueryExp queryExp) {
		HLDAliasManager aliasMgr = new HLDAliasManager(factorySvc, datIdMap);
		return buildQuery(queryExp, aliasMgr, true, null);
	}
	@Override
	public HLDQuery buildQueryEx(QueryExp queryExp, DStructType structType) {
		HLDAliasManager aliasMgr = new HLDAliasManager(factorySvc, datIdMap);
		return buildQuery(queryExp, aliasMgr, true, structType);
	}
	public HLDQuery buildQuery(QueryExp queryExp, HLDAliasManager aliasMgr) {
		return buildQuery(queryExp, aliasMgr, true, null);
	}
	public HLDQuery buildQuery(QueryExp queryExp, HLDAliasManager aliasMgr, boolean doEverything, DStructType structType) {
		HLDQueryBuilder hldBuilder = new HLDQueryBuilder(registry);

		HLDQuery hld = hldBuilder.build(queryExp, structType, varEvaluator);

		if (doEverything) {
			JoinTreeBuilder joinBuilder = new JoinTreeBuilder();
			joinBuilder.generateJoinTree(hld);

			HLDFieldBuilder fieldBuilder = new HLDFieldBuilder();
			fieldBuilder.generateFields(hld);
			
//			HLDAliasBuilder aliasBuilder = new HLDAliasBuilder(aliasMgr);
//			aliasBuilder.assignAliases(hld);
		}
		
		return hld;
	}
	
	public HLDDelete buildDelete(QueryExp queryExp) {
		HLDQuery hld = buildQuery(queryExp);
		HLDDelete hlddel = new HLDDelete(hld);
		return hlddel;
	}
	public void addParentStatementsForDelete(HLDDelete hld, List<SimpleBase> moreL) {
		//Note. the dson body of update doesn't have pk, so we need to get it from the filter
		DValue pkval = getUpdatePK(hld.hld); 
		generateParentUpdateForDelete(hld.getStructType(), pkval, hld.hld, moreL);
		generateParentDeleteForDelete(hld.getStructType(), pkval, hld.hld, moreL, this);
	}
	
	
	public HLDInsert buildInsert(InsertStatementExp insertExp) {
		HLDDsonBuilder hldBuilder = createDsonBuilder(); 
		HLDInsert hld = hldBuilder.buildInsert(insertExp);
		return hld;
	}
	private HLDDsonBuilder createDsonBuilder() {
		HLDDsonBuilder hldBuilder = new HLDDsonBuilder(registry, factorySvc, log, sprigSvc, varEvaluator);
		hldBuilder.setInsertPrebuiltValueIterator(insertPrebuiltValueIterator);
		return hldBuilder;
	}

	public void addParentUpdates(HLDInsert hld, List<SimpleBase> moreL) {
		DStructType structType = hld.getStructType();
		generateParentUpdateIfNeeded(structType, hld.cres.dval, null, moreL);
	}
	public void addParentUpdatesForUpdate(HLDUpdate hld, List<SimpleBase> moreL) {
		DStructType structType = hld.getStructType();
		
		//Note. the dson body of update doesn't have pk, so we need to get it from the filter
		DValue pkval = getUpdatePK(hld.hld); 
		generateParentUpdateIfNeeded(structType, hld.cres.dval, pkval, moreL);
	}
	private DValue getUpdatePK(HLDQuery hld) {
		//Note. the dson body of update doesn't have pk, so we need to get it from the filter
		SqlParamGenerator pgen = new SqlParamGenerator(registry, factorySvc);
		if (hld.filter instanceof SingleFilterCond) {
			SingleFilterCond sfc = (SingleFilterCond) hld.filter;
			DValue pkval = pgen.convert(sfc.val1);
			return pkval;
		} else {
			return null;
		}
	}

	public void addAssocInserts(HLDInsert hld, List<SimpleBase> moreL) {
		DStructType structType = hld.getStructType();
		 generateAssocInsertsIfNeeded(structType, hld.cres.dval, moreL);
	}
	public List<HLDInsert> addAssocInsertsForUpdate(HLDUpdate hld, List<SimpleBase> moreL) {
		DStructType structType = hld.getStructType();
		List<HLDInsert> parentUpdates = generateAssocInsertsIfNeeded(structType, hld.cres.dval, moreL);
		return parentUpdates;
	}
	public List<AssocBundle> addMoreAssoc(HLDUpdate hld, HLDEngineAssoc engineAssoc, QueryExp queryExp, List<SimpleBase> moreL) {
		DStructType structType = hld.getStructType();
		//Note. the dson body of update doesn't have pk, so we need to get it from the filter
		DValue pkval = getUpdatePK(hld.hld); 
		HLDDsonBuilder hldBuilder = createDsonBuilder(); 
		List<AssocBundle> parentUpdates = engineAssoc.xgenAssocField(hld, queryExp, structType, hld.cres.dval, pkval, this, hldBuilder, moreL); 
		return parentUpdates;
	}

	public HLDUpdate buildUpdate(UpdateStatementExp updateExp) {
		HLDDsonBuilder hldBuilder = createDsonBuilder(); 
		HLDUpdate hld = hldBuilder.buildUpdate(updateExp);
		return doBuildUpdate(hld, updateExp.queryExp);
	}
	public HLDUpsert buildUpsert(UpsertStatementExp upsertExp) {
		HLDDsonBuilder hldBuilder = createDsonBuilder(); 
		HLDUpsert hld = hldBuilder.buildUpsert(upsertExp);
		doBuildUpdate(hld, upsertExp.queryExp);
		
		//the filter for upsert must not be [true].
		//other filters are allowed as long as they result in only a single row
		//TODO: enforce single row in db layer
		if (hld.hld.isAllQuery()) {
			DeliaExceptionHelper.throwError("upsert-filter-error", "[true] filter not allowed for upsert: %s", upsertExp.typeName);  
		} else if (isPKQueryOrUniqueQuery(hld)) {
			DeliaExceptionHelper.throwError("upsert-filter-error", "upsert filter cannot contain a serial primary key: %s", upsertExp.typeName);  
		}
		
		return hld;
	}
	
	private boolean isPKQueryOrUniqueQuery(HLDUpsert hld) {
		if (hld.hld.filter instanceof SingleFilterCond) {
			DStructType dtype = hld.hld.fromType;
			String pkfield = DValueHelper.findPrimaryKeyFieldPair(dtype).name;
			if (dtype.fieldIsSerial(pkfield)) {
				return true;
			}
		} 
		return false;
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
		HLDAliasBuilder aliasBuilder = createAliasBuilder();
		//H2 doesn't like tbl alias so we won't use any aliases for INSERT
		aliasBuilder.setOutputAliases(false);
		aliasBuilder.assignAliases(stmt.hldinsert);
		for(SimpleBase simple: stmt.moreL) {
			aliasBuilder.assignAliases(simple);
		}
	}
	public void assignAliases(HLDUpdateStatement stmt) {
		HLDAliasBuilder aliasBuilder = createAliasBuilder();
		aliasBuilder.assignAliases(stmt.hldupdate);
//		for(HLDUpdate hld: stmt.updateL) {
//			aliasBuilder.assignAliases(hld);
//		}
		for(SimpleBase simple: stmt.moreL) {
			aliasBuilder.assignAliases(simple);
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
		HLDAliasBuilder aliasBuilder = createAliasBuilder();
		aliasBuilder.assignAliases(stmt.hldquery);
	}
	public void assignAliases(HLDDeleteStatement stmt) {
		HLDAliasBuilder aliasBuilder = createAliasBuilder(); 
		aliasBuilder.assignAliases(stmt.hlddelete);
		for(SimpleBase simple: stmt.moreL) {
			aliasBuilder.assignAliases(simple);
		}
	}

	private HLDAliasBuilder createAliasBuilder() {
		ConversionHelper helper = new ConversionHelper(registry, factorySvc);
		HLDAliasBuilder aliasBuilder = new HLDAliasBuilder(aliasMgr, helper);
		return aliasBuilder;
	}

	public VarEvaluator getVarEvaluator() {
		return varEvaluator;
	}

	public void setVarEvaluator(VarEvaluator varEvaluator) {
		this.varEvaluator = varEvaluator;
	}

	public void setInsertPrebuiltValueIterator(DValueIterator insertPrebuiltValueIterator) {
		this.insertPrebuiltValueIterator = insertPrebuiltValueIterator;
	}
}