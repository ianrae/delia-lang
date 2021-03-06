package org.delia.dao;

import org.delia.ConnectionDefinitionBuilder;
import org.delia.Delia;
import org.delia.DeliaFactory;
import org.delia.DeliaSession;
import org.delia.builder.DeliaBuilder;
import org.delia.core.FactoryService;
import org.delia.core.FactoryServiceImpl;
import org.delia.db.sql.ConnectionDefinition;
import org.delia.error.ErrorTracker;
import org.delia.error.SimpleErrorTracker;
import org.delia.log.Log;
import org.delia.runner.BlobLoader;
import org.delia.runner.ResultValue;
import org.delia.type.DTypeRegistry;
import org.delia.util.DeliaExceptionHelper;
import org.delia.zdb.DBInterfaceFactory;

/**
 * Represents delia access to a single database through its Delia types.
 * This is a generic DAO that is not related to the code-gen DAOs.
 * This DAO can be used without doing any code generation.
 * 
 * @author Ian Rae
 *
 */
public class DeliaGenericDao  {
	private Delia delia;
	private DBInterfaceFactory dbInterface;
	private DeliaSession mostRecentSess;
	private FactoryService factorySvc;
	private BlobLoader blobLoader;
	
	public DeliaGenericDao() {
		ConnectionDefinition connStr = ConnectionDefinitionBuilder.createMEM();
		this.delia = DeliaBuilder.withConnection(connStr).build();
		this.dbInterface = delia.getDBInterface();
		this.factorySvc = delia.getFactoryService();
	}
	
//	public DeliaGenericDao(ConnectionInfo info) {
//		this(DeliaBuilder.withConnection(info).build());
//	}
	public DeliaGenericDao(Delia delia) {
		this.dbInterface = delia.getDBInterface();
		this.factorySvc = delia.getFactoryService();
		this.delia = delia;
	}
	public DeliaGenericDao(Delia delia, DeliaSession session) {
		this.dbInterface = delia.getDBInterface();
		this.factorySvc = delia.getFactoryService();
		this.delia = delia;
		this.mostRecentSess = session;
	}

	public DeliaGenericDao(ConnectionDefinition connString, Log log) {
		ErrorTracker et = new SimpleErrorTracker(log);
		this.factorySvc = new FactoryServiceImpl(log, et);
		delia = DeliaFactory.create(connString, log, factorySvc);
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
		mostRecentSess = delia.beginSession(src, blobLoader);
		return mostRecentSess.getFinalResult();
	}

	public ResultValue execute(String src) {
		ResultValue res = delia.continueExecution(src, blobLoader, mostRecentSess);
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

	public DBInterfaceFactory getDbInterface() {
		return dbInterface;
	}

	public DeliaSession getMostRecentSession() {
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

	public BlobLoader getBlobLoader() {
		return blobLoader;
	}

	public void setBlobLoader(BlobLoader blobLoader) {
		this.blobLoader = blobLoader;
	}
}