package org.delia.db.postgres;

import org.delia.core.FactoryService;
import org.delia.db.DBAccessContext;
import org.delia.db.DBErrorConverter;
import org.delia.db.h2.SqlHelperFactory;
import org.delia.db.sql.SimpleSqlNameFormatter;
import org.delia.db.sql.SqlNameFormatter;
import org.delia.db.sql.prepared.InsertStatementGenerator;
import org.delia.db.sql.prepared.PreparedStatementGenerator;
import org.delia.db.sql.prepared.SelectFuncHelper;
import org.delia.db.sql.prepared.WhereClauseGenerator;
import org.delia.db.sql.table.FieldGenFactory;
import org.delia.db.sql.table.TableCreator;

public class PostgresSqlHelperFactory extends SqlHelperFactory {

	public PostgresSqlHelperFactory(FactoryService factorySvc) {
		super(factorySvc);
	}

	public FieldGenFactory createFieldGenFactory() {
		return new PostgresFieldgenFactory(factorySvc);
	}
	
	public SqlNameFormatter createNameFormatter(DBAccessContext dbctx) {
		SqlNameFormatter nameFormatter = new SimpleSqlNameFormatter(true);
		return nameFormatter;
	}
	public DBErrorConverter createErrorConverter() {
		return new PostgresErrorConverter(createNameFormatter(null));
	}

	public WhereClauseGenerator createPWhereGen(DBAccessContext dbctx) {
		return new PostgresWhereClauseGenerator(factorySvc, dbctx.registry, dbctx.varEvaluator);
	}
	public PreparedStatementGenerator createPrepSqlGen(DBAccessContext dbctx) {
		PreparedStatementGenerator sqlgen = new PostgresPreparedStatementGenerator(factorySvc, dbctx.registry, this, dbctx.varEvaluator);
		return sqlgen;
	}
	public SelectFuncHelper createSelectFuncHelper(DBAccessContext dbctx) {
		SelectFuncHelper sfhelper = new PostgresSelectFuncHelper(factorySvc, dbctx.registry);
		return sfhelper;
	}
	public InsertStatementGenerator createPrepInsertSqlGen(DBAccessContext dbctx) {
		SqlNameFormatter nameFormatter = createNameFormatter(dbctx);
		InsertStatementGenerator sqlgen = new PostgresInsertStatementGenerator(factorySvc, dbctx.registry, nameFormatter);
		return sqlgen;
	}
	public TableCreator createTableCreator(DBAccessContext dbctx) {
		SqlNameFormatter nameFormatter = createNameFormatter(dbctx); 
		return new PostgresTableCreator(factorySvc, dbctx.registry, this.createFieldGenFactory(), nameFormatter);
	}


}
