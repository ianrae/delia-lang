package org.delia.db.h2;

import java.util.List;

import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.DBAccessContext;
import org.delia.db.DBErrorConverter;
import org.delia.db.ValueHelper;
import org.delia.db.sql.QueryTypeDetector;
import org.delia.db.sql.SimpleSqlNameFormatter;
import org.delia.db.sql.SqlNameFormatter;
import org.delia.db.sql.prepared.FKSqlGenerator;
import org.delia.db.sql.prepared.WhereClauseGenerator;
import org.delia.db.sql.prepared.InsertStatementGenerator;
import org.delia.db.sql.prepared.PreparedStatementGenerator;
import org.delia.db.sql.prepared.SelectFuncHelper;
import org.delia.db.sql.table.FieldGenFactory;
import org.delia.db.sql.table.TableCreator;
import org.delia.db.sql.table.TableInfo;
import org.delia.db.sql.where.SqlWhereConverter;

public class SqlHelperFactory extends ServiceBase {

	public SqlHelperFactory(FactoryService factorySvc) {
		super(factorySvc);
	}
	
	public ValueHelper createValueHelper() {
		return new ValueHelper(factorySvc);
	}
	public FieldGenFactory createFieldGenFactory() {
		return new FieldGenFactory(factorySvc);
	}
	public DBErrorConverter createErrorConverter() {
		return new H2ErrorConverter();
	}
	public PreparedStatementGenerator createPrepSqlGen(DBAccessContext dbctx) {
		PreparedStatementGenerator sqlgen = new PreparedStatementGenerator(factorySvc, dbctx.registry, this, dbctx.varEvaluator);
		return sqlgen;
	}
	public InsertStatementGenerator createPrepInsertSqlGen(DBAccessContext dbctx) {
		SqlNameFormatter nameFormatter = createNameFormatter(dbctx);
		InsertStatementGenerator sqlgen = new InsertStatementGenerator(factorySvc, dbctx.registry, nameFormatter);
		return sqlgen;
	}
	
	public SqlNameFormatter createNameFormatter(DBAccessContext dbctx) {
		SqlNameFormatter nameFormatter = new SimpleSqlNameFormatter();
		return nameFormatter;
	}
	
	public FKSqlGenerator createFKSqlGen(List<TableInfo> tblinfoL, DBAccessContext dbctx) {
		FKSqlGenerator sqlgen = new FKSqlGenerator(factorySvc, dbctx.registry, tblinfoL, this, dbctx.varEvaluator);
		return sqlgen;
	}
	
	public SelectFuncHelper createSelectFuncHelper(DBAccessContext dbctx) {
		SelectFuncHelper sfhelper = new SelectFuncHelper(factorySvc, dbctx.registry);
		return sfhelper;
	}
	
	//why syncrhonized?
	public TableCreator createTableCreator(DBAccessContext dbctx) {
		SqlNameFormatter nameFormatter = createNameFormatter(dbctx); 
		return new TableCreator(factorySvc, dbctx.registry, this.createFieldGenFactory(), nameFormatter);
	}
	
	public QueryTypeDetector createQueryTypeDetector(DBAccessContext dbctx) {
		return new QueryTypeDetector(factorySvc, dbctx.registry);
	}
	
	public SqlWhereConverter createSqlWhereConverter(DBAccessContext dbctx, QueryTypeDetector queryDetectorSvc) {
		return new SqlWhereConverter(factorySvc, dbctx.registry, queryDetectorSvc);
	}

	public WhereClauseGenerator createPWhereGen(DBAccessContext dbctx) {
		return new WhereClauseGenerator(factorySvc, dbctx.registry, dbctx.varEvaluator);
	}
	
}
