package org.delia.sql;

import org.delia.varevaluator.VarEvaluator;
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
	public VarEvaluator varEvaluator; 
	
	public DBAccessContext(DTypeRegistry registry, VarEvaluator eval) {
		this.registry = registry;
		this.varEvaluator = eval;
	}

	public DBAccessContext clone() {
		DBAccessContext ctx = new DBAccessContext(null, null);
		ctx.registry = this.registry;
		ctx.varEvaluator = this.varEvaluator;
		return ctx;
	}
}
