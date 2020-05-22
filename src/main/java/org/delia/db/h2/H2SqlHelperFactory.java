package org.delia.db.h2;

import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.DBAccessContext;
import org.delia.db.DBErrorConverter;
import org.delia.db.SpanHelper;
import org.delia.db.SqlHelperFactory;
import org.delia.db.ValueHelper;
import org.delia.db.sql.QueryTypeDetector;
import org.delia.db.sql.SimpleSqlNameFormatter;
import org.delia.db.sql.SqlNameFormatter;
import org.delia.db.sql.prepared.SelectFuncHelper;
import org.delia.db.sql.where.SqlWhereConverter;

public class H2SqlHelperFactory extends ServiceBase implements SqlHelperFactory {

	public H2SqlHelperFactory(FactoryService factorySvc) {
		super(factorySvc);
	}
	
	@Override
	public ValueHelper createValueHelper() {
		return new ValueHelper(factorySvc);
	}
	@Override
	public DBErrorConverter createErrorConverter() {
		return new H2ErrorConverter();
	}
	
	@Override
	public SqlNameFormatter createNameFormatter() {
		SqlNameFormatter nameFormatter = new SimpleSqlNameFormatter();
		return nameFormatter;
	}
	
	@Override
	public SelectFuncHelper createSelectFuncHelper(DBAccessContext dbctx, SpanHelper spanHelper) {
		SelectFuncHelper sfhelper = new SelectFuncHelper(factorySvc, dbctx.registry, spanHelper);
		return sfhelper;
	}
	
	@Override
	public QueryTypeDetector createQueryTypeDetector(DBAccessContext dbctx) {
		return new QueryTypeDetector(factorySvc, dbctx.registry);
	}
	
	@Override
	public SqlWhereConverter createSqlWhereConverter(DBAccessContext dbctx, QueryTypeDetector queryDetectorSvc) {
		return new SqlWhereConverter(factorySvc, dbctx.registry, queryDetectorSvc);
	}
	
}
