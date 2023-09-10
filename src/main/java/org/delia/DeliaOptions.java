package org.delia;

import org.delia.migration.MigrationAction;
import org.delia.runner.StatementBuilderPlugin;
import org.delia.varevaluator.CustomVarEvaluatorFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Options that can be changed during the lifetime of a Delia object.
 *
 * @author Ian Rae
 */
public class DeliaOptions {
    //    public boolean disableSQLLoggingDuringSchemaMigration = true;
    public MigrationAction migrationAction = MigrationAction.GENERATE;
    //    public boolean enableExecution = true;
//    public boolean useSafeMigrationPolicy = true;
    public boolean saveParseScriptInSession = true; //can be helpful for troubleshooting. not needed by Delia
    //    public boolean logSourceBeforeCompile; //log all delia source before it is compiled
    //	public DBObserverFactory dbObserverFactory;
    public boolean executeInTransaction = false; //use when want execute()/continueExeuction() in transaction
    public String defaultSchema;
    public boolean autoSortByPK;
    public StatementBuilderPlugin statementBuilderPlugin;
    public List<String> assocHints = new ArrayList<>(); //"type1.throughField.type2"
    public int defaultStringColumnLength = 200;
    public CustomVarEvaluatorFactory customVarEvaluatorFactory;
    public boolean generateSqlWhenMEMDBType = false; //we don't need the sql, but it can be useful
    public boolean bulkInsertEnabled = false;
    public int bulkInsertMaxBulkSize = 10;
    public boolean executeSQLDDLStatements = true; //if truen then create table and create schema will be executed on db


    /**
     * Used in child sesssions to create a private copy of options so that the client
     * code can adjust options for the child session w/o affecting other sessions.
     *
     * Note. This may not work for many options. It's intended mainly for bulkInsertEnabled and bulkInsertMaxBulkSize
     * @return
     */
    public DeliaOptions clone() {
        DeliaOptions copy = new DeliaOptions();

        copy.migrationAction = this.migrationAction;
        copy.saveParseScriptInSession = this.saveParseScriptInSession;
        copy.executeInTransaction = this.executeInTransaction;
        copy.defaultSchema = this.defaultSchema;
        copy.autoSortByPK = this.autoSortByPK;
        copy.statementBuilderPlugin = this.statementBuilderPlugin;
        copy.assocHints = this.assocHints;
        copy.defaultStringColumnLength = this.defaultStringColumnLength;
        copy.customVarEvaluatorFactory = this.customVarEvaluatorFactory;
        copy.generateSqlWhenMEMDBType = this.generateSqlWhenMEMDBType;
        copy.bulkInsertEnabled = this.bulkInsertEnabled;
        copy.bulkInsertMaxBulkSize = this.bulkInsertMaxBulkSize;

        return copy;
    }
}
