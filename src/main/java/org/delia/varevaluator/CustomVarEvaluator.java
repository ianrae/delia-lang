package org.delia.varevaluator;

import org.delia.runner.ResultValue;
import org.delia.type.DValue;

import java.util.List;

public abstract class CustomVarEvaluator implements VarEvaluator {

    private final VarEvaluator inner;

    public CustomVarEvaluator(VarEvaluator inner) {
        this.inner = inner;
    }

    @Override
    public boolean exists(String varName) {
        return inner.exists(varName);
    }

    @Override
    public List<DValue> lookupVar(String varName) {
        List<DValue> list = inner.lookupVar(varName);
        list = onLookup(varName, list);
        return list;
    }

    @Override
    public ResultValue lookupVarAsResultValue(String varName) {
        ResultValue res = inner.lookupVarAsResultValue(varName);
        res = onLookupAsResultValue(varName, res);
        return null;
    }

    //derived class can override this. it can use list or ignore it and return its own list, or return null
    protected abstract List<DValue> onLookup(String varName, List<DValue> list);

    //derived class can override this. it can use list or ignore it and return its own list, or return null
    protected abstract ResultValue onLookupAsResultValue(String varName, ResultValue res);

}
