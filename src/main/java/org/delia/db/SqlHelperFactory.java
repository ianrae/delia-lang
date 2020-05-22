package org.delia.db;

import org.delia.db.sql.QueryTypeDetector;
import org.delia.db.sql.SqlNameFormatter;
import org.delia.db.sql.prepared.SelectFuncHelper;
import org.delia.db.sql.where.SqlWhereConverter;

public interface SqlHelperFactory {

	ValueHelper createValueHelper();
//	FieldGenFactory createFieldGenFactory();
	DBErrorConverter createErrorConverter();
//	PreparedStatementGenerator createPrepSqlGen(TableExistenceService existSvc, DBAccessContext dbctx);
//	InsertStatementGenerator createPrepInsertSqlGen(DBAccessContext dbctx, TableExistenceService existSvc);
	SqlNameFormatter createNameFormatter(DBAccessContext dbctx);
//	FKSqlGenerator createFKSqlGen(List<TableInfo> tblinfoL, DBAccessContext dbctx, TableExistenceService existSvc);
	SelectFuncHelper createSelectFuncHelper(DBAccessContext dbctx, SpanHelper spanHelper);
//	TableCreator createTableCreator(DBAccessContext dbctx, DatIdMap datIdMap);
	QueryTypeDetector createQueryTypeDetector(DBAccessContext dbctx);
	SqlWhereConverter createSqlWhereConverter(DBAccessContext dbctx, QueryTypeDetector queryDetectorSvc);
//	WhereClauseGenerator createPWhereGen(DBAccessContext dbctx);
}
