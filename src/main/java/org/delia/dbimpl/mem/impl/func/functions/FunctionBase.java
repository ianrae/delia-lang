package org.delia.dbimpl.mem.impl.func.functions;

import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.lld.LLD;
import org.delia.type.DType;
import org.delia.type.DValue;
import org.delia.type.Shape;
import org.delia.valuebuilder.ScalarValueBuilder;
import org.delia.dbimpl.mem.impl.func.MemFunction;
import org.delia.dbimpl.mem.impl.func.MemFunctionContext;

import java.time.ZonedDateTime;
import java.util.List;

public abstract class FunctionBase extends ServiceBase implements MemFunction {

    protected final LLD.LLDFuncEx funcEx;

    public FunctionBase(FactoryService factorySvc, LLD.LLDFuncEx funcEx) {
        super(factorySvc);
        this.funcEx = funcEx;
    }

    protected int getArgInt(int index) {
        LLD.LLFuncArg arg = (LLD.LLFuncArg) funcEx.argsL.get(index);
        return arg.dval.asInt();
    }

    protected String getArgStr(int index) {
        LLD.LLEx el = funcEx.argsL.get(index);
        if (el instanceof LLD.LLFinalFieldEx) {
            LLD.LLFinalFieldEx arg = (LLD.LLFinalFieldEx) el;
            return arg.fieldName;
        } else {
            LLD.LLFuncArg arg = (LLD.LLFuncArg) el;
            return arg.funcArg;
        }
    }

    protected boolean existsArg(int index) {
        return index < funcEx.argsL.size();
    }

    protected DValue buildIntVal(int max, MemFunctionContext ctx) {
        ScalarValueBuilder builder = new ScalarValueBuilder(null, ctx.registry);
        DValue dval = builder.buildInt(max);
        return dval;
    }
//    protected DValue buildLongVal(long max, MemFunctionContext ctx) {
//        ScalarValueBuilder builder = new ScalarValueBuilder(null, ctx.registry);
//        DValue dval = builder.buildLong(max);
//        return dval;
//    }
    protected DValue buildNumberVal(double max, MemFunctionContext ctx) {
        ScalarValueBuilder builder = new ScalarValueBuilder(null, ctx.registry);
        DValue dval = builder.buildNumber(max);
        return dval;
    }
    protected DValue buildBoolVal(boolean b, MemFunctionContext ctx) {
        ScalarValueBuilder builder = new ScalarValueBuilder(null, ctx.registry);
        DValue dval = builder.buildBoolean(b);
        return dval;
    }
    protected DValue buildStringVal(String s, MemFunctionContext ctx) {
        ScalarValueBuilder builder = new ScalarValueBuilder(null, ctx.registry);
        DValue dval = builder.buildString(s);
        return dval;
    }
    protected DValue buildDateVal(ZonedDateTime zdt, MemFunctionContext ctx) {
        ScalarValueBuilder builder = new ScalarValueBuilder(factorySvc, ctx.registry);
        DValue dval = builder.buildDate(zdt);
        return dval;
    }

    /**
     * may be all null
     *
     * @param dvalList values
     * @return shape or null
     */
    protected Shape detectShape(List<DValue> dvalList) {
        for (DValue dval : dvalList) {
            if (dval != null) {
                return dval.getType().getShape();
            }
        }
        return null;
    }

    /**
     * may be all null
     *
     * @param dvalList values
     * @return type or null
     */
    protected DType detectType(List<DValue> dvalList) {
        for (DValue dval : dvalList) {
            if (dval != null) {
                return dval.getType();
            }
        }
        return null;
    }

}
