package org.delia.runner;

import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.DBExecutor;
import org.delia.db.DBExecutorEx;
import org.delia.db.DBInterfaceFactory;
import org.delia.db.DBType;
import org.delia.dval.DeferredValueService;
import org.delia.error.ErrorTracker;
import org.delia.error.SimpleErrorTracker;
import org.delia.hld.DeliaExecutable;
import org.delia.hld.dat.DatService;
import org.delia.lld.LLD;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.validation.ValidationRunner;
import org.delia.varevaluator.VarEvaluator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OuterRunnerInsertHelper extends ServiceBase {
    private final DBInterfaceFactory dbInterface;
    private VarEvaluator varEvaluator;
    private ExecutionState execState;
    private DeferredValueService deferredValueService;

    public OuterRunnerInsertHelper(FactoryService factorySvc, DBInterfaceFactory dbInterface, DatService datSvc) {
        super(factorySvc);
        this.dbInterface = dbInterface;
        this.deferredValueService = new DeferredValueService(factorySvc);
    }

    public DValue execBulkInsert(LLD.LLBulkInsert stmt, DBExecutor exec, DeliaExecutable executable,
                                 Map<String, ResultValue> varMap, ExecutionState execState, VarEvaluator varEvaluator) {
        this.execState = execState;
        this.varEvaluator = varEvaluator; //TODO this is probably not thread-safe!
        if (isMEMDb(exec)) {
            DValue resultVal = null;
            for (LLD.LLInsert insertStmt : stmt.insertStatements) {
                resultVal = execInsert(insertStmt, exec, executable, varMap, execState, varEvaluator);
            }
            return resultVal;
        } else {
            InsertResultSpec resultSpec = null;
            for (LLD.LLInsert insertStmt : stmt.insertStatements) {
                resultSpec = doExecInsertPrep(insertStmt, exec, executable, execState);
                if (resultSpec == null) {
                    return null;
                }
            }


            //TODO fix this
            //this is really wrong
            //i think we need to resolve vars and validate outside this layer for both insert and bulkInsert
            //and we need to generate the DVal

            LLD.LLInsert fakeInsert = new LLD.LLInsert(stmt.first.getLoc());
            fakeInsert.table = stmt.first.table;
            fakeInsert.fieldL = stmt.first.fieldL;
            fakeInsert.syntheticField = stmt.first.syntheticField;
            fakeInsert.setSql(stmt.getSql());
            return doExecInsert(exec, varMap, fakeInsert, resultSpec.dval, resultSpec.generatedId);
        }
    }

    public DValue execInsert(LLD.LLInsert stmt, DBExecutor exec, DeliaExecutable executable, Map<String, ResultValue> varMap, ExecutionState execState,
                             VarEvaluator varEvaluator) {
        this.execState = execState;
        this.varEvaluator = varEvaluator;
        InsertResultSpec resultSpec = doExecInsertPrep(stmt, exec, executable, execState);
        if (resultSpec == null) {
            return null;
        }
        return doExecInsert(exec, varMap, stmt, resultSpec.dval, resultSpec.generatedId);
    }

    private InsertResultSpec doExecInsertPrep(LLD.LLInsert stmt, DBExecutor exec, DeliaExecutable executable, ExecutionState execState) {
        ErrorTracker localET = new SimpleErrorTracker(log);
        resolveSprigRefs(stmt, execState);
        DsonToDValueConverter dsonConverter = new DsonToDValueConverter(factorySvc, localET, executable.registry, varEvaluator);
        DStructType structType = stmt.table.physicalType;

        ConversionResult cres = new ConversionResult();
        DValue dval = dsonConverter.convertOne(structType.getTypeName(), new DsonExp(stmt.fieldL), cres);

        InsertResultSpec resultSpec = new InsertResultSpec();
        if (dval != null) {
            resultSpec.dval = dval;
            if (exec instanceof DBExecutorEx) {
                DBExecutorEx execEx = (DBExecutorEx) exec;
                resultSpec.generatedId = execEx.execPreInsert(stmt, dval);
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
        return resultSpec;
    }

    private DValue doExecInsert(DBExecutor exec, Map<String, ResultValue> varMap, LLD.LLInsert stmt, DValue dval, DValue generatedId) {
        //Note. postgres doesn't use dval at all, since we've already generated stmt.sql
        DValue generatedId2 = exec.execInsert(stmt, dval);
        DValue finalGeneratedId = generatedId == null ? generatedId2 : generatedId;
        assignVar(OuterRunner.SERIAL_VAR, finalGeneratedId, varMap);

        if (stmt.syntheticField != null) {
            if (finalGeneratedId == null) {
                //error!
            } else {
                execState.sprigSvc.rememberSynthId(stmt.table.physicalType.getTypeName(), dval, finalGeneratedId, stmt.syntheticField.dval);
            }
        }

        return finalGeneratedId;
    }

    private boolean isMEMDb(DBExecutor exec) {
        return exec.getDbInterface().getDBType().equals(DBType.MEM);
    }

    private void resolveAllVars(List<LLD.LLFieldValue> fieldL, DTypeRegistry registry) {
        deferredValueService.resolveAllVars(fieldL, registry, varEvaluator);
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

    private void assignVar(String varName, DValue dval, Map<String, ResultValue> varMap) {
        ResultValue res = new ResultValue();
        res.ok = true;
        res.shape = dval == null ? null : dval.getType().getShape();
        res.val = dval;
        res.varName = varName;

        varMap.put(varName, res);
    }
}
