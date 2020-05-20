package org.delia.zdb.postgres;

import org.delia.core.FactoryService;
import org.delia.db.postgres.PostgresWhereFragmentGenerator;
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
	protected WhereFragmentGenerator createWhereFragmentGenerator(VarEvaluator varEvaluator) {
		return new PostgresWhereFragmentGenerator(factorySvc, registry, varEvaluator);
	}

}
