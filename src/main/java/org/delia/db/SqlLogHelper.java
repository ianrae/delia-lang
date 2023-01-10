package org.delia.db;

import org.delia.lld.LLD;
import org.delia.log.DeliaLog;
import org.delia.log.LoggableBlob;
import org.delia.type.DValue;
import org.delia.type.Shape;

import java.util.StringJoiner;

public class SqlLogHelper {

    public static void logSql(LLD.LLStatement stmt, DeliaLog log) {
        logSql(stmt, log, true);
    }
    public static void logSql(LLD.LLStatement stmt, DeliaLog log, boolean logPrefix) {
        StringJoiner joiner = new StringJoiner(",");
        SqlStatement sqlStatement = stmt.getSql();
        if (sqlStatement != null) {
            for(DValue dval: sqlStatement.paramL) {
                if (dval == null) {
                    joiner.add("null");
                } else if (dval.getType().isShape(Shape.BLOB)) {
                    LoggableBlob lb = new LoggableBlob(dval.asString());
                    joiner.add(String.format("'%s'", lb.toLoggableHexString()));
                } else {
                    joiner.add(String.format("'%s'", dval.asString()));
                }
            }
        }

        String sql = sqlStatement == null ?  "?" : sqlStatement.sql;
        if (logPrefix) {
            log.log("SQL: %s -- (%s)", sql, joiner);
        } else {
            log.log("%s -- (%s)", sql, joiner);
        }
    }

}
