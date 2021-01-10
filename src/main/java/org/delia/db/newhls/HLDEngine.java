package org.delia.db.newhls;

import org.delia.assoc.DatIdMap;
import org.delia.compiler.ast.QueryExp;
import org.delia.core.FactoryService;
import org.delia.log.Log;
import org.delia.sprig.SprigService;
import org.delia.type.DTypeRegistry;

/**
 * Generates the lower-level HLD objects such as HLDQuery,HLDInsert,etc
 * The HLD statement classes contain one or more of these lower-level objects
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

	public HLDEngine(DTypeRegistry registry, FactoryService factorySvc, Log log, DatIdMap datIdMap, SprigService sprigSvc) {
		this.registry = registry;
		this.factorySvc = factorySvc;
		this.datIdMap = datIdMap;
		this.log = log;
		this.sprigSvc = sprigSvc;
	}
	
	public HLDQuery fullBuildQuery(QueryExp queryExp) {
		HLDAliasManager aliasMgr = new HLDAliasManager(factorySvc, datIdMap);
		return fullBuildQuery(queryExp, aliasMgr);
	}
	public HLDQuery fullBuildQuery(QueryExp queryExp, HLDAliasManager aliasMgr) {
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
//	public HLDDelete fullBuildDelete(QueryExp queryExp) {
//		HLDQueryStatement hld = fullBuildQuery(queryExp);
//		HLDDelete hlddel = new HLDDelete(hld.hldquery);
//		return hlddel;
//	}
//	public HLDInsert fullBuildInsert(InsertStatementExp insertExp) {
//		HLDDsonBuilder hldBuilder = new HLDDsonBuilder(registry, factorySvc, log, sprigSvc);
//		HLDInsert hld = hldBuilder.buildInsert(insertExp);
//		return hld;
//	}
//	public HLDUpdate fullBuildUpdate(UpdateStatementExp updateExp) {
//		HLDDsonBuilder hldBuilder = new HLDDsonBuilder(registry, factorySvc, log, sprigSvc);
//		HLDUpdate hld = hldBuilder.buildUpdate(updateExp);
//		hld.hld = fullBuildQuery(updateExp.queryExp);
//		hld.querySpec = new QuerySpec();
//		hld.querySpec.evaluator = null; //TOOD fix
//		hld.querySpec.queryExp = updateExp.queryExp;
//		return hld;
//	}
	
}