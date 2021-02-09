package org.delia.app;

import org.delia.Delia;
import org.delia.DeliaSession;
import org.delia.builder.ConnectionInfo;
import org.delia.builder.DeliaBuilder;
import org.delia.core.FactoryService;
import org.delia.dao.DeliaGenericDao;
import org.delia.db.DBType;
import org.delia.db.sql.ConnectionString;
import org.delia.log.Log;
import org.delia.runner.ResultValue;
import org.delia.type.DTypeRegistry;
import org.delia.zdb.DBInterfaceFactory;

public class TypeDao {
	private DeliaGenericDao innerDao;
	protected String typeName;
	
	public TypeDao(String typeName, ConnectionInfo info) {
		this(typeName, DeliaBuilder.withConnection(info).build());
	}
	public TypeDao(String typeName, Delia delia) {
		this.typeName = typeName;
		this.innerDao = new DeliaGenericDao(delia);
	}
	public TypeDao(String typeName, Delia delia, DeliaSession session) {
		this.typeName = typeName;
		this.innerDao = new DeliaGenericDao(delia, session);
	}

	public TypeDao(String typeName, ConnectionString connString, DBType dbType, Log log) {
		this.typeName = typeName;
		this.innerDao = new DeliaGenericDao(connString, dbType, log);
	}

	public boolean initialize(String src) {
		return innerDao.initialize(src);
	}
	
	public ResultValue queryByPrimaryKey(String primaryKey) {
		return innerDao.queryByPrimaryKey(typeName, primaryKey);
	}

	public ResultValue queryByFilter(String filter) {
		return innerDao.queryByFilter(typeName, filter);
	}
	public ResultValue queryByStatement(String filterEx) {
		return innerDao.queryByStatement(typeName, filterEx);
	}
	public long count() {
		return innerDao.count(typeName);
	}

	public ResultValue insertOne(String fields) {
		return innerDao.insertOne(typeName, fields);
	}

	public ResultValue updateOne(String primaryKey, String fields) {
		return innerDao.updateOne(typeName, primaryKey, fields);
	}
	
	public ResultValue deleteOne(String primaryKey) {
		return innerDao.deleteOne(typeName, primaryKey);
	}

	public Delia getDelia() {
		return innerDao.getDelia();
	}

	public DBInterfaceFactory getDbInterface() {
		return innerDao.getDbInterface();
	}

	public DeliaSession getMostRecentSession() {
		return innerDao.getMostRecentSession();
	}

	public FactoryService getFactorySvc() {
		return innerDao.getFactorySvc();
	}
	public DTypeRegistry getRegistry() {
		return innerDao.getRegistry();
	}
	public DeliaGenericDao getInnerDao() {
		return innerDao;
	}

}