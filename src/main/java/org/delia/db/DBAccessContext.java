package org.delia.db;

import org.delia.runner.Runner;
import org.delia.runner.VarEvaluator;
import org.delia.type.DTypeRegistry;

/**
 * Represents the context of calling the db. (usually a runner)
 * In a web app there is one Delia object and many runners.
 * runners share context through NewExecutionContext.
 * 
 * However it may be that several delias are sharing the same dbinterface,
 * or one delia but beginExecution is being called from multiple threads
 * to run different delia types.
 * 
 * @author Ian Rae
 *
 */
public class DBAccessContext {
	public DTypeRegistry registry;
	public VarEvaluator varEvaluator; //only used by MEMDb
	public Object connObject; //for internal use only
	public boolean disableSqlLogging; //for internal use only
	
	public DBAccessContext(DTypeRegistry registry, VarEvaluator eval) {
		this.registry = registry;
		this.varEvaluator = eval;
	}
	public DBAccessContext(Runner runner) {
		this.registry = runner.getRegistry();
		this.varEvaluator = runner;
	}
	
	public DBAccessContext clone() {
		DBAccessContext ctx = new DBAccessContext(null, null);
		ctx.registry = this.registry;
		ctx.varEvaluator = this.varEvaluator;
		ctx.connObject = this.connObject;
		return ctx;
	}
}
