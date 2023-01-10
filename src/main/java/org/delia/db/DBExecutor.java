package org.delia.db;


import org.delia.hld.dat.DatService;
import org.delia.lld.LLD;
import org.delia.log.DeliaLog;
import org.delia.runner.DeliaRunner;
import org.delia.runner.QueryResponse;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;

public interface DBExecutor extends AutoCloseable {
    DBConnection getDBConnection(); //for raw db access

    DeliaLog getLog();

    DValue execInsert(LLD.LLInsert stmt, DValue dval);
    void execDelete(LLD.LLDelete stmt);
    void execUpdate(LLD.LLUpdate stmt);
    void execUpsert(LLD.LLUpsert stmt, DValue dval);

    QueryResponse execSelect(LLD.LLSelect stmt, SelectDBContext ctx);

    void execCreateTable(LLD.LLCreateTable stmt);
    void execCreateAssocTable(LLD.LLCreateAssocTable stmt);

    void execCreateSchema(LLD.LLCreateSchema stmt);


//    //executor holds session data regarding db
    void init1(DTypeRegistry registry, DatService datSvc, DeliaRunner deliaRunner);
//    void init2(DatIdMap datIdMap, VarEvaluator varEvaluator);
//    String getDefaultSchema();
//    void setDefaultSchema(String schema);
//    FetchRunner createFetchRunner();
//    DatIdMap getDatIdMap();


//    //these can be called after init1
//    DValue rawInsert(SqlStatement stm, InsertContext ctx);
//    boolean rawTableDetect(String tableName);
//    boolean rawFieldDetect(String tableName, String fieldName);
//    void rawCreateTable(String tableName);

    //these can ONLY be called after init2
//    QueryResponse executeHLDQuery(HLDQueryStatement hld, SqlStatementGroup stmgrp, QueryContext qtx);
//    DValue executeInsert(HLDInsertStatement hld, SqlStatementGroup stmgrp, InsertContext ctx);
//    int executeUpdate(HLDUpdateStatement hld, SqlStatementGroup stmgrp);
//    int executeUpsert(HLDUpsertStatement hld, SqlStatementGroup stmgrp, boolean noUpdateFlag);
//    void executeDelete(HLDDeleteStatement hld, SqlStatementGroup stmgrp);

//    //schema actions (only be called after init2)
//    boolean doesTableExist(String tableName);
//    boolean doesFieldExist(String tableName, String fieldName);
//    void createTable(String tableName);
//    void deleteTable(String tableName);
//    void renameTable(String tableName, String newTableName);
//    void createField(String typeName, String field, int sizeof);
//    void deleteField(String typeName, String field, int datId);
//    void renameField(String typeName, String fieldName, String newName);
//    void alterFieldType(String typeName, String fieldName, String newFieldType, int sizeof);
//    void alterField(String typeName, String fieldName, String deltaFlags);
//    void performSchemaChangeAction(SchemaChangeAction action);
//    void executeSchemaChangeOperation(SchemaChangeOperation op);

    DBInterfaceFactory getDbInterface();

}