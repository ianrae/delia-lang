package org.delia.runner;

import org.delia.Delia;
import org.delia.DeliaOptions;
import org.delia.DeliaSession;
import org.delia.api.DeliaSessionImpl;
import org.delia.api.InjectedVarMap;
import org.delia.compiler.ast.AST;
import org.delia.compiler.impl.CompilerResults;
import org.delia.compiler.impl.DeliaParseException;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.DBCapabilties;
import org.delia.db.DBInterfaceFactory;
import org.delia.db.DBType;
import org.delia.error.DeliaError;
import org.delia.hld.DeliaExecutable;
import org.delia.hld.HLDBuilder;
import org.delia.hld.HLDFirstPassResults;
import org.delia.hld.dat.DatService;
import org.delia.hld.dat.SyntheticDatService;
import org.delia.transaction.TransactionAdapter;
import org.delia.type.DTypeRegistry;
import org.delia.type.DTypeRegistryBuilder;
import org.delia.type.DValue;
import org.delia.valuebuilder.ScalarValueBuilder;
import org.delia.varevaluator.SessionVarEvaluator;
import org.delia.varevaluator.VarEvaluator;

import java.util.Collections;
import java.util.Map;

public class DeliaSessionRunnerShim extends ServiceBase {

    private final InjectedVarMap injectedVarMap;
    private final AdditionalCompilerPasses additionalCompilerPasses;

    public DeliaSessionRunnerShim(FactoryService factorySvc, InjectedVarMap injectedVarMap) {
        super(factorySvc);
        this.injectedVarMap = injectedVarMap;
        this.additionalCompilerPasses = new AdditionalCompilerPasses(factorySvc);
    }

    public DeliaSession beginSession(Delia delia, String src) {
        return doSession(delia, src, null);
    }

    public DeliaSession continueSession(Delia delia, String src, DeliaSession session) {
        return doSession(delia, src, session);
    }

    private DeliaSession doSession(Delia delia, String src, DeliaSession sessionParam) {
        DeliaSession sess = null;
        try {
            sess = doSessionOrThrow(delia, src, sessionParam);
        } catch (DeliaException e) {
            //TODO later get all errors from exception
            //TODO also perhaps map all errors to a delia-compile-error??
            log.logError("DeliaException!: %d errors", e.getErrorCount());
            for (DeliaError err : e.getErrors()) {
                log.logError("  %s", err.toString());
            }

            DeliaError err = e.getFirstError();
            return buildFailedSession(err, delia);
        } catch (DeliaParseException e) {
            //TODO later get all errors from exception
            //TODO also perhaps map all errors to a delia-compile-error??
            log.logError("DeliaParseException!");
             log.logError("  %s", e.getMessage());

            DeliaError err = new DeliaError("parse-error", e.getMessage());
            return buildFailedSession(err, delia);
        }
        return sess;
    }

    private DeliaSession doSessionOrThrow(Delia delia, String src, DeliaSession sessionParam) {
        DeliaStatementBuilder dsb = new DeliaStatementBuilder(factorySvc);
        if (delia.getOptions().statementBuilderPlugin != null) {
            dsb.setPlugin(delia.getOptions().statementBuilderPlugin);
        }
        CompilerResults results = dsb.compile(src);
        if (results == null) {
            //TODO learn how to get syntax errors from antlr
            DeliaError err = new DeliaError("delia-syntax-error", "syntax error: details not available");
            return buildFailedSession(err, delia);
        }

        AST.DeliaScript script = dsb.buildScript(results);

//        //TODO. later!! 1st pass
//        TransactionProvider tpEIT = createTransactionProviderIfExecuteInTransaction();
//        Runner mainRunner = createRunner(null, blobLoader, tpEIT);
//        execTypes(mainRunner, expL);
//        MigrationExtraInfo extraInfo = new MigrationExtraInfo();
//        ResultValue migrationPlanRes = doPass3AndDBMigration(src, expL, mainRunner, plan, extraInfo);
//        if (migrationPlanRes != null) {
//            DeliaSessionImpl session = new DeliaSessionImpl(this);
//            session.execCtx = mainRunner.getExecutionState();
//            session.ok = true;
//            session.res = migrationPlanRes;
//            session.expL = deliaOptions.saveParseExpObjectsInSession ? expL : null;
//            session.datIdMap = extraInfo.datIdMap;
//            session.mostRecentRunner = mainRunner;
//            session.zoneId = factorySvc.getTimeZoneService().getDefaultTimeZone();
//            return session;
//        }

        //        ResultValue res = doExecute(mainRunner, expL, extraInfo.datIdMap, tpEIT);
        DBType dbType = delia.getDBInterface().getDBType();
        SyntheticDatService datSvc = sessionParam == null ? new SyntheticDatService() : (SyntheticDatService) sessionParam.getDatIdMap();
        DeliaExecutable executable = buildExecutable(script, dbType, sessionParam, delia, datSvc);
        if (executable == null) {
            return null;
        }

        DBInterfaceFactory dbInterface = delia.getDBInterface();
        if (sessionParam != null) {
            DeliaSessionImpl sessimpl = (DeliaSessionImpl) sessionParam;
            if (sessimpl.transactionProvider instanceof TransactionAdapter) {
                TransactionAdapter transactionAdapter = (TransactionAdapter) sessimpl.transactionProvider;
                log.log("transaction!..");
                dbInterface = transactionAdapter.getTransactionAwareDBInterface();
            }
        }

        OuterRunner runner = new OuterRunner(factorySvc, dbInterface, datSvc);
        DeliaSessionImpl session = null;
        if (sessionParam == null) {
            session = new DeliaSessionImpl(delia);
            session.execCtx = runner.createNewExecutionState(executable.registry);
            session.datSvc = executable.datSvc;
            session.beginScript = delia.getOptions().saveParseScriptInSession ? script : null;
        } else {
            session = (DeliaSessionImpl) sessionParam;
            session.execCtx = sessionParam.getExecutionContext();
            session.mostRecentContinueScript = delia.getOptions().saveParseScriptInSession ? script : null;
        }
        session.mostRecentExecutable = executable;

        executable.inTransaction = false;
        if (delia.getOptions().executeInTransaction) {
            final DeliaSessionImpl sessFinal = session;
            session.runInTransactionVoid(() -> {
                executable.inTransaction = true;
                runExecutable(executable, runner, sessFinal, sessionParam, delia.getOptions());
            });
        } else {
            runExecutable(executable, runner, session, sessionParam, delia.getOptions());
        }
        return session;
    }

    private void runExecutable(DeliaExecutable executable, OuterRunner runner, DeliaSessionImpl session, DeliaSession sessionParam, DeliaOptions options) {
        session.execCtx.deliaRunner = new DeliaRunnerImpl(session, true); //run detached (safer)
        //TODO runner should run in transaction if is one

        //merge
        if (!injectedVarMap.varMap.isEmpty()) {
            for (String varName : injectedVarMap.varMap.keySet()) {
                ResultValue rrr = injectedVarMap.varMap.get(varName);
                DValue dval = rrr == null ? null : rrr.getAsDValue();
                log.log("inject var '%s' = '%s'", varName, dval == null ? "null" : dval.asString());
            }
        }
        session.execCtx.varMap.putAll(injectedVarMap.getMap());
        BasicRunnerResults res = runner.executeOnDBInterface(executable, session.execCtx, options, (sessionParam == null));

        session.ok = true; //TODO should this be false if res contains errors?
        session.res = buildResultVal(res);
//        session.expL = deliaOptions.saveParseExpObjectsInSession ? expL : null;
//        session.datIdMap = extraInfo.datIdMap;
//        session.mostRecentRunner = mainRunner;
        session.zoneId = factorySvc.getTimeZoneService().getDefaultTimeZone();
    }

    private DeliaSession buildFailedSession(DeliaError err, Delia delia) {
        DeliaSessionImpl session = new DeliaSessionImpl(delia);
        session.execCtx = null;
        session.ok = false;
        ResultValue res = new ResultValue();
        session.res = res;
        res.ok = false;
        res.errors = Collections.singletonList(err);
        return session;
    }

    private ResultValue buildResultVal(BasicRunnerResults rres) {
        ResultValue res = new ResultValue();
        if (rres.qresp == null) {
            res.ok = true; //TODO how detect errors
            DValue dval = rres.insertResultVal;
            res.val = dval;
            res.shape = (res.val == null) ? null : dval.getType().getShape();
        } else {
            res.ok = rres.qresp.ok;
            res.val = rres.qresp;
            res.errors = rres.qresp.err == null ? Collections.emptyList() : Collections.singletonList(rres.qresp.err);
        }
        return res;
    }

    private DeliaExecutable buildExecutable(AST.DeliaScript script, DBType dbType, DeliaSession sessionParam, Delia delia, DatService datSvc) {
        VarEvaluator varEvaluator = createVarEvaluator(sessionParam, delia);
        DBCapabilties capabilties = delia.getDBInterface().getCapabilities();
        HLDBuilder hldBuilder = new HLDBuilder(factorySvc, datSvc, delia.getOptions(), capabilties.getDefaultSchema());
        DTypeRegistry existingRegistry = sessionParam == null ? null : sessionParam.getRegistry();
        if (existingRegistry != null && existingRegistry.getAll().size() == DTypeRegistry.NUM_BUILTIN_TYPES) {
            existingRegistry = null;
        }
        HLDFirstPassResults firstPassResults = hldBuilder.buildTypesOnly(script, existingRegistry);
        Map<String, String> syntheticIdMaps = hldBuilder.gatherSyntheticIds(script, existingRegistry);
        String currentSchema = calcCurrentSchema(sessionParam, delia);

        additionalCompilerPasses.runAdditionalCompilerPasses(script, firstPassResults, currentSchema, dbType, capabilties.getDefaultSchema());

        ExecutableBuilder execBuilder = new ExecutableBuilder(factorySvc, datSvc, varEvaluator, delia.getOptions(), syntheticIdMaps, capabilties.getDefaultSchema());
        DeliaExecutable executable = execBuilder.buildFromScript(script, firstPassResults, dbType);
        executable.datSvc = datSvc;
        return executable;
    }

    private String calcCurrentSchema(DeliaSession sessionParam, Delia delia) {
         String currentSchema = sessionParam == null ? delia.getOptions().defaultSchema : sessionParam.getExecutionContext().currentSchema;
        DBCapabilties capabilties = delia.getDBInterface().getCapabilities();
        return currentSchema;
    }

    private VarEvaluator createVarEvaluator(DeliaSession sessionParam, Delia delia) {
        if (sessionParam == null) {
            DeliaSessionImpl sess = new DeliaSessionImpl(delia);
            sess.execCtx = new ExecutionState();
            sess.execCtx.varMap.putAll(injectedVarMap.getMap());
            sessionParam = sess;
        }
        return new SessionVarEvaluator(sessionParam);
    }

    protected ScalarValueBuilder createValueBuilder() {
        DTypeRegistryBuilder registryBuilder = new DTypeRegistryBuilder();
        registryBuilder.init();
        DTypeRegistry registry = registryBuilder.getRegistry();
        return new ScalarValueBuilder(factorySvc, registry);
    }

}
