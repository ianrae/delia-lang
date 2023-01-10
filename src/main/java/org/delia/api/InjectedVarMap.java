package org.delia.api;

import org.delia.runner.QueryResponse;
import org.delia.runner.ResultValue;
import org.delia.type.DValue;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InjectedVarMap {
    public Map<String, ResultValue> varMap = new ConcurrentHashMap<>();

    public void addVar(String varName, DValue dval) {
        ResultValue res = new ResultValue();
        res.ok = true;
        res.val = dval;
        res.shape = dval == null ? null : dval.getType().getShape();
        varMap.put(varName, res);
    }
    public void addVarToQueryResp(String varName, DValue dval) {
        QueryResponse qresp = new QueryResponse();
        qresp.ok = true;
        qresp.dvalList = Collections.singletonList(dval);
        ResultValue res = new ResultValue();
        res.ok = true;
        res.val = qresp;
        varMap.put(varName, res);
    }

    public Map<String, ResultValue> getMap() {
        return varMap;
    }

}
