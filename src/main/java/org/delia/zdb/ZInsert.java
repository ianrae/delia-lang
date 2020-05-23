package org.delia.zdb;

import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.DBAccessContext;
import org.delia.db.InsertContext;
import org.delia.db.h2.H2SqlHelperFactory;
import org.delia.db.hls.AssocTblManager;
import org.delia.db.sql.fragment.FragmentParserService;
import org.delia.db.sql.fragment.InsertFragmentParser;
import org.delia.db.sql.fragment.InsertStatementFragment;
import org.delia.db.sql.prepared.SqlStatementGroup;
import org.delia.runner.DoNothingVarEvaluator;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.zdb.h2.H2DeliaSessionCache.CacheData;

public class ZInsert extends ServiceBase {
	
	protected DTypeRegistry registry;
	protected H2SqlHelperFactory sqlHelperFactory;

	public ZInsert(FactoryService factorySvc, DTypeRegistry registry) {
		super(factorySvc);
		this.registry = registry;
		this.sqlHelperFactory = new H2SqlHelperFactory(factorySvc);
	}

	public SqlStatementGroup generate(DValue dval, InsertContext ctx, ZTableCreator tableCreator, CacheData cacheData, ZDBExecutor zexec) {
		
		DBAccessContext dbctx = new DBAccessContext(registry, new DoNothingVarEvaluator());
		FragmentParserService fpSvc = new FragmentParserService(factorySvc, registry, 
				new DoNothingVarEvaluator(), tableCreator.alreadyCreatedL, null, dbctx, sqlHelperFactory, null, null);
		ZTableExistenceService existSvc = new ZTableExistenceService(zexec);
		fpSvc.setExistSvc(existSvc);
		AssocTblManager assocTblMgr = new AssocTblManager(zexec.getDatIdMap(), cacheData.assocTblExistMap);
		InsertFragmentParser parser = new InsertFragmentParser(factorySvc, fpSvc, assocTblMgr);
		String typeName = dval.getType().getName();
		InsertStatementFragment selectFrag = parser.parseInsert(typeName, dval);
		SqlStatementGroup stgroup = parser.renderInsertGroup(selectFrag);
		
		return stgroup;
	}
}
