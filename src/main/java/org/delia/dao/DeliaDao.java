package org.delia.dao;

import org.delia.api.DeliaSession;
import org.delia.builder.ConnectionInfo;
import org.delia.builder.DeliaBuilder;
import org.delia.api.Delia;
import org.delia.api.DeliaFactory;
import org.delia.core.FactoryService;
import org.delia.core.FactoryServiceImpl;
import org.delia.db.DBInterface;
import org.delia.db.DBType;
import org.delia.db.sql.ConnectionString;
import org.delia.error.ErrorTracker;
import org.delia.error.SimpleErrorTracker;
import org.delia.log.Log;
import org.delia.runner.ResultValue;
import org.delia.type.DTypeRegistry;
import org.delia.util.DeliaExceptionHelper;

/**
 * Represents delia access to a single database through its Delia types.
 * 
 * @author Ian Rae
 *
 */
public class DeliaDao  {
	private Delia delia;
	private DBInterface dbInterface;
	private DeliaSession mostRecentSess;
	private FactoryService factorySvc;
	
	public DeliaDao(ConnectionInfo info) {
		this(DeliaBuilder.withConnection(info).build());
	}
	public DeliaDao(Delia delia) {
		this.dbInterface = delia.getDBInterface();
		this.factorySvc = delia.getFactoryService();
		this.delia = delia;
	}

	public DeliaDao(ConnectionString connString, DBType dbType, Log log) {
		ErrorTracker et = new SimpleErrorTracker(log);
		this.factorySvc = new FactoryServiceImpl(log, et);
		delia = DeliaFactory.create(connString, dbType, log, factorySvc);
	}

	public boolean initialize(String src) {
		mostRecentSess = null;
		ResultValue res = new ResultValue();
		res = beginExecution(src);
		return res.ok;
	}
	
	protected ResultValue beginExecution(String src) {
		if (mostRecentSess != null) {
			DeliaExceptionHelper.throwError("dao-init-error", "can't start a Dao more than once");
		}
		//TODO: ensure sess is null
		mostRecentSess = delia.beginSession(src);
		return mostRecentSess.getFinalResult();
	}

	public ResultValue execute(String src) {
		ResultValue res = delia.continueExecution(src, mostRecentSess);
		return res;
	}
	
	public ResultValue queryByPrimaryKey(String type, String primaryKey) {
		String src = String.format("let $$ = %s[%s]", type, primaryKey);
		return execute(src);
	}

	public ResultValue queryByFilter(String type, String filter) {
		String src = String.format("let $$ = %s[%s]", type, filter);
		return execute(src);
	}
	public ResultValue queryByStatement(String type, String filterEx) {
		String src = String.format("let $$ = %s%s", type, filterEx);
		return execute(src);
	}
	public long count(String type) {
		String src = String.format("let $$ = %s[true].count()", type);
		ResultValue res = execute(src);
		if (res.ok) {
			Long n = res.getAsDValue().asLong();
			return n;
		} else {
			return 0;
		}
	}

	public ResultValue insertOne(String type, String fields) {
		String src;
		if (! fields.startsWith("{")) {
			src = String.format("insert %s {%s}", type, fields);
		} else {
			src = String.format("insert %s %s", type, fields);
		}
		return execute(src);
	}

	public ResultValue updateOne(String type, String primaryKey, String fields) {
		String src;
		if (! fields.startsWith("{")) {
			src = String.format("update %s[%s] {%s}", type, primaryKey, fields);
		} else {
			src = String.format("update %s[%s] %s", type, primaryKey, fields);
		}
		return execute(src);
	}
	
	public ResultValue deleteOne(String type, String primaryKey) {
		String src = String.format("delete %s[%s]", type, primaryKey);
		return execute(src);
	}

	public Delia getDelia() {
		return delia;
	}

	public DBInterface getDbInterface() {
		return dbInterface;
	}

	public DeliaSession getMostRecentSess() {
		return mostRecentSess;
	}

	public FactoryService getFactorySvc() {
		return factorySvc;
	}
	public DTypeRegistry getRegistry() {
		if (mostRecentSess == null) {
			DeliaExceptionHelper.throwError("dao-registry-not-created-yet", "you must call initialize() before calling this method");
		}
		return this.mostRecentSess.getExecutionContext().registry;
	}
}