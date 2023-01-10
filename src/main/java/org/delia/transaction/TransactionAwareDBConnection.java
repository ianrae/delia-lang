package org.delia.transaction;

import org.delia.db.DBConnection;
import org.delia.db.DBConnectionInternal;
import org.delia.db.DBExecuteContext;
import org.delia.db.SqlStatement;
import org.delia.type.DType;

import java.sql.ResultSet;

public class TransactionAwareDBConnection implements DBConnection {

    private DBConnection conn;

    public TransactionAwareDBConnection(DBConnection conn) {
        this.conn = conn;
    }

    @Override
    public void openDB() {
        conn.openDB();
    }

    @Override
    public void close() {
        //do nothing
    }

    public void actuallyClose() {
        conn.close();
    }

    public DBConnectionInternal getConnInternal() {
        return (DBConnectionInternal) conn;
    }

    @Override
    public ResultSet execQueryStatement(SqlStatement statement, DBExecuteContext dbctx) {
        return conn.execQueryStatement(statement, dbctx);
    }

    @Override
    public void execStatement(SqlStatement statement, DBExecuteContext sqlctx) {
        conn.execStatement(statement, sqlctx);
    }

    @Override
    public int executeCommandStatement(SqlStatement statement, DBExecuteContext sqlctx) {
        return conn.executeCommandStatement(statement, sqlctx);
    }

    @Override
    public int executeCommandStatementGenKey(SqlStatement statement, DType keyType, DBExecuteContext sqlctx) {
        return conn.executeCommandStatementGenKey(statement, keyType, sqlctx);
    }


}
