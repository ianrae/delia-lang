package org.delia.dbimpl.mysql;

import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.*;
import org.delia.dbimpl.mem.impl.DBStuff;
import org.delia.dbimpl.mem.impl.FKResolver;
import org.delia.hld.dat.DatService;
import org.delia.lld.LLD;
import org.delia.log.DeliaLog;
import org.delia.runner.DeliaRunner;
import org.delia.runner.QueryResponse;
import org.delia.sql.DBAccessContext;
import org.delia.sql.HLDResultSetConverter;
import org.delia.sql.ResultTypeInfo;
import org.delia.sql.SelectHelper;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.util.DeliaExceptionHelper;
import org.delia.varevaluator.DoNothingVarEvaluator;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class MySqlDBExecutor extends ServiceBase implements DBExecutor {

    private final MySqlDBInterfaceFactory dbInterface;
    private final FKResolver fkResolver;
    private final DeliaLog sqlLog;
    private final DBErrorConverter errorConverter;
    private DTypeRegistry registry;
    private DBStuff stuff;
    private DBConnection conn;
    private DatService datSvc;

    public MySqlDBExecutor(FactoryService factorySvc, DeliaLog sqlLog, MySqlDBInterfaceFactory dbInterface, DBConnection conn,
                           FKResolver fkResolver, DBErrorConverter errorConverter) {
        super(factorySvc);
        this.dbInterface = dbInterface;
        this.fkResolver = fkResolver;
        this.conn = conn;
        this.sqlLog = sqlLog;
        this.errorConverter = errorConverter;
    }

    @Override
    public DBConnection getDBConnection() {
        return conn;
    }

    @Override
    public DeliaLog getLog() {
        return sqlLog;
    }

    @Override
    public void execCreateSchema(LLD.LLCreateSchema stmt) {
        logSql(stmt);
        SqlStatement statement = stmt.getSql();
        DBExecuteContext dbctx = createContext(stmt);
        conn.execStatement(statement, dbctx);
    }

    private void logSql(LLD.LLStatement stmt) {
        SqlLogHelper.logSql(stmt, log);
    }

    @Override
    public void init1(DTypeRegistry registry, DatService datSvc, DeliaRunner deliaRunner) {
        this.registry = registry;
        this.datSvc = datSvc; //TODO probably should store it in here because same dbINterface might be used across different sessions
        this.errorConverter.setRegistry(registry);
        //deliaRunner not needed for postgres
    }

    @Override
    public void execCreateTable(LLD.LLCreateTable stmt) {
        logSql(stmt);
        SqlStatement statement = stmt.getSql();
        DBExecuteContext dbctx = createContext(stmt);
        conn.execStatement(statement, dbctx);
    }

    @Override
    public void execCreateAssocTable(LLD.LLCreateAssocTable stmt) {
        logSql(stmt);
        SqlStatement statement = stmt.getSql();
        DBExecuteContext dbctx = createContext(stmt);
        conn.execStatement(statement, dbctx);
    }

    @Override
    public DValue execInsert(LLD.LLInsert stmt, DValue dval) {
        logSql(stmt);
        //TODO: do we need to check structType or is checking fields ok?
        boolean isSpecialInsert = stmt.subQueryInfo != null;
        if (stmt.fieldL.isEmpty() && !isSpecialInsert && !stmt.areFieldsToInsert()) {
            et.add("cant-insert-empty-type", String.format("type '%s' has no fields. Can't execute insert.", stmt.getTableName()));
            return null;
        }

        DType keyType = null;// ctx.genKeytype;
        DBExecuteContext dbctxMain = null; //only one statement is allowed to generate keys
        DBExecuteContext dbctx = createContext(stmt);
        int n = conn.executeCommandStatementGenKey(stmt.getSql(), keyType, dbctx);
        dbctxMain = dbctx;

        DValue genVal = null;
        if (stmt.isSerialPK() && !dbctxMain.genKeysL.isEmpty()) {
            try {
                DBAccessContext dbactx = new DBAccessContext(registry, new DoNothingVarEvaluator());

                //we need to know index of the column in the sql table
                DStructType structType = (DStructType) dval.getType();
                List<String> alLFields = structType.getAllFields().stream().map(x -> x.name).collect(Collectors.toList());
                String pkField = stmt.getSerialPKPair().name;
                int pkFieldIndex = alLFields.indexOf(pkField);

                HLDResultSetConverter hldRSCconverter = createResultConverter();
                genVal = hldRSCconverter.extractGeneratedKey(dbctxMain.genKeysL, stmt.getSerialPKPair(), pkFieldIndex, dbactx);
            } catch (SQLException e) {
                DeliaExceptionHelper.throwError("extract-generated-key-failed", e.getMessage());
            }
        }
        return genVal;
    }

    @Override
    public void execDelete(LLD.LLDelete stmt) {
        logSql(stmt);
        SqlStatement statement = stmt.getSql();

        DBExecuteContext dbctx = createContext(stmt);
        conn.executeCommandStatement(statement, dbctx);   // *** call the DB ***
        //TODO: do we need to catch and interpret exceptions here??
    }

    @Override
    public void execUpdate(LLD.LLUpdate stmt) {
        logSql(stmt);
        SqlStatement statement = stmt.getSql();

        DBExecuteContext dbctx = createContext(stmt);
        conn.executeCommandStatement(statement, dbctx);   // *** call the DB ***
        //TODO: do we need to catch and interpret exceptions here??
    }

    @Override
    public void execUpsert(LLD.LLUpsert stmt, DValue dval) {
        logSql(stmt);
        SqlStatement statement = stmt.getSql();

        DBExecuteContext dbctx = createContext(stmt);
        conn.executeCommandStatement(statement, dbctx);   // *** call the DB ***
        //TODO: do we need to catch and interpret exceptions here??
    }

    protected DBExecuteContext createContext(LLD.LLStatement stmt) {
        DBExecuteContext dbctx = new DBExecuteContext();
        dbctx.logToUse = log;
        dbctx.currentStatement = stmt;
        return dbctx;
    }

//    /**
//     * Ugly. we need a serial provider per registry (really per runner i think)
//     */
//    protected DBStuff findOrCreateStuff() {
//        if (stuff == null) {
//            stuff = new DBStuff();
//            stuff.init(factorySvc, registry, dbInterface.getSerialMap());
//        }
//        return stuff;
//    }
//

    @Override
    public QueryResponse execSelect(LLD.LLSelect stmt, SelectDBContext ctx) {
        logSql(stmt);
        SqlStatement statement = stmt.getSql();

        DBExecuteContext dbctx = createContext(stmt);
        ResultSet rs = conn.execQueryStatement(statement, dbctx);   // *** call the DB ***
        //TODO: do we need to catch and interpret exceptions here??

        QueryResponse qresp = new QueryResponse();
        SelectHelper selectHelper = new SelectHelper(factorySvc, registry);
        ResultTypeInfo selectResultType = selectHelper.getSelectResultType(stmt);
        DBAccessContext dbactx = new DBAccessContext(registry, new DoNothingVarEvaluator());
        HLDResultSetConverter hldRSCconverter = createResultConverter();
        boolean isScalar = stmt.resultType.isScalarShape();
        if (isScalar) {
            qresp.dvalList = hldRSCconverter.buildScalarResult(rs, selectResultType, dbactx);
            qresp.ok = true;
        } else {
            qresp.dvalList = hldRSCconverter.buildDValueList(rs, dbactx, stmt);
            qresp.ok = true;
        }
        return qresp;
    }

    private HLDResultSetConverter createResultConverter() {
        ValueHelper valueHelper = new ValueHelper(factorySvc); //conn.createValueHelper;
        HLDResultSetConverter hldRSCconverter = new HLDResultSetConverter(factorySvc, valueHelper, registry, datSvc);
        return hldRSCconverter;
    }

    @Override
    public DBInterfaceFactory getDbInterface() {
        return dbInterface;
    }

    @Override
    public void close() throws Exception {
        conn.close();
    }
}
