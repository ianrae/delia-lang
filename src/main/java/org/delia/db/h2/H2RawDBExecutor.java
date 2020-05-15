package org.delia.db.h2;

import org.delia.db.DBAccessContext;
import org.delia.db.InsertContext;
import org.delia.db.QueryContext;
import org.delia.db.QuerySpec;
import org.delia.db.RawDBExecutor;
import org.delia.runner.QueryResponse;
import org.delia.type.DValue;

public class H2RawDBExecutor implements RawDBExecutor {

	private H2DBInterface dbInterface;
	private DBAccessContext dbctx;

	public H2RawDBExecutor(H2DBInterface dbInterface, DBAccessContext ctx, H2DBConnection conn) {
		this.dbInterface = dbInterface;
		this.dbctx = ctx;
		dbctx.connObject = conn;
	}
	
	public H2DBConnection getConn() {
		return (H2DBConnection) dbctx.connObject;
	}

	@Override
	public void close() throws Exception {
		H2DBConnection conn = (H2DBConnection) dbctx.connObject;
		conn.close();
	}

	@Override
	public DValue executeInsert(DValue dval, InsertContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public QueryResponse executeQuery(QuerySpec spec, QueryContext qtx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean execTableDetect(String tableName) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void createTable(String tableName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean execFieldDetect(String tableName, String fieldName) {
		// TODO Auto-generated method stub
		return false;
	}

}
