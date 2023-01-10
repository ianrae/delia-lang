package org.delia.dbimpl.mem.impl.func.functions;

import org.delia.core.FactoryService;
import org.delia.lld.LLD;
import org.delia.type.DValue;
import org.delia.dbimpl.mem.impl.func.MemFunctionContext;

import java.util.List;

//MEM dvals already contain fks. We use RemoveFksFunction to remove them if necessary
public class FksFunction extends FunctionBase {

    public FksFunction(FactoryService factorySvc, LLD.LLDFuncEx funcEl) {
        super(factorySvc, funcEl);
    }

    @Override
    public List<DValue> execute(List<DValue> dvalList, MemFunctionContext ctx) {
        return dvalList;
    }

}
