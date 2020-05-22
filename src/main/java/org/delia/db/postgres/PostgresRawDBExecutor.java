//package org.delia.db.postgres;
//
//import org.delia.db.DBAccessContext;
//import org.delia.db.InsertContext;
//import org.delia.db.QueryContext;
//import org.delia.db.QuerySpec;
//import org.delia.db.RawDBExecutor;
//import org.delia.db.h2.H2DBConnection;
//import org.delia.runner.QueryResponse;
//import org.delia.type.DValue;
//
//public class PostgresRawDBExecutor implements RawDBExecutor {
//
//	private PostgresDBInterface dbInterface;
//	private DBAccessContext dbctx;
//
//	public PostgresRawDBExecutor(PostgresDBInterface dbInterface, DBAccessContext ctx, H2DBConnection conn) {
//		this.dbInterface = dbInterface;
//		this.dbctx = ctx;
//		dbctx.connObject = conn;
//	}
//	public H2DBConnection getConn() {
//		return (H2DBConnection) dbctx.connObject;
//	}
//
//
//	@Override
//	public QueryResponse executeQuery(QuerySpec spec, QueryContext qtx) {
//		return dbInterface.executeQuery(spec, qtx, dbctx);
//	}
//	@Override
//	public void close() throws Exception {
//		H2DBConnection conn = (H2DBConnection) dbctx.connObject;
//		conn.close();
//	}
//	@Override
//	public DValue executeInsert(DValue dval, InsertContext ctx) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//	@Override
//	public boolean execTableDetect(String tableName) {
//		// TODO Auto-generated method stub
//		return false;
//	}
//	@Override
//	public void createTable(String tableName) {
//		// TODO Auto-generated method stub
//		
//	}
//	@Override
//	public boolean execFieldDetect(String tableName, String fieldName) {
//		// TODO Auto-generated method stub
//		return false;
//	}
//
//}
