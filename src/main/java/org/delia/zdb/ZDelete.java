package org.delia.zdb;

import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.DBAccessContext;
import org.delia.db.QueryDetails;
import org.delia.db.QuerySpec;
import org.delia.db.h2.H2SqlHelperFactory;
import org.delia.db.sql.fragment.DeleteFragmentParser;
import org.delia.db.sql.fragment.DeleteStatementFragment;
import org.delia.db.sql.fragment.FragmentParserService;
import org.delia.db.sql.fragment.WhereFragmentGenerator;
import org.delia.db.sql.prepared.SqlStatement;
import org.delia.db.sql.prepared.SqlStatementGroup;
import org.delia.runner.DoNothingVarEvaluator;
import org.delia.runner.VarEvaluator;
import org.delia.type.DTypeRegistry;

public class ZDelete extends ServiceBase {

	private DTypeRegistry registry;
	private H2SqlHelperFactory sqlHelperFactory;

	public ZDelete(FactoryService factorySvc, DTypeRegistry registry) {
		super(factorySvc);
		this.registry = registry;
		this.sqlHelperFactory = new H2SqlHelperFactory(factorySvc);
	}

	public SqlStatementGroup generate(QuerySpec spec, VarEvaluator varEvaluator, ZTableCreator tableCreator, ZDBExecutor zexec) {
		SqlStatementGroup stgroup = new SqlStatementGroup();

		WhereFragmentGenerator whereGen = new WhereFragmentGenerator(factorySvc, registry, varEvaluator);
		DBAccessContext dbctx = new DBAccessContext(registry, varEvaluator);
		FragmentParserService fpSvc = new FragmentParserService(factorySvc, registry, 
				new DoNothingVarEvaluator(), tableCreator.alreadyCreatedL, null, dbctx, sqlHelperFactory, whereGen, null);
		ZTableExistenceService existSvc = new ZTableExistenceService(zexec);
		fpSvc.setExistSvc(existSvc);

		DeleteFragmentParser parser = new DeleteFragmentParser(factorySvc, fpSvc);
		whereGen.tableFragmentMaker = parser;
		QueryDetails details = new QueryDetails();
		DeleteStatementFragment selectFrag = parser.parseDelete(spec, details);
		parser.renderDelete(selectFrag);
		SqlStatement statement = selectFrag.statement;
		stgroup.statementL.add(statement);
		return stgroup;
	}	
}
