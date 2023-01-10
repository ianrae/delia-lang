package org.delia.dbimpl.mem.impl.func;

import org.delia.type.DValue;

import java.util.List;

public interface MemFunction {
    List<DValue> execute(List<DValue> dvalList, MemFunctionContext ctx);
}
