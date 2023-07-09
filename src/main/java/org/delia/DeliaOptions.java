package org.delia;

import org.delia.migration.MigrationAction;
import org.delia.varevaluator.CustomVarEvaluatorFactory;
import org.delia.runner.StatementBuilderPlugin;

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
}
