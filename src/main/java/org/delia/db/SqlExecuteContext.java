package org.delia.db;

import java.sql.ResultSet;

import org.delia.runner.VarEvaluator;
import org.delia.type.DTypeRegistry;

public class SqlExecuteContext extends DBAccessContext {
	public boolean getGeneratedKeys;
	public ResultSet genKeys;

	public SqlExecuteContext(DTypeRegistry registry, VarEvaluator eval) {
		super(registry, eval);
	}
	public SqlExecuteContext(DBAccessContext dbctx) {
		super(dbctx.registry, dbctx.varEvaluator);
		this.connObject = dbctx.connObject;
	}
}
