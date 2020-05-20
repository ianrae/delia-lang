package org.delia.zdb.postgres;

import org.delia.core.FactoryService;
import org.delia.db.postgres.PostgresWhereFragmentGenerator;
import org.delia.db.sql.fragment.WhereFragmentGenerator;
import org.delia.runner.VarEvaluator;
import org.delia.type.DTypeRegistry;
import org.delia.zdb.ZUpsert;

public class PostgresZUpsert extends ZUpsert {

	public PostgresZUpsert(FactoryService factorySvc, DTypeRegistry registry) {
		super(factorySvc, registry);
	}

	@Override
	protected WhereFragmentGenerator createWhereFragmentGenerator(VarEvaluator varEvaluator) {
		return new PostgresWhereFragmentGenerator(factorySvc, registry, varEvaluator);
	}

}
