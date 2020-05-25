package org.delia.zdb;

import java.util.Map;

import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.DBAccessContext;
import org.delia.db.QueryDetails;
import org.delia.db.QuerySpec;
import org.delia.db.h2.H2SqlHelperFactory;
import org.delia.db.sql.fragment.AssocTableReplacer;
import org.delia.db.sql.fragment.FragmentParserService;
import org.delia.db.sql.fragment.UpdateFragmentParser;
import org.delia.db.sql.fragment.UpdateStatementFragment;
import org.delia.db.sql.fragment.WhereFragmentGenerator;
import org.delia.db.sql.prepared.SqlStatementGroup;
import org.delia.runner.DoNothingVarEvaluator;
import org.delia.runner.VarEvaluator;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;

public class ZUpdate extends ServiceBase {
	
	protected DTypeRegistry registry;
	protected H2SqlHelperFactory sqlHelperFactory;

	public ZUpdate(FactoryService factorySvc, DTypeRegistry registry) {
		super(factorySvc);
		this.registry = registry;
		this.sqlHelperFactory = new H2SqlHelperFactory(factorySvc);
	}

	public SqlStatementGroup generate(QuerySpec spec, DValue dvalPartial, Map<String, String> assocCrudMap,
			VarEvaluator varEvaluator, ZTableCreator tableCreator, ZDBExecutor zexec) {
		SqlStatementGroup stgroup;
		
		WhereFragmentGenerator whereGen = createWhereFragmentGenerator(varEvaluator);
		DBAccessContext dbctx = new DBAccessContext(registry, varEvaluator);
		FragmentParserService fpSvc = new FragmentParserService(factorySvc, registry, 
				new DoNothingVarEvaluator(), tableCreator.alreadyCreatedL, null, dbctx, sqlHelperFactory, whereGen, null);
		ZTableExistenceService existSvc = new ZTableExistenceService();
		fpSvc.setExistSvc(existSvc);
		
		AssocTableReplacer assocTblReplacer = createAssocTableReplacer(fpSvc);
		UpdateFragmentParser parser = new UpdateFragmentParser(factorySvc, fpSvc, assocTblReplacer);
		whereGen.tableFragmentMaker = parser;
		adjustParser(parser);
		QueryDetails details = new QueryDetails();
		UpdateStatementFragment updateFrag = parser.parseUpdate(spec, details, dvalPartial, assocCrudMap);
		stgroup = parser.renderUpdateGroup(updateFrag);
		return stgroup;
	}	
	
	protected void adjustParser(UpdateFragmentParser parser) {
	}
	
	protected AssocTableReplacer createAssocTableReplacer(FragmentParserService fpSvc) {
		return new AssocTableReplacer(factorySvc, fpSvc);
	}

	protected WhereFragmentGenerator createWhereFragmentGenerator(VarEvaluator varEvaluator) {
		return new WhereFragmentGenerator(factorySvc, registry, varEvaluator);
	}
}
