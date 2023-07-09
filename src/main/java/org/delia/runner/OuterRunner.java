package org.delia.runner;

import org.delia.DeliaOptions;
import org.delia.core.ConfigureService;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.*;
import org.delia.dval.DValueConverterService;
import org.delia.error.ErrorTracker;
import org.delia.error.SimpleErrorTracker;
import org.delia.hld.DeliaExecutable;
import org.delia.hld.dat.DatService;
import org.delia.lld.LLD;
import org.delia.sprig.SprigServiceImpl;
import org.delia.sql.SqlValueRenderer;
import org.delia.tok.Tok;
import org.delia.tok.TokValueVisitor;
import org.delia.type.*;
import org.delia.util.DeliaExceptionHelper;
import org.delia.validation.ValidationRunner;
import org.delia.valuebuilder.ScalarValueBuilder;
import org.delia.varevaluator.ExecStateVarEvaluator;
import org.delia.varevaluator.VarEvaluator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OuterRunner extends ServiceBase {
    public static final String DOLLAR_DOLLAR = "$$";
    public static final String SERIAL_VAR = "_serial";

    private final DBInterfaceFactory dbInterface;
    private final ConfigureService configSvc;
    private final DatService datSvc;
    private final DValueConverterService dvalConverterService;
    private VarEvaluator varEvaluator;
    private SqlValueRenderer sqlValueRenderer;
    private ExecutionState execState;
    private QueryResponse hackQResp; //TOD: remove this by fixing code!

    public OuterRunner(FactoryService factorySvc, DBInterfaceFactory dbInterface, DatService datSvc) {
        super(factorySvc);
        this.dbInterface = dbInterface;
        this.configSvc = factorySvc.getConfigureService();
        this.datSvc = datSvc;
        this.sqlValueRenderer = new SqlValueRenderer(factorySvc);
        this.dvalConverterService = new DValueConverterService(factorySvc);
        //clear error tracker.
        factorySvc.getErrorTracker().clear();
        //TODO fix this so don't have shared error tracker -- too many leftover errors!
    }

    public BasicRunnerResults executeOnDBInterface(DeliaExecutable executable, ExecutionState execState, DeliaOptions options, boolean isNewSession) {
        this.execState = execState;
        BasicRunnerResults res = null;
        try (DBExecutor exec = dbInterface.createExecutor()) {
            res = doExecuteOnDBInterface(exec, executable, execState, options, isNewSession);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.logError("executeOnDBInterface: Unexpected error", e);
        }
        return res; //can return
    }

    private BasicRunnerResults doExecuteOnDBInterface(DBExecutor exec, DeliaExecutable executable, ExecutionState execState, DeliaOptions options, boolean isNewSession) {
        exec.init1(executable.registry, datSvc, execState.deliaRunner);
        if (executable.inTransaction) {
            log.logDebug(".......exec(transaction)......." + (isNewSession ? "NEW" : "CONTINUE"));
        } else {
            log.logDebug(".......exec......." + (isNewSession ? "NEW" : "CONTINUE"));
        }
        DValue insertOrOtherDval = null;
        QueryResponse qresp = null;

        Map<String, ResultValue> varMap = execState.varMap;
        varEvaluator = new ExecStateVarEvaluator(execState);
        if (options.customVarEvaluatorFactory != null) {
            varEvaluator = options.customVarEvaluatorFactory.create(varEvaluator);
        }
        execState.currentSchema = options.defaultSchema; //ok if schema is null

        //MEM doesn't but POSTGRES does
        boolean requiresSql = dbInterface.getCapabilities().isRequiresSql();

        for (LLD.LLStatement stmt : executable.lldStatements) {
            if (requiresSql && stmt.requiresSql() && stmt.getSql() == null) {
                if (stmt instanceof LLD.LLCreateSchema) {
                    doExecSchemaOff(execState);
                }
                continue; //nothing to do
            }

            log.log(stmt.toString());
            if (stmt instanceof LLD.LLInsert) {
                insertOrOtherDval = doExecInsert((LLD.LLInsert) stmt, exec, executable, varMap, execState);
                assignVar(DOLLAR_DOLLAR, insertOrOtherDval, varMap);
            } else if (stmt instanceof LLD.LLBulkInsert) {
                insertOrOtherDval = doExecBulkInsert((LLD.LLBulkInsert) stmt, exec, executable, varMap, execState);
                assignVar(DOLLAR_DOLLAR, insertOrOtherDval, varMap);
            } else if (stmt instanceof LLD.LLSelect) {
                qresp = doExecSelect((LLD.LLSelect) stmt, exec, executable, execState);
                LLD.LLSelect llsel = (LLD.LLSelect) stmt;
                if (llsel.varName != null) {
                    assignVar(llsel.varName, qresp, varMap);
                }
                assignVar(DOLLAR_DOLLAR, qresp, varMap);
            } else if (stmt instanceof LLD.LLDelete) {
                doExecDelete((LLD.LLDelete) stmt, exec, executable);
            } else if (stmt instanceof LLD.LLUpdate) {
                doExecUpdate((LLD.LLUpdate) stmt, exec, executable);
            } else if (stmt instanceof LLD.LLUpsert) {
                doExecUpsert((LLD.LLUpsert) stmt, exec, executable);
            } else if (stmt instanceof LLD.LLCreateTable) {
                doExecCreateTable((LLD.LLCreateTable) stmt, exec);
            } else if (stmt instanceof LLD.LLCreateAssocTable) {
                doExecCreateAssocTable((LLD.LLCreateAssocTable) stmt, exec);
            } else if (stmt instanceof LLD.LLCreateSchema) {
                doExecCreateSchema((LLD.LLCreateSchema) stmt, exec, execState);
            } else if (stmt instanceof LLD.LLConfigure) {
                doConfigure((LLD.LLConfigure) stmt, executable.registry, execState);
            } else if (stmt instanceof LLD.LLLog) {
                doLog((LLD.LLLog) stmt, executable.registry);
            } else if (stmt instanceof LLD.LLAssign) {
                hackQResp = null;
                insertOrOtherDval = doAssign((LLD.LLAssign) stmt, executable.registry, varMap);
                if (hackQResp != null) {
                    qresp = hackQResp;
                }
            } else {
                DeliaExceptionHelper.throwError("unknown-LLD-type: %s", stmt.getClass().getSimpleName());
            }

            if (!et.areNoErrors()) {
                BasicRunnerResults res = new BasicRunnerResults();
                res.qresp = new QueryResponse();
                res.qresp.err = et.getFirstError(); //et.getLastError(); //TODO: propogate all errors
                return res;
            }

        }

        BasicRunnerResults res = new BasicRunnerResults();
        res.insertResultVal = insertOrOtherDval;
        res.qresp = qresp;
        return res;
    }

    private DValue doAssign(LLD.LLAssign stmt, DTypeRegistry registry, Map<String, ResultValue> varMap) {
        DValue insertOrOtherDval = null;
        String varName = stmt.rhsExpr;
        String subName = null;
        if (stmt.rhsExpr != null) {
            if (stmt.rhsExpr.contains(".")) {
                String[] ar = stmt.rhsExpr.split("\\.");
                //TODO handle more than two later!!
                varName = ar[0];
                subName = ar[1];
            }


            List<DValue> list = varEvaluator.lookupVar(varName);
            if (list != null && list.size() == 1) {
                insertOrOtherDval = list.get(0);
                if (subName != null && insertOrOtherDval != null) {
                    insertOrOtherDval = insertOrOtherDval.asStruct().getField(subName);
                }
            } else {
                ResultValue res = varEvaluator.lookupVarAsResultValue(varName);
                if (res != null && res.val instanceof QueryResponse) {
                    hackQResp = (QueryResponse) res.val;
                    if (subName != null) {
                        List<DValue> innerValues = new ArrayList<>();
                        for (DValue dval : hackQResp.dvalList) {
                            DValue inner = dval.asStruct().getField(subName);
                            innerValues.add(inner);
                        }
                        hackQResp.dvalList = innerValues;
                    }
                }
                assignVarRaw(stmt.varName, res, varMap);
                assignVarRaw(DOLLAR_DOLLAR, res, varMap);
                //TODO: add code to validate contents of res
                return null; //TODO: is this ok?
            }
        } else {
            insertOrOtherDval = stmt.dvalue;
        }

        //validate integer effective-shape
        if (insertOrOtherDval != null && insertOrOtherDval.getType().isShape(Shape.INTEGER)) {
            ScalarValueBuilder valueBuilder = new ScalarValueBuilder(factorySvc, registry);
            if (!valueBuilder.checkIntegerEffectiveShape(insertOrOtherDval)) {
                String msg = String.format("int value %d too large to fit in Java int", insertOrOtherDval.asLong());
                et.add("int-too-large-for-effective-shape", msg).setLoc(stmt.getLoc());
            }
        }

        //validate type of assignment
        if (stmt.dtype != null && insertOrOtherDval != null) {
            DType type1 = stmt.dtype;
            DType type2 = insertOrOtherDval.getType();
            if (!type1.isAssignmentCompatible(type2)) {
                //scalar types: can do let car = vehicle because we are going to validate (below)
                //struct types: can't do reverse check because types may have different fields
                boolean isScalar = type1.isScalarShape();
                if (!isScalar || !type2.isAssignmentCompatible(type1)) {
                    String msg = String.format("Cannot assign let %s = %s", type1.getName(), type2.getName());
                    DeliaExceptionHelper.throwError(stmt.getLoc(), "incompatible-assignment", msg);
                }
            }
        }
        //TODO do validation
        ErrorTracker localET = new SimpleErrorTracker(log);
        validateDValue(insertOrOtherDval, localET, registry, false);
        if (!localET.areNoErrors()) {
            et.addAll(localET.getErrors());
            return insertOrOtherDval;
        }

        assignVar(stmt.varName, insertOrOtherDval, varMap);
        assignVar(DOLLAR_DOLLAR, insertOrOtherDval, varMap);
        return insertOrOtherDval;
    }

    private void doExecUpsert(LLD.LLUpsert stmt, DBExecutor exec, DeliaExecutable executable) {
        ErrorTracker localET = new SimpleErrorTracker(log);
        DsonToDValueConverter dsonConverter = new DsonToDValueConverter(factorySvc, localET, executable.registry, varEvaluator);
        DStructType structType = stmt.table.physicalType;
        ConversionResult cres = new ConversionResult();
        DValue dval = dsonConverter.convertOneUpsert(structType.getTypeName(), new DsonExp(stmt.fieldL), cres);
        if (dval != null) {
            validateDValueForUpdate(dval, localET, executable.registry);
        }
        if (!localET.areNoErrors()) {
            et.addAll(localET.getErrors());
            return;
        }

        //resolve all vars (which exist as DeferredDValues)
        resolveAllVars(stmt.fieldL, executable.registry);

        exec.execUpsert(stmt, dval);

    }

    private void doExecUpdate(LLD.LLUpdate stmt, DBExecutor exec, DeliaExecutable executable) {
        ErrorTracker localET = new SimpleErrorTracker(log);
        DsonToDValueConverter dsonConverter = new DsonToDValueConverter(factorySvc, localET, executable.registry, varEvaluator);
        DStructType structType = stmt.table.physicalType;
        ConversionResult cres = new ConversionResult();
        DValue dval = dsonConverter.convertOnePartial(structType.getTypeName(), new DsonExp(stmt.fieldL), cres);
        if (dval != null) {
            validateDValueForUpdate(dval, localET, executable.registry);
        }
        if (!localET.areNoErrors()) {
            et.addAll(localET.getErrors());
            return;
        }

        //resolve all vars (which exist as DeferredDValues)
        resolveAllVars(stmt.fieldL, executable.registry);

        exec.execUpdate(stmt);
    }

    private void resolveAllVars(List<LLD.LLFieldValue> fieldL, DTypeRegistry registry) {
        ScalarValueBuilder valueBuilder = new ScalarValueBuilder(factorySvc, registry);
        for (LLD.LLFieldValue field : fieldL) {
            if (field.dval != null) {
                resolveSingleDeferredVar(field.dval, valueBuilder);
            } else if (field.dvalList != null) {
                field.dvalList.forEach(d -> resolveSingleDeferredVar(d, valueBuilder));
            }
        }
    }

    private void resolveSingleDeferredVar(DValue dval, ScalarValueBuilder valueBuilder) {
        if (dval != null) {
            DValue realVal = DeferredDValueHelper.preResolveDeferredDval(dval, varEvaluator);
            realVal = dvalConverterService.normalizeValue(realVal, dval.getType(), valueBuilder);
            DeferredDValueHelper.resolveTo(dval, realVal); //note. realVal can be null
        }
    }

    private void resolveAllVars(SqlStatement sql, DTypeRegistry registry) {
        if (sql == null) return;
        ScalarValueBuilder valueBuilder = new ScalarValueBuilder(factorySvc, registry);
        List<DValue> newList = new ArrayList<>();
        for (DValue dval : sql.paramL) {
            if (dval != null) {
                DValue realVal = DeferredDValueHelper.preResolveDeferredDval(dval, varEvaluator);
                if (realVal != null) {
                    realVal = dvalConverterService.normalizeValue(realVal, dval.getType(), valueBuilder);
                } else {
                    realVal = dval;
                }

                newList.add(realVal);
            } else {
                newList.add(dval);
            }
        }

        sql.paramL = newList;
    }

    private void resolveAllVars(Tok.WhereTok whereClause, DTypeRegistry registry) {
        ScalarValueBuilder valueBuilder = new ScalarValueBuilder(factorySvc, registry);
//        MyValueVisitor visitor = new MyValueVisitor();
//        whereClause.visit(visitor);
        TokValueVisitor visitor = new TokValueVisitor();
        whereClause.visit(visitor, null);

        for (Tok.ValueTok vexp : visitor.allValues) {
            if (vexp.value != null) {
                DValue realVal = DeferredDValueHelper.preResolveDeferredDval(vexp.value, varEvaluator);
                if (realVal != null) {
                    vexp.value = dvalConverterService.normalizeValue(realVal, vexp.value.getType(), valueBuilder);
                }
            }
        }
    }

    private void doExecDelete(LLD.LLDelete stmt, DBExecutor exec, DeliaExecutable executable) {
        exec.execDelete(stmt);
    }

    private void doExecCreateSchema(LLD.LLCreateSchema stmt, DBExecutor exec, ExecutionState execState) {
        exec.execCreateSchema(stmt);
        execState.currentSchema = stmt.schema;
    }

    private void doExecSchemaOff(ExecutionState execState) {
        execState.currentSchema = null;
    }

    private void doExecCreateTable(LLD.LLCreateTable stmt, DBExecutor exec) {
        exec.execCreateTable(stmt);
    }

    private void doExecCreateAssocTable(LLD.LLCreateAssocTable stmt, DBExecutor exec) {
        exec.execCreateAssocTable(stmt);
    }

    private QueryResponse doExecSelect(LLD.LLSelect stmt, DBExecutor exec, DeliaExecutable executable, ExecutionState execState) {
        //resolve all vars (which exist as DeferredDValues)
        resolveAllVars(stmt.getSql(), executable.registry);
        if (isMEMDb(exec)) {
            resolveAllVars(stmt.whereTok, executable.registry);
        }

        SelectDBContext ctx = new SelectDBContext();
        ctx.enableRemoveFks = execState.enableRemoveFks;
        QueryResponse qresp = exec.execSelect(stmt, ctx);
        boolean validateSelectResults = true; //if we really trust the db, this could be false
        //TODO add validateSelectResults to DeliaOptions
        if (validateSelectResults && qresp != null) {
            ErrorTracker localET = new SimpleErrorTracker(log);
            for (DValue dval : qresp.dvalList) {
                validateDValue(dval, localET, executable.registry);
            }
            if (!localET.areNoErrors()) {
                et.addAll(localET.getErrors());
            }
        }
        return qresp;
    }

    private boolean isMEMDb(DBExecutor exec) {
        return exec.getDbInterface().getDBType().equals(DBType.MEM);
    }


    private DValue doExecBulkInsert(LLD.LLBulkInsert stmt, DBExecutor exec, DeliaExecutable executable, Map<String, ResultValue> varMap, ExecutionState execState) {
        DValue resultVal = null;
        for(LLD.LLInsert insertStmt: stmt.insertStatements) {
            resultVal = doExecInsert(insertStmt, exec, executable, varMap, execState);
        }
        return resultVal;
    }

    private DValue doExecInsert(LLD.LLInsert stmt, DBExecutor exec, DeliaExecutable executable, Map<String, ResultValue> varMap, ExecutionState execState) {
        ErrorTracker localET = new SimpleErrorTracker(log);
        resolveSprigRefs(stmt, execState);
        DsonToDValueConverter dsonConverter = new DsonToDValueConverter(factorySvc, localET, executable.registry, varEvaluator);
        DStructType structType = stmt.table.physicalType;
        ConversionResult cres = new ConversionResult();
        DValue dval = dsonConverter.convertOne(structType.getTypeName(), new DsonExp(stmt.fieldL), cres);
        DValue generatedId = null;
        if (dval != null) {
            if (exec instanceof DBExecutorEx) {
                DBExecutorEx execEx = (DBExecutorEx) exec;
                generatedId = execEx.execPreInsert(stmt, dval);
            }
            if (et.errorCount() > 0) {
                return null;
            }
            validateDValue(dval, localET, executable.registry);
        }
        if (!localET.areNoErrors()) {
            et.addAll(localET.getErrors());
            return null;
        }

        //resolve all vars (which exist as DeferredDValues)
        resolveAllVars(stmt.fieldL, executable.registry);

        //Note. postgres doesn't use dval at all, since we've already generated stmt.sql
        DValue generatedId2 = exec.execInsert(stmt, dval);
        DValue finalGeneratedId = generatedId == null ? generatedId2 : generatedId;
        assignVar(SERIAL_VAR, finalGeneratedId, varMap);

        if (stmt.syntheticField != null) {
            if (finalGeneratedId == null) {
                //error!
            } else {
                execState.sprigSvc.rememberSynthId(stmt.table.physicalType.getTypeName(), dval, finalGeneratedId, stmt.syntheticField.dval);
            }
        }

        return finalGeneratedId;
    }

    private void resolveSprigRefs(LLD.LLInsert stmt, ExecutionState execState) {
        int index = 0;
        for (LLD.LLFieldValue field : stmt.fieldL) {
            if (field.field.physicalPair.type.isStructShape()) {
                DStructType structType = (DStructType) field.field.physicalPair.type;
                if (execState.sprigSvc.haveEnabledFor(structType)) {
                    DValue resolvedValue = execState.sprigSvc.resolveSyntheticId(structType, field.dval.asString());
                    field.dval = resolvedValue; //actual PK
                    if (stmt.getSql() != null) {
                        List<DValue> newlist = new ArrayList<>();
                        for (int k = 0; k < stmt.getSql().paramL.size(); k++) {
                            if (k == index) {
                                newlist.add(resolvedValue);
                            } else {
                                newlist.add(stmt.getSql().paramL.get(k));
                            }
                        }
                        stmt.getSql().paramL = newlist;
                    }
                }
            }
            index++;
        }
    }

    //TODO: later move this to a base class so postgresInsert will use same code
    private boolean validateDValue(DValue dval, ErrorTracker localET, DTypeRegistry registry) {
        return validateDValue(dval, localET, registry, true);
    }

    private boolean validateDValue(DValue dval, ErrorTracker localET, DTypeRegistry registry, boolean insertFlag) {
        if (dval == null) return true;
        ValidationRunner validationRunner = factorySvc.createValidationRunner(dbInterface, registry, execState.deliaRunner);
        validationRunner.enableInsertFlag(insertFlag);
        if (!validationRunner.validateDVal(dval)) {
            validationRunner.propogateErrors(localET);
            return false;
        }
        return true;
    }

    private void validateDValueForUpdate(DValue dval, ErrorTracker localET, DTypeRegistry registry) {
        //validate the fields of the partial DValue
        ValidationRunner validationRunner = factorySvc.createValidationRunner(dbInterface, registry, execState.deliaRunner);
        if (!validationRunner.validateFieldsOnly(dval)) {
            validationRunner.propogateErrors(localET);
        }

        //then validate the affected rules (of the struct)
        //We determine the rules dependent on each field in partial dval
        //and execute those rules only
        if (!validationRunner.validateDependentRules(dval)) {
            validationRunner.propogateErrors(localET);
        }

    }


    private void doConfigure(LLD.LLConfigure stmt, DTypeRegistry registry, ExecutionState execState) {
        configSvc.execute(stmt, registry, execState.sprigSvc);
    }

    private void doLog(LLD.LLLog stmt, DTypeRegistry registry) {
        //TODO: support log x  varname later
        if (stmt.dvalue != null) {
            log.log("%s", stmt.dvalue.asString());
        }
    }


    private void assignVar(String varName, QueryResponse qresp, Map<String, ResultValue> varMap) {
        ResultValue res = new ResultValue();
        res.ok = true;
        res.shape = null;
        res.val = qresp;
        res.varName = varName;

        varMap.put(varName, res);
    }

    private void assignVar(String varName, DValue dval, Map<String, ResultValue> varMap) {
        ResultValue res = new ResultValue();
        res.ok = true;
        res.shape = dval == null ? null : dval.getType().getShape();
        res.val = dval;
        res.varName = varName;

        varMap.put(varName, res);
    }

    private void assignVarRaw(String varName, ResultValue res, Map<String, ResultValue> varMap) {
        varMap.put(varName, res);
    }

    public ExecutionState createNewExecutionState(DTypeRegistry registry) {
        ExecutionState execState = new ExecutionState();
        execState.registry = registry;
        execState.sprigSvc = new SprigServiceImpl(factorySvc, registry);
        return execState;
    }

}
