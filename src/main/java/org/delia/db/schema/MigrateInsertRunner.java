package org.delia.db.schema;

import java.util.HashMap;
import java.util.Map;

import org.delia.compiler.ast.IdentExp;
import org.delia.compiler.ast.InsertStatementExp;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.error.DeliaError;
import org.delia.hld.HLDFacade;
import org.delia.hld.HLDFactory;
import org.delia.runner.BlobLoader;
import org.delia.runner.DValueIterator;
import org.delia.runner.DoNothingVarEvaluator;
import org.delia.runner.FetchRunner;
import org.delia.runner.InsertStatementRunner;
import org.delia.runner.ResultValue;
import org.delia.runner.RunnerForMigrateInsert;
import org.delia.runner.ZFetchRunnerImpl;
import org.delia.sprig.SprigService;
import org.delia.sprig.SprigServiceImpl;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.util.DeliaExceptionHelper;
import org.delia.zdb.DBExecutor;
import org.delia.zdb.DBInterfaceFactory;

/**
 * Perform INSERT statements using HLD.
 * 
 * @author irae
 *
 */
public class MigrateInsertRunner extends ServiceBase {

	private DTypeRegistry registry;
	private DBExecutor dbexecutor;
	private DBInterfaceFactory dbInterface;
	private HLDFactory hldFactory;

	public MigrateInsertRunner(FactoryService factorySvc, DTypeRegistry registry, DBExecutor dbexecutor, DBInterfaceFactory dbInterface) {
		super(factorySvc);
		this.dbexecutor = dbexecutor;
		this.registry = registry;
		this.dbInterface = dbInterface;
	}

	
	public void doInsert(DValue dval) {
		RunnerForMigrateInsert runner = new RunnerForMigrateInsert();
		Map<String,ResultValue> varMap = new HashMap<>();
		InsertStatementRunner insertRunner = new InsertStatementRunner(factorySvc, dbInterface, runner, registry, varMap);
		SprigService sprigSvc = new SprigServiceImpl(factorySvc, registry);
		InsertStatementExp exp = new InsertStatementExp(99, new IdentExp(dval.getType().getName()), null);
		ResultValue res = new ResultValue();
		FetchRunner fetchRunner = new ZFetchRunnerImpl(factorySvc, dbexecutor, registry, new DoNothingVarEvaluator());
		HLDFacade hldFacade = new  HLDFacade(factorySvc, dbInterface, registry, new DoNothingVarEvaluator());
		DValueIterator dvalIter = new DValueIterator(dval);
		
		BlobLoader blobLoader = null; //TODO fix!
		insertRunner.executeInsertStatement(exp, res, hldFacade, dbexecutor, fetchRunner, dvalIter, sprigSvc, blobLoader);
		if (!res.isSuccess()) {
			DeliaError err = res.getLastError();
			DeliaExceptionHelper.throwError(err);
		}
	}
}