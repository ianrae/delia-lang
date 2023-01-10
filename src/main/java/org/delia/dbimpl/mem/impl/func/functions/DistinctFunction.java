package org.delia.dbimpl.mem.impl.func.functions;

import org.delia.core.FactoryService;
import org.delia.lld.LLD;
import org.delia.type.*;
import org.delia.util.DeliaExceptionHelper;
import org.delia.valuebuilder.RelationValueBuilder;
import org.delia.dbimpl.mem.impl.func.MemFunctionContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DistinctFunction extends FunctionBase {

    public DistinctFunction(FactoryService factorySvc, LLD.LLDFuncEx funcEl) {
        super(factorySvc, funcEl);
    }

    @Override
    public List<DValue> execute(List<DValue> dvalList, MemFunctionContext ctx) {
        if (dvalList == null || dvalList.size() <= 1) {
            return dvalList; //nothing to sort
        }

        List<DValue> newList = new ArrayList<>();
        DValue firstVal = dvalList.get(0);
        if (firstVal.getType().isShape(Shape.RELATION)) {
            //build list of distinct fk values, using map
            List<DValue> distinctFKList = new ArrayList<>();
            Map<String,DValue> map = new HashMap<>();
            DTypeName typeName = null;
            for(DValue dval: dvalList) {
                DRelation drel = dval.asRelation();
                typeName = drel.getTypeName();
                for(DValue fk: drel.getMultipleKeys()) {
                    String strval = fk.asString(); //use string for now
                    if (! map.containsKey(strval)) {
                        map.put(strval, fk);
                        distinctFKList.add(fk);
                    }
                }
            }

            DValue finalVal = createRelation(typeName, distinctFKList, ctx);
            newList.add(finalVal);
        } else {
            //build list of distinct values, using map
            Map<String,DValue> map = new HashMap<>();
            for(DValue dval: dvalList) {
                String strval = dval.asString(); //use string for now
                if (! map.containsKey(strval)) {
                    map.put(strval, dval);
                    newList.add(dval);
                }
            }
        }

        return newList;
    }

    private DValue createRelation(DTypeName typeName, List<DValue> fks, MemFunctionContext ctx) {
        DType relType = ctx.registry.getType(BuiltInTypes.RELATION_SHAPE);
        RelationValueBuilder builder = new RelationValueBuilder(relType, typeName, ctx.registry);
        builder.buildFromList(fks);
        boolean b = builder.finish();
        if (!b) {
            DeliaExceptionHelper.throwError("relation-create-failed-assocCrud", "Type '%s': Failed to create empty relation", typeName);
            return null;
        } else {
            DValue dval = builder.getDValue();
            return dval;
        }
    }
}
