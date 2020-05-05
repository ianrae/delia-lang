package org.delia.db;

import java.util.List;

import org.delia.db.sql.QueryTypeDetector;
import org.delia.db.sql.SqlNameFormatter;
import org.delia.db.sql.prepared.FKSqlGenerator;
import org.delia.db.sql.prepared.InsertStatementGenerator;
import org.delia.db.sql.prepared.PreparedStatementGenerator;
import org.delia.db.sql.prepared.SelectFuncHelper;
import org.delia.db.sql.prepared.WhereClauseGenerator;
import org.delia.db.sql.table.FieldGenFactory;
import org.delia.db.sql.table.TableCreator;
import org.delia.db.sql.table.TableInfo;
import org.delia.db.sql.where.SqlWhereConverter;

public interface SqlHelperFactory {

	void init(DBInterface dbInterface);
	ValueHelper createValueHelper();
	FieldGenFactory createFieldGenFactory();
	DBErrorConverter createErrorConverter();
	PreparedStatementGenerator createPrepSqlGen(TableExistenceService existSvc, DBAccessContext dbctx);
//	InsertStatementGenerator createPrepInsertSqlGen(DBAccessContext dbctx, TableExistenceService existSvc);
	SqlNameFormatter createNameFormatter(DBAccessContext dbctx);
	FKSqlGenerator createFKSqlGen(List<TableInfo> tblinfoL, DBAccessContext dbctx, TableExistenceService existSvc);
	SelectFuncHelper createSelectFuncHelper(DBAccessContext dbctx);
	TableCreator createTableCreator(DBAccessContext dbctx);
	QueryTypeDetector createQueryTypeDetector(DBAccessContext dbctx);
	SqlWhereConverter createSqlWhereConverter(DBAccessContext dbctx, QueryTypeDetector queryDetectorSvc);
	WhereClauseGenerator createPWhereGen(DBAccessContext dbctx);
}
