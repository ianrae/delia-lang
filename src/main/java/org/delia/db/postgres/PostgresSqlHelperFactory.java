package org.delia.db.postgres;

import org.delia.core.FactoryService;
import org.delia.db.DBAccessContext;
import org.delia.db.DBErrorConverter;
import org.delia.db.DBInterface;
import org.delia.db.TableExistenceService;
import org.delia.db.TableExistenceServiceImpl;
import org.delia.db.h2.H2SqlHelperFactory;
import org.delia.db.sql.SimpleSqlNameFormatter;
import org.delia.db.sql.SqlNameFormatter;
import org.delia.db.sql.prepared.PreparedStatementGenerator;
import org.delia.db.sql.prepared.SelectFuncHelper;
import org.delia.db.sql.prepared.WhereClauseGenerator;
import org.delia.db.sql.table.FieldGenFactory;
import org.delia.db.sql.table.TableCreator;

public class PostgresSqlHelperFactory extends H2SqlHelperFactory {

	public PostgresSqlHelperFactory(FactoryService factorySvc) {
		super(factorySvc);
	}

	@Override
	public FieldGenFactory createFieldGenFactory() {
		return new PostgresFieldgenFactory(factorySvc);
	}
	
	@Override
	public SqlNameFormatter createNameFormatter(DBAccessContext dbctx) {
		SqlNameFormatter nameFormatter = new SimpleSqlNameFormatter(true);
		return nameFormatter;
	}
	@Override
	public DBErrorConverter createErrorConverter() {
		return new PostgresErrorConverter(createNameFormatter(null));
	}

	@Override
	public WhereClauseGenerator createPWhereGen(DBAccessContext dbctx) {
		return new PostgresWhereClauseGenerator(factorySvc, dbctx.registry, dbctx.varEvaluator);
	}
	@Override
	public PreparedStatementGenerator createPrepSqlGen(TableExistenceService existSvc, DBAccessContext dbctx) {
		PreparedStatementGenerator sqlgen = new PostgresPreparedStatementGenerator(factorySvc, dbctx.registry, this, dbctx.varEvaluator, existSvc);
		return sqlgen;
	}
	@Override
	public SelectFuncHelper createSelectFuncHelper(DBAccessContext dbctx) {
		SelectFuncHelper sfhelper = new PostgresSelectFuncHelper(factorySvc, dbctx.registry);
		return sfhelper;
	}
//	@Override
//	public InsertStatementGenerator createPrepInsertSqlGen(DBAccessContext dbctx, TableExistenceService existSvc) {
//		SqlNameFormatter nameFormatter = createNameFormatter(dbctx);
//		InsertStatementGenerator sqlgen = new PostgresInsertStatementGenerator(factorySvc, dbctx.registry, nameFormatter, existSvc);
//		return sqlgen;
//	}
	@Override
	public TableCreator createTableCreator(DBAccessContext dbctx) {
		SqlNameFormatter nameFormatter = createNameFormatter(dbctx); 
		TableExistenceService existSvc = new TableExistenceServiceImpl(dbInterface, dbctx);
		return new PostgresTableCreator(factorySvc, dbctx.registry, this.createFieldGenFactory(), nameFormatter, existSvc);
	}

}
