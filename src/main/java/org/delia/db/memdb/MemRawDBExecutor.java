//package org.delia.db.memdb;
//
//import org.delia.db.DBAccessContext;
//import org.delia.db.DBInterface;
//import org.delia.db.InsertContext;
//import org.delia.db.QueryContext;
//import org.delia.db.QuerySpec;
//import org.delia.db.RawDBExecutor;
//import org.delia.db.SchemaContext;
//import org.delia.runner.QueryResponse;
//import org.delia.type.DValue;
// 
//public class MemRawDBExecutor implements RawDBExecutor {
//
//	private DBInterface dbInterface;
//	private DBAccessContext dbctx;
//
//	public MemRawDBExecutor(MemDBInterface memDBInterface, DBAccessContext ctx) {
//		this.dbInterface = memDBInterface;
//		this.dbctx = ctx;
//	}
//	public void forceDBInterface(DBInterface newOne) {
//		this.dbInterface = newOne;
//	}
//
//	//close is from AutoClosable
//	@Override
//	public void close() {
////		conn.close(); no conn with mem db
//	}
//	@Override
//	public DValue executeInsert(DValue dval, InsertContext ctx) {
//		return dbInterface.executeInsert(dval, ctx, dbctx);
//	}
//	@Override
//	public QueryResponse executeQuery(QuerySpec spec, QueryContext qtx) {
//		return dbInterface.executeQuery(spec, qtx, dbctx);
//	}
//	@Override
//	public boolean execTableDetect(String tableName) {
//		return dbInterface.doesTableExist(tableName, dbctx);
//	}
//	@Override
//	public void createTable(String tableName) {
//		dbInterface.createTable(tableName, dbctx, new SchemaContext());
//	}
//	@Override
//	public boolean execFieldDetect(String tableName, String fieldName) {
//		return dbInterface.doesFieldExist(tableName, fieldName, dbctx);
//	}
//
//}
