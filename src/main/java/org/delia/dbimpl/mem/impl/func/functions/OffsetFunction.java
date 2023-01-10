package org.delia.dbimpl.mem.impl.func.functions;

import org.delia.core.FactoryService;
import org.delia.lld.LLD;
import org.delia.type.DValue;
import org.delia.util.DeliaExceptionHelper;
import org.delia.dbimpl.mem.impl.func.MemFunctionContext;

import java.util.Collections;
import java.util.List;

public class OffsetFunction extends FunctionBase {
    private int offsetIndex;

    public OffsetFunction(FactoryService factorySvc, LLD.LLDFuncEx funcEl) {
        super(factorySvc, funcEl);
        this.offsetIndex = getArgInt(0);
    }

    @Override
    public List<DValue> execute(List<DValue> dvalList, MemFunctionContext ctx) {
        List<DValue> newList;
        if (offsetIndex == 0 && dvalList.isEmpty()) {
            return Collections.emptyList();
        }

        if (offsetIndex < 0 || offsetIndex >= dvalList.size()) {
            DeliaExceptionHelper.throwError("queryfn-bad-index", "bad index!! %d in fn '%s'", offsetIndex, funcEx.fnName);
        }

        int n = dvalList.size();
        newList = dvalList.subList(offsetIndex, n);

        return newList;
    }
}
