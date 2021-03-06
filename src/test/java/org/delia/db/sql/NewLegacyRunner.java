package org.delia.db.sql;

import org.delia.Delia;
import org.delia.DeliaSession;
import org.delia.api.DeliaImpl;
import org.delia.base.DBTestHelper;
import org.delia.compiler.generate.DeliaGeneratePhase;
import org.delia.core.FactoryService;
import org.delia.db.DBAccessContext;
import org.delia.log.Log;
import org.delia.runner.DoNothingVarEvaluator;
import org.delia.runner.InternalCompileState;
import org.delia.runner.ResultValue;
import org.delia.runner.Runner;
import org.delia.type.DTypeRegistry;
import org.delia.zdb.DBInterfaceFactory;

public class NewLegacyRunner {
	private Delia delia;
	private DeliaSession session = null;
	private Log log;

	public NewLegacyRunner(Log log) {
		this.delia = DBTestHelper.createNewDelia();
		this.log = delia.getLog();
	}
	public NewLegacyRunner(Delia delia) {
		this.delia = delia;
		this.log = delia.getLog();
	}
	
	public DeliaSession begin(String src) {
		log.log("NLR: %s", src);
		session = delia.beginSession(src);
		return session;
	}

//	public ResultValue executeOneStatement(TypeStatementExp exp0) {
//		String src = exp0.strValue(); //  xxxTODxO this may not be correct
//		return continueExecution(src);
//	}
	public ResultValue continueExecution(String src){
		log.log("NLRc: %s", src);
		ResultValue res = delia.continueExecution(src, session);
		return res;
	}

	public DTypeRegistry getRegistry() {
		return session.getExecutionContext().registry;
	}

	public DBAccessContext createDBAccessContext() {
		DBAccessContext dbctx = new DBAccessContext(getRegistry(), new DoNothingVarEvaluator());
		return dbctx;
	}
	public FactoryService getFactoryService() {
		return delia.getFactoryService();
	}
	public Delia getDelia() {
		return delia;
	}
	public void forceDBInterface(DBInterfaceFactory dbInter) {
		DeliaImpl deliaimpl = (DeliaImpl) delia;
		deliaimpl.setDbInterface(dbInter);
	}
	public ResultValue beginOrContinue(String src, boolean shouldPass) {
		if (session == null) {
			begin(src);
			ResultValue res = new ResultValue();
			res.ok = shouldPass; //hack
			return res;
		} else {
			return continueExecution(src);
		}
	}
	public InternalCompileState getCompileState() {
		Runner runner = session.getMostRecentRunner();
		return runner.getCompileState();
	}
	public DeliaGeneratePhase createGenerator() {
		Runner runner = session.getMostRecentRunner();
		return runner.createGenerator();
	}
	public Runner getDeliaRunner() {
		Runner runner = session.getMostRecentRunner();
		return runner;
	}
	public boolean exists(String varName) {
		Runner runner = session.getMostRecentRunner();
		return runner.exists(varName);
	}
	public ResultValue getVar(String varName) {
		Runner runner = session.getMostRecentRunner();
		return runner.getVar(varName);
	}

}