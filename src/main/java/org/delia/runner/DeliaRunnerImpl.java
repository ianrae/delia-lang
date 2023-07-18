package org.delia.runner;

import org.delia.Delia;
import org.delia.DeliaFactory;
import org.delia.DeliaOptions;
import org.delia.DeliaSession;
import org.delia.api.DeliaSessionImpl;
import org.delia.compiler.ast.AST;
import org.delia.compiler.impl.CompilerResults;
import org.delia.core.FactoryService;
import org.delia.core.FactoryServiceImpl;
import org.delia.db.DBCapabilties;
import org.delia.db.DBInterfaceFactory;
import org.delia.db.sql.ConnectionDefinition;
import org.delia.error.DeliaError;
import org.delia.error.ErrorFormatterImpl;
import org.delia.error.ErrorTracker;
import org.delia.error.SimpleErrorTracker;
import org.delia.hld.DeliaExecutable;
import org.delia.hld.HLDBuilder;
import org.delia.hld.HLDFirstPassResults;
import org.delia.hld.dat.SyntheticDatService;
import org.delia.log.DeliaLog;
import org.delia.transaction.TransactionAdapter;
import org.delia.type.DTypeRegistry;
import org.delia.type.DTypeRegistryBuilder;
import org.delia.type.DValue;
import org.delia.valuebuilder.ScalarValueBuilder;
import org.delia.varevaluator.DoNothingVarEvaluator;
import org.delia.varevaluator.SessionVarEvaluator;
import org.delia.varevaluator.VarEvaluator;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DeliaRunnerImpl implements DeliaRunner {
    private final AdditionalCompilerPasses additionalCompilerPasses;
    private boolean runDetached;
    private FactoryService factorySvc;
    private DeliaLog log;
    private Delia delia;
    private SyntheticDatService datSvc = new SyntheticDatService();
    private DeliaSession existingSession; //can be null
    private DBInterfaceFactory dbInterface;

    public DeliaRunnerImpl(ConnectionDefinition connDef, DeliaLog log) {
        ErrorTracker et = new SimpleErrorTracker(log);
        this.factorySvc = new FactoryServiceImpl(log, et);
        delia = DeliaFactory.create(connDef, log, factorySvc);

        //auto-sort results by id (makes bdd files simpler)
        delia.getOptions().autoSortByPK = true;
        dbInterface = delia.getDBInterface();
        this.additionalCompilerPasses = new AdditionalCompilerPasses(factorySvc);
    }

    public DeliaRunnerImpl(DeliaSession existingSession, boolean runDetached) {
        this.existingSession = existingSession;
        this.runDetached = runDetached;
        this.delia = existingSession.getDelia();
        datSvc = (SyntheticDatService) existingSession.getDatIdMap();

        this.factorySvc = delia.getFactoryService();
        this.additionalCompilerPasses = new AdditionalCompilerPasses(factorySvc);

        log = delia.getLog();
        //auto-sort results by id (makes bdd files simpler)
        delia.getOptions().autoSortByPK = true;

        dbInterface = delia.getDBInterface();
        DeliaSessionImpl sessimpl = (DeliaSessionImpl) existingSession;
        if (sessimpl.transactionProvider instanceof TransactionAdapter) {
            TransactionAdapter transactionAdapter = (TransactionAdapter) sessimpl.transactionProvider;
            log.log("(runner)transaction!..");
            dbInterface = transactionAdapter.getTransactionAwareDBInterface();
        }
    }

    @Override
    public Delia getDelia() {
        return delia;
    }

    @Override
    public ScalarValueBuilder createValueBuilder() {
        DTypeRegistry registry = null;
        if (existingSession != null) {
            registry = existingSession.getExecutionContext().registry;
        } else {
            //create a basic registry (built-in types only). enough to get started
            DTypeRegistryBuilder registryBuilder = new DTypeRegistryBuilder();
            registryBuilder.init();
            registry = registryBuilder.getRegistry();
        }
        return new ScalarValueBuilder(factorySvc, registry);
    }

    private VarEvaluator createVarEvaluator() {
        if (existingSession == null) {
            return new DoNothingVarEvaluator();
        }
        return new SessionVarEvaluator(existingSession);
    }

    @Override
    public HLDFirstPassResults buildFirstPassResults(AST.DeliaScript script) {
        DBCapabilties capabilties = dbInterface.getCapabilities();
        HLDBuilder hldBuilder = new HLDBuilder(factorySvc, datSvc, delia.getOptions(), capabilties.getDefaultSchema());
        DTypeRegistry existingRegistry = existingSession == null ? null : existingSession.getRegistry();
        HLDFirstPassResults firstPassResults = hldBuilder.buildTypesOnly(script, existingRegistry);
        return firstPassResults;
    }

    @Override
    public AST.DeliaScript compile(String deliaSrc, ErrorTracker et) {
        DeliaStatementBuilder dsb = new DeliaStatementBuilder(factorySvc);
        CompilerResults results = dsb.compile(deliaSrc);
        if (results == null) {
            //TODO learn how to get syntax errors from antlr
            DeliaError err = new DeliaError("delia-syntax-error", "syntax error: details not available");
            et.add(err);
            return null;
        }
        AST.DeliaScript script = dsb.buildScript(results);
        script.errorFormatter = new ErrorFormatterImpl(log);
        script.errorFormatter.setDeliaSrc(deliaSrc);

        return script;
    }

    @Override
    public DeliaExecutable buildExecutable(AST.DeliaScript script) {
        VarEvaluator varEvaluator = createVarEvaluator();
        SyntheticDatService datSvc = new SyntheticDatService();
        Map<String, String> syntheticIdMap = new HashMap<>(); //TODO do we need a real one?

        DBCapabilties capabilties = dbInterface.getCapabilities();
        ExecutableBuilder execBuilder = new ExecutableBuilder(factorySvc, datSvc, varEvaluator, getOptions(delia, existingSession), syntheticIdMap, capabilties.getDefaultSchema());
        HLDFirstPassResults firstPassResults = buildFirstPassResults(script);

        String currentSchema = calcCurentSchema();
        additionalCompilerPasses.runAdditionalCompilerPasses(script, firstPassResults, currentSchema, dbInterface.getDBType(), capabilties.getDefaultSchema());

        DeliaExecutable executable = execBuilder.buildFromScript(script, firstPassResults, dbInterface.getDBType());
        return executable;
    }
    private DeliaOptions getOptions(Delia delia, DeliaSession sessionParam) {
        if (sessionParam != null && sessionParam.getSessionOptions() != null) {
            return sessionParam.getSessionOptions();
        }
        return delia.getOptions();
    }

    private String calcCurentSchema() {
        String currentSchema = existingSession == null ? delia.getOptions().defaultSchema : existingSession.getExecutionContext().currentSchema;
        return currentSchema;
    }

    @Override
    public DeliaSession execute(DeliaExecutable executable) {
        OuterRunner runner = new OuterRunner(factorySvc, dbInterface, datSvc);
        DeliaSessionImpl session = null;

        //runDetached means we don't want to affect vars or $$ in the outer session
        if (existingSession == null || runDetached) {
            session = new DeliaSessionImpl(delia);
            session.execCtx = runner.createNewExecutionState(executable.registry);
            session.execCtx.currentSchema = existingSession == null ? null : existingSession.getExecutionContext().currentSchema;
            session.datSvc = executable.datSvc;
        } else {
            session = (DeliaSessionImpl) existingSession;
            session.execCtx = existingSession.getExecutionContext();
        }

        //copy over sessionOptions if detached
        if (runDetached && existingSession != null) {
            session.sessionOptions = ((DeliaSessionImpl)existingSession).sessionOptions;
        }

        session.execCtx.deliaRunner = this;
        session.mostRecentExecutable = executable;

        // ** run it **
        executable.inTransaction = false;
        boolean isNewSession = existingSession == null;
        if (delia.getOptions().executeInTransaction) {
            final DeliaSessionImpl sessFinal = session;
            session.runInTransactionVoid(() -> {
                executable.inTransaction = true;
                runExecutable(runner, executable, sessFinal, isNewSession);
            });
        } else {
            runExecutable(runner, executable, session, isNewSession);
        }

        return session;
    }

    private void runExecutable(OuterRunner runner, DeliaExecutable executable, DeliaSessionImpl session, boolean isNewSession) {
        session.execCtx.enableRemoveFks = false;
        BasicRunnerResults res = runner.executeOnDBInterface(executable, session.execCtx, this.delia.getOptions(), isNewSession);
        session.execCtx.enableRemoveFks = true; //restore

        session.ok = true;
        session.res = buildResultVal(res);
        //        session.expL = deliaOptions.saveParseExpObjectsInSession ? expL : null;
        //        session.datIdMap = extraInfo.datIdMap;
        //        session.mostRecentRunner = mainRunner;
        session.zoneId = factorySvc.getTimeZoneService().getDefaultTimeZone();
        existingSession = session;
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
}
