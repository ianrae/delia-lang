package org.delia.bddnew.core;

import org.delia.core.FactoryService;
import org.delia.core.FactoryServiceImpl;
import org.delia.db.DBType;
import org.delia.db.SqlStatement;
import org.delia.db.sql.ConnectionFactory;
import org.delia.db.sql.ConnectionFactoryImpl;
import org.delia.error.SimpleErrorTracker;
import org.delia.h2.H2ErrorConverter;
import org.delia.log.DeliaLog;
import org.delia.log.LogLevel;
import org.delia.util.StrCreator;
import org.delia.db.DBConnection;
import org.delia.dbimpl.h2.H2DBConnection;
import org.delia.dbimpl.postgres.PostgresDBConnection;
import org.delia.dbimpl.postgres.PostgresErrorConverter;

public class SqlSnippetRunner implements SnippetRunner {
    private final DeliaLog log;
    private ConnectionProvider connProvider;

    public SqlSnippetRunner(DeliaLog log) {
        this.log = log;
    }

    @Override
    public void setConnectionProvider(ConnectionProvider connProvider) {
        this.connProvider = connProvider;
    }
    @Override
    public BDDSnippetResult execute(BDDSnippet snippet, BDDSnippetResult previousRes) {
        BDDSnippetResult res = new BDDSnippetResult();

        try {
            DBConnection conn = createConnection(snippet.dbType);
            conn.openDB();

            StrCreator sc = new StrCreator();
            for(String line: snippet.lines) {
                sc.addStr(line);
                sc.nl();
            }
            SqlStatement statement = new SqlStatement(null);
            statement.sql = sc.toString();
            log.log("SQL snippet: %s", statement.sql);

            conn.execStatement(statement, null);
            log.log("SQL snippet: end");

            conn.close();
            res.ok = true;
        } catch (Exception e) {
            log.logException(LogLevel.ERROR, "sqlSnippet failed", e);
        }


        res.sess = previousRes == null ? null : previousRes.sess;
        return res;
    }

    private DBConnection createConnection(DBType dbType) {
        FactoryService factorySvc = new FactoryServiceImpl(log, new SimpleErrorTracker(log));
        ConnectionFactory connFact = new ConnectionFactoryImpl(connProvider.getConnectionDef(), log);
        switch(dbType) {
            case H2:
                return new H2DBConnection(factorySvc, connFact, new H2ErrorConverter());
            case POSTGRES:
                return new PostgresDBConnection(factorySvc, connFact, new PostgresErrorConverter()); //new SimpleSqlNameFormatter(null, true) ));
            default:
                throw new RuntimeException(String.format("unknown dbType %s", dbType));
        }
    }

}
