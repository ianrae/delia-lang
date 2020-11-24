package org.delia.zdb.postgres;

import org.delia.assoc.DatIdMap;
import org.delia.core.FactoryService;
import org.delia.db.postgres.PostgresAssocTablerReplacer;
import org.delia.db.postgres.PostgresWhereFragmentGenerator;
import org.delia.db.sql.fragment.AssocTableReplacer;
import org.delia.db.sql.fragment.FragmentParserService;
import org.delia.db.sql.fragment.UpdateFragmentParser;
import org.delia.db.sql.fragment.WhereFragmentGenerator;
import org.delia.runner.VarEvaluator;
import org.delia.type.DTypeRegistry;
import org.delia.zdb.ZUpdate;

public class PostgresZUpdate extends ZUpdate {

	public PostgresZUpdate(FactoryService factorySvc, DTypeRegistry registry) {
		super(factorySvc, registry);
	}
	
	@Override
	protected void adjustParser(UpdateFragmentParser parser) {
		parser.useAliases(false); //doesn't like alias in update statements
	}

	@Override
	protected AssocTableReplacer createAssocTableReplacer(FragmentParserService fpSvc) {
		return new PostgresAssocTablerReplacer(factorySvc, fpSvc);
	}

	@Override
	protected WhereFragmentGenerator createWhereFragmentGenerator(VarEvaluator varEvaluator, DatIdMap datIdMap) {
		return new PostgresWhereFragmentGenerator(factorySvc, registry, varEvaluator, datIdMap);
	}

}
