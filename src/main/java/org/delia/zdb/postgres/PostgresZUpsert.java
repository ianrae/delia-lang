package org.delia.zdb.postgres;

import org.delia.core.FactoryService;
import org.delia.db.postgres.PostgresAssocTablerReplacer;
import org.delia.db.postgres.PostgresUpsertFragmentParser;
import org.delia.db.postgres.PostgresWhereFragmentGenerator;
import org.delia.db.sql.fragment.AssocTableReplacer;
import org.delia.db.sql.fragment.FragmentParserService;
import org.delia.db.sql.fragment.UpsertFragmentParser;
import org.delia.db.sql.fragment.WhereFragmentGenerator;
import org.delia.runner.VarEvaluator;
import org.delia.type.DTypeRegistry;
import org.delia.zdb.ZUpsert;

public class PostgresZUpsert extends ZUpsert {

	public PostgresZUpsert(FactoryService factorySvc, DTypeRegistry registry) {
		super(factorySvc, registry);
	}

	@Override
	protected AssocTableReplacer createAssocTableReplacer(FragmentParserService fpSvc) {
		return new PostgresAssocTablerReplacer(factorySvc, fpSvc);
	}

	@Override
	protected WhereFragmentGenerator createWhereFragmentGenerator(VarEvaluator varEvaluator) {
		return new PostgresWhereFragmentGenerator(factorySvc, registry, varEvaluator);
	}

	@Override
	protected UpsertFragmentParser createUpsertFragmentParser(FragmentParserService fpSvc, AssocTableReplacer assocTblReplacer) {
		return new PostgresUpsertFragmentParser(factorySvc, fpSvc, assocTblReplacer);
	}
}