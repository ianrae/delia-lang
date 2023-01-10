package org.delia.dbimpl.mem.impl.func.functions;

import org.delia.core.FactoryService;
import org.delia.lld.LLD;
import org.delia.type.DValue;
import org.delia.dbimpl.mem.impl.func.MemFunctionContext;

import java.util.List;

public class LimitFunction extends FunctionBase {
    private int pageSize;

    public LimitFunction(FactoryService factorySvc, LLD.LLDFuncEx funcEl) {
        super(factorySvc, funcEl);
        this.pageSize = getArgInt(0);
    }

    @Override
    public List<DValue> execute(List<DValue> dvalList, MemFunctionContext ctx) {
        List<DValue> newList;

        int n = dvalList.size();
        if (n < pageSize) {
            pageSize = n;
        }
        newList = dvalList.subList(0, pageSize);

        return newList;
    }
}
