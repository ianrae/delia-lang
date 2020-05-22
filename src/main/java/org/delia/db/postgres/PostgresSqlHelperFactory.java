package org.delia.db.postgres;

import org.delia.core.FactoryService;
import org.delia.db.DBErrorConverter;
import org.delia.db.h2.H2SqlHelperFactory;
import org.delia.db.sql.SimpleSqlNameFormatter;
import org.delia.db.sql.SqlNameFormatter;

public class PostgresSqlHelperFactory extends H2SqlHelperFactory {

	public PostgresSqlHelperFactory(FactoryService factorySvc) {
		super(factorySvc);
	}
	
	@Override
	public SqlNameFormatter createNameFormatter() {
		SqlNameFormatter nameFormatter = new SimpleSqlNameFormatter(true);
		return nameFormatter;
	}
	@Override
	public DBErrorConverter createErrorConverter() {
		return new PostgresErrorConverter(createNameFormatter());
	}

}
