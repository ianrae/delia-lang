package org.delia.varevaluator;

import org.delia.runner.ExecutionState;
import org.delia.runner.ResultValue;
import org.delia.type.DValue;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ExecStateVarEvaluator implements VarEvaluator {
    private final ExecutionState execState;
    private Map<String, ResultValue> varMap;

    public ExecStateVarEvaluator(ExecutionState execState) {
        this.execState = execState;
        this.varMap = execState.varMap;
    }

    @Override
    public boolean exists(String varName) {
        if (varMap == null) return false;
        return varMap.containsKey(varName);
    }

    @Override
    public List<DValue> lookupVar(String varName) {
        if (varMap == null) return null;

        ResultValue res = varMap.get(varName);
        if (res == null) return null;

        List<DValue> list = res.getAsDValueList();
        if (list == null) {
            list = Collections.singletonList(res.getAsDValue());
        }
        return list;
    }

    @Override
    public ResultValue lookupVarAsResultValue(String varName) {
        if (varMap == null) return null;

        ResultValue res = varMap.get(varName);
        return res;
    }
}
