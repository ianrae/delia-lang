package org.delia.dbimpl.mem.impl.func.functions;

import org.apache.commons.collections.CollectionUtils;
import org.delia.core.FactoryService;
import org.delia.lld.LLD;
import org.delia.runner.QueryResponse;
import org.delia.type.DValue;
import org.delia.type.Shape;
import org.delia.util.DeliaExceptionHelper;
import org.delia.dbimpl.mem.impl.func.MemFunctionContext;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class MaxFunction extends FunctionBase {

    public MaxFunction(FactoryService factorySvc, LLD.LLDFuncEx funcEl) {
        super(factorySvc, funcEl);
    }

    @Override
    public List<DValue> execute(List<DValue> dvalList, MemFunctionContext ctx) {
        List<DValue> newList = new ArrayList<>();
        if (CollectionUtils.isEmpty(dvalList)) {
//            dvalList = null;
            return dvalList; //min of empty set is null
        }

        Shape shape = detectShape(dvalList);
        if (shape == null) {
            return dvalList;
        }

        QueryResponse qresp = null; //TODO: we might need this later
        DValue dvalResult = null;
        switch (shape) {
            case INTEGER:
                dvalResult = processInt(qresp, dvalList, ctx);
                break;
//            case LONG:
//                dvalResult = processLong(qresp, dvalList, ctx);
//                break;
            case NUMBER:
                dvalResult = processNumber(qresp, dvalList, ctx);
                break;
            case BOOLEAN:
                dvalResult = processBoolean(qresp, dvalList, ctx);
                break;
            case STRING:
                dvalResult = processString(qresp, dvalList, ctx);
                break;
            case DATE:
                dvalResult = processDate(qresp, dvalList, ctx);
                break;
            default:
                DeliaExceptionHelper.throwError("unsupported-min-type", "min() doesn't support type '%s'", shape);
                break;
        }

        newList.add(dvalResult);
        return newList;
    }

    private DValue processInt(QueryResponse qresp, List<DValue> dvalList, MemFunctionContext ctx) {
        int max = Integer.MIN_VALUE;
        for (DValue dval : dvalList) {
            if (dval == null) {
                continue;
            }
            int k = dval.asInt();
            if (k > max) {
                max = k;
            }
        }

        DValue dval = buildIntVal(max, ctx);
        return dval;
    }

//    private DValue processLong(QueryResponse qresp, List<DValue> dvalList, MemFunctionContext ctx) {
//        long max = Long.MIN_VALUE;
//        for (DValue dval : dvalList) {
//            if (dval == null) {
//                continue;
//            }
//            long k = dval.asLong();
//            if (k > max) {
//                max = k;
//            }
//        }
//
//        DValue dval = buildLongVal(max, ctx);
//        return dval;
//    }

    private DValue processNumber(QueryResponse qresp, List<DValue> dvalList, MemFunctionContext ctx) {
        double max = Double.MIN_VALUE;
        for (DValue dval : dvalList) {
            if (dval == null) {
                continue;
            }
            double k = dval.asNumber();
            if (k > max) {
                max = k;
            }
        }

        DValue dval = buildNumberVal(max, ctx);
        return dval;
    }

    private DValue processBoolean(QueryResponse qresp, List<DValue> dvalList, MemFunctionContext ctx) {
        Boolean max = false;
        for (DValue dval : dvalList) {
            if (dval == null) {
                continue;
            }
            Boolean k = dval.asBoolean();
            if (k.compareTo(max) > 0) {
                max = k;
            }
        }

        DValue dval = buildBoolVal(max, ctx);
        return dval;
    }

    private DValue processString(QueryResponse qresp, List<DValue> dvalList, MemFunctionContext ctx) {
        String max = null; //min possible string
        for (DValue dval : dvalList) {
            if (dval == null) {
                continue;
            }
            String k = dval.asString();

            if (max == null) {
                max = k;
            } else if (k.compareTo(max) > 0) {
                max = k;
            }
        }

        DValue dval = buildStringVal(max, ctx);
        return dval;
    }

    private DValue processDate(QueryResponse qresp, List<DValue> dvalList, MemFunctionContext ctx) {
        Instant min = Instant.MIN;
        ZonedDateTime maxZdt = null;

        for (DValue dval : dvalList) {
            if (dval == null) {
                continue;
            }
            ZonedDateTime zdt = dval.asDate();

            if (min == null) {
                min = zdt.toInstant();
                maxZdt = zdt;
            } else if (zdt.toInstant().compareTo(min) > 0) {
                min = zdt.toInstant();
                maxZdt = zdt;
            }
        }

        DValue dval = buildDateVal(maxZdt, ctx);
        return dval;
    }

}
