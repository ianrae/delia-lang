package org.delia.dbimpl.mem.impl.func;

import org.delia.type.DTypeRegistry;
import org.delia.dbimpl.mem.impl.FKResolver;

public class MemFunctionContext {
    public DTypeRegistry registry;
    public FKResolver fkResolver;
    public QScope scope;
    public FunctionExecutorInternal internalFuncExecutor;
}
