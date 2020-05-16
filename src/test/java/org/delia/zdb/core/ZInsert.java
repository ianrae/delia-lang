package org.delia.zdb.core;

import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.DBAccessContext;
import org.delia.db.InsertContext;
import org.delia.db.h2.H2SqlHelperFactory;
import org.delia.db.sql.fragment.FragmentParserService;
import org.delia.db.sql.fragment.InsertFragmentParser;
import org.delia.db.sql.fragment.InsertStatementFragment;
import org.delia.db.sql.prepared.SqlStatementGroup;
import org.delia.runner.DoNothingVarEvaluator;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;

public class ZInsert extends ServiceBase {
	
	private DTypeRegistry registry;
	private H2SqlHelperFactory sqlHelperFactory;

	public ZInsert(FactoryService factorySvc, DTypeRegistry registry) {
		super(factorySvc);
		this.registry = registry;
		this.sqlHelperFactory = new H2SqlHelperFactory(factorySvc);
	}

	public SqlStatementGroup generate(DValue dval, InsertContext ctx, ZTableCreator tableCreator) {
		
		DBAccessContext dbctx = new DBAccessContext(registry, new DoNothingVarEvaluator());
		FragmentParserService fpSvc = new FragmentParserService(factorySvc, registry, 
				new DoNothingVarEvaluator(), tableCreator.alreadyCreatedL, null, dbctx, sqlHelperFactory, null, null);
		InsertFragmentParser parser = new InsertFragmentParser(factorySvc, fpSvc);
		String typeName = dval.getType().getName();
		InsertStatementFragment selectFrag = parser.parseInsert(typeName, dval);
		SqlStatementGroup stgroup = parser.renderInsertGroup(selectFrag);
		
		return stgroup;
	}
}
