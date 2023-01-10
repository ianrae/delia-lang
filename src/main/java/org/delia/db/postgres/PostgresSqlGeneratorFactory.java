package org.delia.db.postgres;

import org.delia.assoc.DatIdMap;
import org.delia.core.FactoryService;
import org.delia.db.sqlgen.SqlFieldListClause;
import org.delia.db.sqlgen.SqlGeneratorFactoryImpl;
import org.delia.db.sqlgen.SqlMergeAllIntoStatement;
import org.delia.db.sqlgen.SqlMergeIntoStatement;
import org.delia.db.sqlgen.SqlMergeUsingStatement;
import org.delia.db.sqlgen.SqlSelectStatement;
import org.delia.db.sqlgen.SqlTableNameClause;
import org.delia.db.sqlgen.SqlValueListClause;
import org.delia.db.sqlgen.SqlWhereClause;
import org.delia.type.DTypeRegistry;

public class PostgresSqlGeneratorFactory extends SqlGeneratorFactoryImpl {
	
	public PostgresSqlGeneratorFactory(DTypeRegistry registry, FactoryService factorySvc, String defaultSchema) {
		super(registry, factorySvc, defaultSchema);
	}

	@Override
	public SqlMergeAllIntoStatement createMergeAllInto() {
		return new PostgresSqlMergeAllIntoStatement(new SqlTableNameClause(), new SqlValueListClause());
	}
	
	@Override
	public SqlMergeIntoStatement createMergeInto() {
		return new PostgresSqlMergeIntoStatement(new SqlTableNameClause(), new SqlFieldListClause(), new SqlValueListClause());
	}

	@Override
	public SqlMergeUsingStatement createMergeUsing() {
		return new PostgresSqlMergeUsingStatement(new SqlTableNameClause(), new SqlFieldListClause(), new SqlValueListClause());
	}

	@Override
	public SqlSelectStatement createSelect(DatIdMap datIdMap) {
		return new PostgresSqlSelectStatement(registry, factorySvc, datIdMap, new SqlTableNameClause(), new SqlWhereClause(registry, factorySvc));
	}
	
}
