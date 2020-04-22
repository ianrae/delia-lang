package org.delia.db;

public class TableExistenceServiceImpl implements TableExistenceService {

	private DBAccessContext dbctx;
	private DBInterface dbInterface;
	
	public TableExistenceServiceImpl(DBInterface dbInterfae, DBAccessContext dbctx) {
		this.dbInterface = dbInterfae;
		this.dbctx = dbctx;
	}
	
	@Override
	public boolean doesTableExist(String tableName) {
		return dbInterface.doesTableExist(tableName, dbctx);
	}

}
