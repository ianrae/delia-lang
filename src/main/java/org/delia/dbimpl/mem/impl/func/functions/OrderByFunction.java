package org.delia.dbimpl.mem.impl.func.functions;

import org.delia.core.FactoryService;
import org.delia.lld.LLD;
import org.delia.type.*;
import org.delia.dbimpl.mem.impl.func.MemFunctionContext;
import org.delia.util.DValueHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

public class OrderByFunction extends FunctionBase {
    private String orderByField; //TODO support more than one later!
    private boolean isAsc;

    public OrderByFunction(FactoryService factorySvc, LLD.LLDFuncEx funcEl) {
        super(factorySvc, funcEl);
        this.orderByField = getArgStr(0);
        isAsc = true;
        if (existsArg(1)) {
            String tmp = getArgStr(1);
            if (tmp.equals("desc")) {
                isAsc = false;
            }
        }
    }


    @Override
    public List<DValue> execute(List<DValue> dvalList, MemFunctionContext ctx) {
        if (dvalList == null || dvalList.size() <= 1) {
            return dvalList; //nothing to sort
        }

        TreeMap<Object,List<DValue>> map = new TreeMap<>();
        List<DValue> nulllist = new ArrayList<>();

        //ensureFieldExists(dvalList, "orderBy", fieldName);
        for(DValue dval: dvalList) {
            DValue inner = dval.asStruct().getField(orderByField);

            if (inner == null) {
                nulllist.add(dval);
            } else {
                Object key = calcKey(inner, dval, dvalList, ctx);
                List<DValue> valuelist = map.get(key);
                if (valuelist == null) {
                    valuelist = new ArrayList<>();
                }
                valuelist.add(dval);
                map.put(key, valuelist);
            }
        }

        List<DValue> newlist = new ArrayList<>();
        for(Object key: map.keySet()) {
            List<DValue> valuelist = map.get(key);
            newlist.addAll(valuelist);
        }

        //add null values
        if (isAsc) {
            nulllist.addAll(newlist);
            newlist = nulllist;
        } else {
            newlist.addAll(nulllist);
        }

        if (! isAsc) {
            Collections.reverse(newlist);
        }

        return newlist;
    }

    private Object calcKey(DValue inner, DValue dval, List<DValue> dvalList, MemFunctionContext ctx) {
        if (inner.getType().isRelationShape()) {
            ctx.internalFuncExecutor.execFunctionInternal("fetch", dvalList, ctx);
            //now drel.fetchL has been filled
            DRelation drel = inner.asRelation();
            //currently we only support 1:1 and M:1. TODO later support M:M
            //see t0-queryfn-orderby-relation.txt
            //we only support orderBy on pk field of relation, and you must include .fetch() of the org field!
            //TODO support orderBy('cust.x')
            if (drel.haveFetched() && drel.getFetchedItems().size() == 1) {
                DValue foreignVal = drel.getFetchedItems().get(0);
                DStructType structType = (DStructType) foreignVal.getType();
                TypePair pair = DValueHelper.findPrimaryKeyFieldPair(structType);
                if (pair != null) {
                    DValue pkval = DValueHelper.getFieldValue(foreignVal, pair.name);
                    return pkval.getObject();
                } else {
                    return null;
                }
            }
            return "abc";
        } else {
            return inner.getObject();
        }
    }
}
