package org.delia.dbimpl.mem.impl.func;

import org.delia.type.DValue;

import java.util.List;

public interface FunctionExecutorInternal {
    List<DValue> execFunctionInternal(String fnName, List<DValue> dvalList, MemFunctionContext ctx);
}