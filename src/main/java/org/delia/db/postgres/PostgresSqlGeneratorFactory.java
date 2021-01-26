package org.delia.db.postgres;

import org.delia.core.FactoryService;
import org.delia.db.sqlgen.SqlGeneratorFactoryImpl;
import org.delia.db.sqlgen.SqlMergeIntoStatement;
import org.delia.db.sqlgen.SqlTableNameClause;
import org.delia.db.sqlgen.SqlValueListClause;
import org.delia.type.DTypeRegistry;

public class PostgresSqlGeneratorFactory extends SqlGeneratorFactoryImpl {
	
	public PostgresSqlGeneratorFactory(DTypeRegistry registry, FactoryService factorySvc) {
		super(registry, factorySvc);
	}
	
	@Override
	public SqlMergeIntoStatement createMergeInto() {
		return new PostgresSqlMergeIntoStatement(new SqlTableNameClause(), new SqlValueListClause());
	}
	
}
