package org.delia.db.sql.fragment;

import java.util.List;

import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.DBAccessContext;
import org.delia.db.DBInterface;
import org.delia.db.SpanHelper;
import org.delia.db.SqlHelperFactory;
import org.delia.db.TableExistenceService;
import org.delia.db.TableExistenceServiceImpl;
import org.delia.db.sql.QueryTypeDetector;
import org.delia.db.sql.prepared.SelectFuncHelper;
import org.delia.db.sql.table.TableInfo;
import org.delia.queryresponse.LetSpan;
import org.delia.runner.VarEvaluator;
import org.delia.type.DTypeRegistry;

public class FragmentParserService extends ServiceBase {
	public int nextAliasIndex = 0;
	public QueryTypeDetector queryDetectorSvc;
	public DTypeRegistry registry;
	public WhereFragmentGenerator whereGen;
	public SelectFuncHelper selectFnHelper;
	public TableExistenceService existSvc;
	public FKHelper fkHelper;
	public JoinFragment savedJoinedFrag;
	public List<TableInfo> tblinfoL;
	public VarEvaluator varEvaluator;
	public DBInterface dbInterface;
	public DBAccessContext dbctx;
	public SqlHelperFactory sqlHelperFactory;
	public SpanHelper spanHelper;
	
	public FragmentParserService(FactoryService factorySvc, DTypeRegistry registry, VarEvaluator varEvaluator, List<TableInfo> tblinfoL, 
			DBInterface dbInterface, DBAccessContext dbctx, SqlHelperFactory sqlHelperFactory, WhereFragmentGenerator whereGen, List<LetSpan> spanL) {
		super(factorySvc);
		this.registry = registry;
		this.queryDetectorSvc = new QueryTypeDetector(factorySvc, registry);
		this.varEvaluator = varEvaluator;
		this.tblinfoL = tblinfoL;
		this.dbInterface = dbInterface;
		this.dbctx = dbctx;
		this.sqlHelperFactory = sqlHelperFactory;
		this.whereGen = whereGen; 
		this.spanHelper = spanL == null ? null : new SpanHelper(spanL);

		
		this.selectFnHelper = new SelectFuncHelper(factorySvc, registry, spanHelper);
		this.existSvc = new TableExistenceServiceImpl(dbInterface, dbctx);
	}

	public QueryTypeDetector createQueryTypeDetector() {
		this.queryDetectorSvc = new QueryTypeDetector(factorySvc, registry);
		return this.queryDetectorSvc;
	}

	public TableExistenceService createTableExistenceService() {
		return existSvc;
	}

	public FKHelper createFKHelper() {
		return new FKHelper(factorySvc, registry, tblinfoL, sqlHelperFactory, varEvaluator, existSvc, spanHelper);
	}

	public SelectFuncHelper createSelectFuncHelper() {
		return new SelectFuncHelper(factorySvc, registry, spanHelper);
	}

	public void setExistSvc(TableExistenceService existSvc) {
		this.existSvc = existSvc;
	}

}
