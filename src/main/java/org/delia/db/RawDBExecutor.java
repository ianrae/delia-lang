//package org.delia.db;
//
//import org.delia.runner.QueryResponse;
//import org.delia.type.DValue;
//
///**
// * Low-level access to db. used during startup
// * when registry or idDatMap not fully available.
// * 
// * We manage jdbc connection  inside this class.
// * @author Ian Rae
// *
// */
//public interface RawDBExecutor extends AutoCloseable {
//	
//	DValue executeInsert(DValue dval, InsertContext ctx);
//	QueryResponse executeQuery(QuerySpec spec, QueryContext qtx);
//	
//	boolean execTableDetect(String tableName);
//	boolean execFieldDetect(String tableName, String fieldName);
//	
//	//schema actions
//	void createTable(String tableName);
//}
