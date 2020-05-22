package org.delia.db;

import org.delia.db.sql.QueryTypeDetector;
import org.delia.db.sql.SqlNameFormatter;
import org.delia.db.sql.prepared.SelectFuncHelper;
import org.delia.db.sql.where.SqlWhereConverter;

public interface SqlHelperFactory {

	ValueHelper createValueHelper();
	DBErrorConverter createErrorConverter();
	SqlNameFormatter createNameFormatter();
	SelectFuncHelper createSelectFuncHelper(DBAccessContext dbctx, SpanHelper spanHelper);
	QueryTypeDetector createQueryTypeDetector(DBAccessContext dbctx);
	SqlWhereConverter createSqlWhereConverter(DBAccessContext dbctx, QueryTypeDetector queryDetectorSvc);
}
