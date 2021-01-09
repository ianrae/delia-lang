package org.delia.db.newhls;

import org.delia.assoc.DatIdMap;
import org.delia.core.FactoryService;
import org.delia.db.DBAccessContext;
import org.delia.db.sql.fragment.InsertFragmentParser;
import org.delia.db.sql.fragment.InsertStatementFragment;
import org.delia.db.sql.prepared.SqlStatementGroup;
import org.delia.runner.DoNothingVarEvaluator;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;

public class HLDInsertSQLGenerator {
	private DTypeRegistry registry;
	private FactoryService factorySvc;
	private DatIdMap datIdMap;
	private SqlColumnBuilder columnBuilder;

	public HLDInsertSQLGenerator(DTypeRegistry registry, FactoryService factorySvc, DatIdMap datIdMap) {
		this.registry = registry;
		this.factorySvc = factorySvc;
		this.datIdMap = datIdMap;
		this.columnBuilder = new SqlColumnBuilder(datIdMap);
	}

	
	public SqlStatementGroup generate(DValue dval) {
		
//		DBAccessContext dbctx = new DBAccessContext(registry, new DoNothingVarEvaluator());
//		WhereFragmentGenerator whereGen = createWhereFragmentGenerator(dbctx.varEvaluator, zexec.getDatIdMap());
//		FragmentParserService fpSvc = new FragmentParserService(factorySvc, registry, 
//				new DoNothingVarEvaluator(), tableCreator.alreadyCreatedL, dbctx, sqlHelperFactory, whereGen, null);
//		ZTableExistenceService existSvc = new ZTableExistenceService();
//		fpSvc.setExistSvc(existSvc);
		InsertFragmentParser parser = new InsertFragmentParser(factorySvc, null, datIdMap, registry);
		String typeName = dval.getType().getName();
		InsertStatementFragment selectFrag = parser.parseInsert(typeName, dval);
		SqlStatementGroup stgroup = parser.renderInsertGroup(selectFrag);
		
		return stgroup;
	}
	
}
