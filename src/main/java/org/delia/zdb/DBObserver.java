//package org.delia.zdb;
//
//import org.delia.db.InsertContext;
//import org.delia.db.QueryContext;
//import org.delia.db.SqlStatementGroup;
//import org.delia.hld.HLDQueryStatement;
//import org.delia.hld.cud.HLDDeleteStatement;
//import org.delia.hld.cud.HLDInsertStatement;
//import org.delia.hld.cud.HLDUpdateStatement;
//import org.delia.hld.cud.HLDUpsertStatement;
//import org.delia.runner.QueryResponse;
//import org.delia.type.DValue;
//
//public interface DBObserver {
//
//	QueryResponse executeHLDQuery(HLDQueryStatement hld, SqlStatementGroup stmgrp, QueryContext qtx);
//	DValue executeInsert(HLDInsertStatement hld, SqlStatementGroup stmgrp, InsertContext ctx);
//	int executeUpdate(HLDUpdateStatement hld, SqlStatementGroup stmgrp); 
//	int executeUpsert(HLDUpsertStatement hld, SqlStatementGroup stmgrp, boolean noUpdateFlag); 
//	void executeDelete(HLDDeleteStatement hld, SqlStatementGroup stmgrp);
//
//}