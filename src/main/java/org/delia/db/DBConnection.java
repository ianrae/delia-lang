package org.delia.db;


import org.delia.type.DType;

import java.sql.ResultSet;

//low level access to db. execute statements
//get back void, int, resultSet
//Short-term object. need to call close()
public interface DBConnection {
    void openDB();

    void close();

    ResultSet execQueryStatement(SqlStatement statement, DBExecuteContext dbctx);

    void execStatement(SqlStatement statement, DBExecuteContext sqlctx);

    int executeCommandStatement(SqlStatement statement, DBExecuteContext sqlctx);

    int executeCommandStatementGenKey(SqlStatement statement, DType keyType, DBExecuteContext sqlctx);

//    ValueHelper createValueHelper();
}