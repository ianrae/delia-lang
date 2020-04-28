package org.delia.db.sql.fragment;

import java.util.List;

import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.DBAccessContext;
import org.delia.db.DBInterface;
import org.delia.db.SqlHelperFactory;
import org.delia.db.TableExistenceServiceImpl;
import org.delia.db.sql.QueryTypeDetector;
import org.delia.db.sql.prepared.SelectFuncHelper;
import org.delia.db.sql.table.TableInfo;
import org.delia.runner.VarEvaluator;
import org.delia.type.DTypeRegistry;

public class FragmentParserService extends ServiceBase {
	protected int nextAliasIndex = 0;
	protected QueryTypeDetector queryDetectorSvc;
	protected DTypeRegistry registry;
	protected WhereFragmentGenerator whereGen;
	protected SelectFuncHelper selectFnHelper;
	protected TableExistenceServiceImpl existSvc;
	protected FKHelper fkHelper;
	protected JoinFragment savedJoinedFrag;
	protected List<TableInfo> tblinfoL;
	private VarEvaluator varEvaluator;
	private DBInterface dbInterface;
	private DBAccessContext dbctx;
	private SqlHelperFactory sqlHelperFactory;
	
	public FragmentParserService(FactoryService factorySvc, DTypeRegistry registry, VarEvaluator varEvaluator, List<TableInfo> tblinfoL, 
			DBInterface dbInterface, DBAccessContext dbctx, SqlHelperFactory sqlHelperFactory, WhereFragmentGenerator whereGen) {
		super(factorySvc);
		this.registry = registry;
		this.queryDetectorSvc = new QueryTypeDetector(factorySvc, registry);
		this.varEvaluator = varEvaluator;
		this.tblinfoL = tblinfoL;
		this.dbInterface = dbInterface;
		this.dbctx = dbctx;
		this.sqlHelperFactory = sqlHelperFactory;
		this.whereGen = whereGen; 
		
		this.selectFnHelper = new SelectFuncHelper(factorySvc, registry);
		this.existSvc = new TableExistenceServiceImpl(dbInterface, dbctx);
	}

	public QueryTypeDetector createQueryTypeDetector() {
		this.queryDetectorSvc = new QueryTypeDetector(factorySvc, registry);
		return this.queryDetectorSvc;
	}

	public TableExistenceServiceImpl createTableExistenceService() {
		return new TableExistenceServiceImpl(dbInterface, dbctx);
	}

	public FKHelper createFKHelper() {
		return new FKHelper(factorySvc, registry, tblinfoL, sqlHelperFactory, varEvaluator, existSvc);
	}

}
