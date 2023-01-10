package org.delia.dbimpl.mem.impl.func;

import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.lld.LLD;
import org.delia.runner.DeliaRunner;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.util.DeliaExceptionHelper;
import org.delia.dbimpl.mem.impl.FKResolver;
import org.delia.dbimpl.mem.impl.func.functions.*;

import java.util.List;

import static java.util.Objects.isNull;

public class FuncExecutor extends ServiceBase implements  FunctionExecutorInternal {

    private final DTypeRegistry registry;
    private final FKResolver fkResolver;
    private final DeliaRunner deliaRunner;

    public FuncExecutor(FactoryService factorySvc, DTypeRegistry registry, FKResolver fkResolver, DeliaRunner deliaRunner) {
        super(factorySvc);
        this.registry = registry;
        this.fkResolver = fkResolver;
        this.deliaRunner = deliaRunner;
    }

    public List<DValue> execFunction(LLD.LLDFuncEx funcEl, List<DValue> dvalList, QScope scope) {
        MemFunction func = createFunc(funcEl);
        MemFunctionContext ctx = new MemFunctionContext();
        ctx.registry = registry;
        ctx.fkResolver = fkResolver;
        ctx.scope = scope;
        ctx.internalFuncExecutor = this;
        return func.execute(dvalList, ctx);
    }

    private MemFunction createFunc(LLD.LLDFuncEx funcEl) {
        MemFunction func = null;

        switch(funcEl.fnName) {
            case "count":
                func = new CountFunction(factorySvc, funcEl);
                break;
            case "distinct":
                func = new DistinctFunction(factorySvc, funcEl);
                break;
            case "exists":
                func = new ExistsFunction(factorySvc, funcEl);
                break;
            case "fks":
                func = new FksFunction(factorySvc, funcEl);
                break;
            case "fetch":
                func = new FetchFunction(factorySvc, funcEl, deliaRunner, registry);
                break;
            case "remove-fks":
                func = new RemoveFksFunction(factorySvc, funcEl);
                break;
            case "first":
            case "ith":
            case "last":
                func = new IthFunction(factorySvc, funcEl);
                break;
            case "limit":
                func = new LimitFunction(factorySvc, funcEl);
                break;
            case "min":
                func = new MinFunction(factorySvc, funcEl);
                break;
            case "max":
                func = new MaxFunction(factorySvc, funcEl);
                break;
            case "offset":
                func = new OffsetFunction(factorySvc, funcEl);
                break;
            case "orderBy":
                func = new OrderByFunction(factorySvc, funcEl);
                break;
            default:
                DeliaExceptionHelper.throwNotImplementedError("unknown mem fn: %s", funcEl.fnName);
                break;
        }
        return func;
    }

    //used when one func needs to call another
    @Override
    public List<DValue> execFunctionInternal(String fnName, List<DValue> dvalList, MemFunctionContext ctx) {
        LLD.LLDFuncEx targetFn = null;
        for(LLD.LLEx element: ctx.scope.funcL) {
            LLD.LLDFuncEx fn = (LLD.LLDFuncEx) element;
            if (fn.fnName.equals(fnName)) {
                targetFn = fn;
            }
        }

        if (isNull(targetFn)) {
            DeliaExceptionHelper.throwError("unknown-func", "Unknown filter fn '%s' - it needs to be included in the query statement", fnName);
        }

        MemFunction func = createFunc(targetFn);
       return func.execute(dvalList, ctx);
    }
}
