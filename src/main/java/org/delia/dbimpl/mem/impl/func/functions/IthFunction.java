package org.delia.dbimpl.mem.impl.func.functions;

import org.delia.core.FactoryService;
import org.delia.lld.LLD;
import org.delia.type.DValue;
import org.delia.dbimpl.mem.impl.func.MemFunctionContext;

import java.util.ArrayList;
import java.util.List;

public class IthFunction extends FunctionBase {
    private boolean ithFlag;
    private int indexToRetrieve;
    private boolean isFirst;

    public IthFunction(FactoryService factorySvc, LLD.LLDFuncEx funcEl) {
        super(factorySvc, funcEl);
        if (funcEl.fnName.equals("ith")) {
            ithFlag = true;
            indexToRetrieve = getArgInt(0);
        } else if (funcEl.fnName.equals("first")) {
            isFirst = true;
        } else if (funcEl.fnName.equals("last")) {
            isFirst = false;
        }
    }

    @Override
    public List<DValue> execute(List<DValue> dvalList, MemFunctionContext ctx) {
        List<DValue> newList = new ArrayList<>();
        int n;
        if (ithFlag) {
            n = indexToRetrieve;
            //sql doesn't do range checking. a SELECT using LIMIT will simply return null if you go 'off the end'
            //So we will return null as well
            if (n < 0 || n >= dvalList.size()) {
                //DeliaExceptionHelper.throwError("queryfn-bad-index", "bad index!! %d in fn '%s'", n, funcEx.fnName);
                return newList; //null
            }
        } else {
            n = isFirst ? 0 : dvalList.size() - 1;
            if (n < 0 || n >= dvalList.size()) {
                return newList; //null
            }
        }

        DValue dval = dvalList.get(n);
        newList.add(dval); //first one

        return newList;
    }
}
