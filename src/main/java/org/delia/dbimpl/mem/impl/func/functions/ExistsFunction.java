package org.delia.dbimpl.mem.impl.func.functions;

import org.delia.core.FactoryService;
import org.delia.lld.LLD;
import org.delia.type.DValue;
import org.delia.dbimpl.mem.impl.func.MemFunctionContext;

import java.util.ArrayList;
import java.util.List;

public class ExistsFunction extends FunctionBase {

    public ExistsFunction(FactoryService factorySvc, LLD.LLDFuncEx funcEl) {
        super(factorySvc, funcEl);
    }

    @Override
    public List<DValue> execute(List<DValue> dvalList, MemFunctionContext ctx) {
        List<DValue> newList = new ArrayList<>();
        boolean b = !dvalList.isEmpty();
        DValue dval = buildBoolVal(b, ctx);

        newList.add(dval);
        return newList;
    }
}
