package org.delia.dbimpl.mem.impl.func.functions;

import org.apache.commons.collections.CollectionUtils;
import org.delia.core.FactoryService;
import org.delia.lld.LLD;
import org.delia.type.DValue;
import org.delia.dbimpl.mem.impl.func.MemFunctionContext;

import java.util.ArrayList;
import java.util.List;

public class CountFunction extends FunctionBase {

    public CountFunction(FactoryService factorySvc, LLD.LLDFuncEx funcEl) {
        super(factorySvc, funcEl);
    }

    @Override
    public List<DValue> execute(List<DValue> dvalList, MemFunctionContext ctx) {
        List<DValue> newList = new ArrayList<>();
        if (CollectionUtils.isEmpty(dvalList)) {
            DValue dval = buildIntVal(0, ctx);
            newList.add(dval);
            return newList; //count of empty set is 0
        }

        //don't count null values
        int n = 0;
        for (DValue dval : dvalList) {
            if (dval == null) {
                continue;
            }
            n++;
        }

        DValue dval = buildIntVal(n, ctx);
        newList.add(dval);
        return newList;
    }
}
