package org.delia.runner;

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
import org.delia.sql.SqlValueRenderer;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.validation.ValidationRunner;
import org.delia.valuebuilder.ScalarValueBuilder;
import org.delia.varevaluator.VarEvaluator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OuterRunnerInsertHelper extends ServiceBase {
    public static final String DOLLAR_DOLLAR = "$$";
    public static final String SERIAL_VAR = "_serial";

    private final DBInterfaceFactory dbInterface;
    private final ConfigureService configSvc;
    private final DatService datSvc;
    private final DValueConverterService dvalConverterService;
    private VarEvaluator varEvaluator;
//    private SqlValueRenderer sqlValueRenderer;
    private ExecutionState execState;
//    private QueryResponse hackQResp; //TOD: remove this by fixing code!

    public OuterRunnerInsertHelper(FactoryService factorySvc, DBInterfaceFactory dbInterface, DatService datSvc) {
        super(factorySvc);
        this.dbInterface = dbInterface;
        this.configSvc = factorySvc.getConfigureService();
        this.datSvc = datSvc;
//        this.sqlValueRenderer = new SqlValueRenderer(factorySvc);
        this.dvalConverterService = new DValueConverterService(factorySvc);
        //clear error tracker.
        factorySvc.getErrorTracker().clear();
        //TODO fix this so don't have shared error tracker -- too many leftover errors!
    }
    private boolean isMEMDb(DBExecutor exec) {
        return exec.getDbInterface().getDBType().equals(DBType.MEM);
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


    private DValue doExecBulkInsert(LLD.LLBulkInsert stmt, DBExecutor exec, DeliaExecutable executable, Map<String, ResultValue> varMap, ExecutionState execState) {
        if (isMEMDb()) {
            DValue resultVal = null;
            for (LLD.LLInsert insertStmt : stmt.insertStatements) {
                resultVal = doExecInsert(insertStmt, exec, executable, varMap, execState);
            }
            return resultVal;
        } else {

        }
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


    private void assignVar(String varName, DValue dval, Map<String, ResultValue> varMap) {
        ResultValue res = new ResultValue();
        res.ok = true;
        res.shape = dval == null ? null : dval.getType().getShape();
        res.val = dval;
        res.varName = varName;

        varMap.put(varName, res);
    }
}
