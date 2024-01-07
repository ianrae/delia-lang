package org.delia.base;

import org.delia.core.FactoryService;
import org.delia.core.FactoryServiceImpl;
import org.delia.db.DBException;
import org.delia.db.DBType;
import org.delia.db.SqlStatement;
import org.delia.db.sql.ConnectionDefinition;
import org.delia.db.sql.ConnectionFactory;
import org.delia.db.sql.ConnectionFactoryImpl;
import org.delia.error.ErrorTracker;
import org.delia.error.SimpleErrorTracker;
import org.delia.h2.H2ConnectionHelper;
import org.delia.h2.H2ErrorConverter;
import org.delia.log.DeliaLog;
import org.delia.mysql.MySqlConnectionHelper;
import org.delia.postgres.PostgresConnectionHelper;
import org.delia.db.DBConnection;
import org.delia.dbimpl.h2.H2DBConnection;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;

public class DbTableCleaner {
    private String schemaName;

    //    public void clean() {
//        clean(H2ConnectionHelper.getTestDB(), "PUBLIC");
//    }
    public void cleanDB(DBType dbType) {
        if (DBType.H2.equals(dbType)) {
            clean(H2ConnectionHelper.getTestDB(), "PUBLIC");
        } else if (DBType.POSTGRES.equals(dbType)) {
            clean(PostgresConnectionHelper.getTestDB(), "public");
        } else if (DBType.MYSQL.equals(dbType)) {
            clean(MySqlConnectionHelper.getTestDB(), "public");
        }
    }

    public void clean(ConnectionDefinition connDef, String schemaName) {
        this.schemaName = schemaName;
        ConnectionFactory connFact = new ConnectionFactoryImpl(connDef, log);
        H2DBConnection conn = null;
        try {
            conn = new H2DBConnection(factorySvc, connFact, new H2ErrorConverter());
            log.log("TableCleaner..");
            conn.openDB();

//            log.log("and..");
            deleteTable(conn, "cars");
            //northwind
            deleteTable(conn, "Order_Detail"); //Order can't be used:reserved work in postgres
            deleteTable(conn, "Orders"); //Order can't be used:reserved work in postgres
            deleteTable(conn, "Orderz"); //Order can't be used:reserved work in postgres
            deleteTable(conn, "Product");
            deleteTable(conn, "Category");
            deleteTable(conn, "Shipper");
            deleteTable(conn, "Supplier");
            deleteTable(conn, "Employee_Territory");
            deleteTable(conn, "Territory");
            deleteTable(conn, "Region");
            deleteTable(conn, "Customer");
            deleteTable(conn, "Customer22");
            deleteTable(conn, "Employee");

            //bdd
            deleteTable(conn, "Flight");
            deleteTable(conn, "Flight2");

            //other
            deleteTable(conn, "Person2");
            deleteTable(conn, "Person2__BAK");
            deleteTable(conn, "Address");
            deleteTable(conn, "Address22");
            deleteTable(conn, "alpha.Person");
            deleteTable(conn, "Person");
            deleteTable(conn, "Person__BAK");
            deleteTable(conn, "customeraddressdat1");
            deleteTable(conn, "customeraddressdat2");
            deleteTable(conn, "Address");
            deleteTable(conn, "Address__BAK");
            deleteTable(conn, "Customer");
            deleteTable(conn, "Customer__BAK");
            deleteTable(conn, "delia_schema_version"); //SchemaMigrator.SCHEMA_TABLE);
            deleteTable(conn, "delia_assoc"); //SchemaMigrator.DAT_TABLE);

            deleteTable(conn, "s2.Address");
            deleteTable(conn, "s2.Customer");

            deleteTable(conn, "gg", "Person");
            deleteTable(conn, "gg", "delia_schema_version"); //SchemaMigrator.SCHEMA_TABLE);
            deleteTable(conn, "gg", "delia_assoc"); //SchemaMigrator.DAT_TABLE);


//            dumpSchema(conn);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            conn.close();
        }

        log.log("end.");
    }

    private void deleteTable(H2DBConnection conn, String tblName) {
        String sql = String.format("DROP TABLE IF EXISTS %s CASCADE;", tblName.toUpperCase(Locale.ROOT));
        execStatement(conn, sql);
    }

    private void deleteTable(H2DBConnection conn, String schema, String tblName) {
        String sql = String.format("DROP TABLE IF EXISTS %s.%s CASCADE;", schema.toUpperCase(Locale.ROOT), tblName.toUpperCase(Locale.ROOT));
        execStatement(conn, sql);
    }

    private void dumpSchema(DBConnection conn) throws SQLException {
        log.log("dump schema: TABLES...");
        SqlStatement statement = new SqlStatement(null);
        statement.sql = String.format("SELECT * from information_schema.tables where TABLE_SCHEMA='%s';", schemaName);
        ResultSet rs = conn.execQueryStatement(statement, null);
        while (rs.next()) {
            log.log(rs.getString("TABLE_NAME"));
        }

    }

    private void execStatement(H2DBConnection conn, String sql) {
        SqlStatement statement = new SqlStatement(null);
        statement.sql = sql;
        try {
            conn.execStatement(statement, null);
        } catch (DBException e) {
            if (e.getMessage().contains("cannot drop table")) {
                //ignore
            } else {
                e.printStackTrace();
            }
        }
    }

    //--
//    protected Runner runner;
    protected DeliaLog log = new UnitTestLog();
    protected ErrorTracker et = new SimpleErrorTracker(log);
    protected FactoryService factorySvc = new FactoryServiceImpl(log, et);

}
