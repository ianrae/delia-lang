package org.delia.db.h2;

import org.delia.assoc.DatIdMap;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.DBAccessContext;
import org.delia.db.DBErrorConverter;
import org.delia.db.DBInterface;
import org.delia.db.SpanHelper;
import org.delia.db.SqlHelperFactory;
import org.delia.db.TableExistenceService;
import org.delia.db.TableExistenceServiceImpl;
import org.delia.db.ValueHelper;
import org.delia.db.sql.QueryTypeDetector;
import org.delia.db.sql.SimpleSqlNameFormatter;
import org.delia.db.sql.SqlNameFormatter;
import org.delia.db.sql.prepared.PreparedStatementGenerator;
import org.delia.db.sql.prepared.SelectFuncHelper;
import org.delia.db.sql.table.FieldGenFactory;
import org.delia.db.sql.table.TableCreator;
import org.delia.db.sql.where.SqlWhereConverter;

public class H2SqlHelperFactory extends ServiceBase implements SqlHelperFactory {

	protected DBInterface dbInterface;

	public H2SqlHelperFactory(FactoryService factorySvc) {
		super(factorySvc);
	}
	
	@Override
	public void init(DBInterface dbInterface) {
		this.dbInterface = dbInterface;
	}
	
	@Override
	public ValueHelper createValueHelper() {
		return new ValueHelper(factorySvc);
	}
	@Override
	public FieldGenFactory createFieldGenFactory() {
		return new FieldGenFactory(factorySvc);
	}
	@Override
	public DBErrorConverter createErrorConverter() {
		return new H2ErrorConverter();
	}
	@Override
	public PreparedStatementGenerator createPrepSqlGen(TableExistenceService existSvc, DBAccessContext dbctx) {
		PreparedStatementGenerator sqlgen = new PreparedStatementGenerator(factorySvc, dbctx.registry, this, dbctx.varEvaluator, existSvc);
		return sqlgen;
	}
//	@Override
//	public InsertStatementGenerator createPrepInsertSqlGen(DBAccessContext dbctx, TableExistenceService existSvc) {
//		SqlNameFormatter nameFormatter = createNameFormatter(dbctx);
//		InsertStatementGenerator sqlgen = new InsertStatementGenerator(factorySvc, dbctx.registry, nameFormatter, existSvc);
//		return sqlgen;
//	}
	
	@Override
	public SqlNameFormatter createNameFormatter(DBAccessContext dbctx) {
		SqlNameFormatter nameFormatter = new SimpleSqlNameFormatter();
		return nameFormatter;
	}
	
//	@Override
//	public FKSqlGenerator createFKSqlGen(List<TableInfo> tblinfoL, DBAccessContext dbctx, TableExistenceService existSvc) {
//		FKSqlGenerator sqlgen = new FKSqlGenerator(factorySvc, dbctx.registry, tblinfoL, this, dbctx.varEvaluator, existSvc);
//		return sqlgen;
//	}
	
	@Override
	public SelectFuncHelper createSelectFuncHelper(DBAccessContext dbctx, SpanHelper spanHelper) {
		SelectFuncHelper sfhelper = new SelectFuncHelper(factorySvc, dbctx.registry, spanHelper);
		return sfhelper;
	}
	
	//why syncrhonized?
	@Override
	public TableCreator createTableCreator(DBAccessContext dbctx, DatIdMap datIdMap) {
		SqlNameFormatter nameFormatter = createNameFormatter(dbctx); 
		TableExistenceService existSvc =  null; //TODOfixnew TableExistenceServiceImpl(dbInterface, dbctx);
		return new TableCreator(factorySvc, dbctx.registry, this.createFieldGenFactory(), nameFormatter, existSvc, datIdMap);
	}
	
	@Override
	public QueryTypeDetector createQueryTypeDetector(DBAccessContext dbctx) {
		return new QueryTypeDetector(factorySvc, dbctx.registry);
	}
	
	@Override
	public SqlWhereConverter createSqlWhereConverter(DBAccessContext dbctx, QueryTypeDetector queryDetectorSvc) {
		return new SqlWhereConverter(factorySvc, dbctx.registry, queryDetectorSvc);
	}
//
//	@Override
//	public WhereClauseGenerator createPWhereGen(DBAccessContext dbctx) {
//		return new WhereClauseGenerator(factorySvc, dbctx.registry, dbctx.varEvaluator);
//	}
	
}
