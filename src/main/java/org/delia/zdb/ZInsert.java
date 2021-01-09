package org.delia.zdb;

import org.delia.assoc.DatIdMap;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.DBAccessContext;
import org.delia.db.InsertContext;
import org.delia.db.h2.H2SqlHelperFactory;
import org.delia.db.sql.fragment.FragmentParserService;
import org.delia.db.sql.fragment.InsertFragmentParser;
import org.delia.db.sql.fragment.InsertStatementFragment;
import org.delia.db.sql.fragment.WhereFragmentGenerator;
import org.delia.db.sql.prepared.SqlStatementGroup;
import org.delia.runner.DoNothingVarEvaluator;
import org.delia.runner.VarEvaluator;
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
		WhereFragmentGenerator whereGen = createWhereFragmentGenerator(dbctx.varEvaluator, zexec.getDatIdMap());
		FragmentParserService fpSvc = new FragmentParserService(factorySvc, registry, 
				new DoNothingVarEvaluator(), tableCreator.alreadyCreatedL, dbctx, sqlHelperFactory, whereGen, null);
		ZTableExistenceService existSvc = new ZTableExistenceService();
		fpSvc.setExistSvc(existSvc);
		InsertFragmentParser parser = new InsertFragmentParser(factorySvc, fpSvc, zexec.getDatIdMap(), null);
		String typeName = dval.getType().getName();
		InsertStatementFragment selectFrag = parser.parseInsert(typeName, dval);
		SqlStatementGroup stgroup = parser.renderInsertGroup(selectFrag);
		
		return stgroup;
	}
	protected WhereFragmentGenerator createWhereFragmentGenerator(VarEvaluator varEvaluator, DatIdMap datIdMap) {
		return new WhereFragmentGenerator(factorySvc, registry, varEvaluator, datIdMap);
	}

}
