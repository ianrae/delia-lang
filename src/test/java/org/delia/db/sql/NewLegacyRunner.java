package org.delia.db.sql;

import org.delia.api.Delia;
import org.delia.api.DeliaImpl;
import org.delia.api.DeliaSession;
import org.delia.builder.ConnectionBuilder;
import org.delia.builder.ConnectionInfo;
import org.delia.builder.DeliaBuilder;
import org.delia.compiler.ast.TypeStatementExp;
import org.delia.core.FactoryService;
import org.delia.db.DBAccessContext;
import org.delia.db.DBInterface;
import org.delia.db.DBType;
import org.delia.log.Log;
import org.delia.runner.DoNothingVarEvaluator;
import org.delia.runner.ResultValue;
import org.delia.type.DTypeRegistry;

public class NewLegacyRunner {
	private Delia delia;
	private DeliaSession session = null;

	public NewLegacyRunner(Log log) {
		ConnectionInfo info = ConnectionBuilder.dbType(DBType.MEM).build();
		Delia delia = DeliaBuilder.withConnection(info).log(log).build();
		this.delia = delia;
	}
	public NewLegacyRunner(Delia delia) {
		this.delia = delia;
	}
	
	public DeliaSession begin(String src) {
		session = delia.beginSession(src);
		return session;
	}

	public ResultValue executeOneStatement(TypeStatementExp exp0) {
		String src = exp0.strValue(); //TODO this may not be correct
		return continueExecution(src);
	}
	public ResultValue continueExecution(String src){
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
	public void forceDBInterface(DBInterface dbInter) {
		DeliaImpl deliaimpl = (DeliaImpl) delia;
		deliaimpl.setDbInterface(dbInter);
	}
	public ResultValue beginOrContinue(String src, boolean shouldPass) {
		if (session == null) {
			session = delia.beginSession(src);
			ResultValue res = new ResultValue();
			res.ok = shouldPass; //hack
			return res;
		} else {
			return delia.continueExecution(src, session);
		}
	}

}