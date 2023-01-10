package org.delia.dval;

import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.type.*;
import org.delia.valuebuilder.StructValueBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * In our MEM database we don't want to modify the dvalues in the memory tables.
 * This service will clone dvals so that we can add FKs.
 */
public class DValueCopyService extends ServiceBase  {
    private final DTypeRegistry registry;

    public DValueCopyService(FactoryService factorySvc, DTypeRegistry registry) {
        super(factorySvc);
        this.registry = registry;
    }

    public DValue clone(DValue dval) {
        if (dval == null) return null;
        if (dval.getType().isScalarShape()) {
            return dval; //are idempotent so is safe to re-use object
        }

        DStructType structType = dval.asStruct().getType();
        StructValueBuilder builder = new StructValueBuilder(structType);
        for(String fieldName: dval.asMap().keySet()) {
            DValue inner = dval.asMap().get(fieldName);
            if (inner != null && inner.getType().isRelationShape()) {
                DRelation drel = inner.asRelation();

                DValue newDvalRelation = DRelationHelper.createEmptyRelation(structType, fieldName, registry);
                newDvalRelation.asRelation().getMultipleKeys().addAll(drel.getMultipleKeys());
                builder.addField(fieldName, newDvalRelation);
                //TODO: later need to clone fetched dvals too. with code to avoid infinite recursion
            } else {
                DValue innerClone = clone(inner); //***recursion***
                builder.addField(fieldName, innerClone);
            }
        }
        boolean b = builder.finish();
        if (! b) {
            log.logError("clone failed!!");
        }

        DValue clone = builder.getDValue();
        DValueInternal dvi = (DValueInternal) clone;
        dvi.setPersistenceId(dval.getPersistenceId());
        dvi.setValidationState(dval.getValidationState());

        return clone;
    }
    public List<DValue> clone(List<DValue> dvalList) {
        final List<DValue> list = new ArrayList<>();
        dvalList.forEach(dval -> list.add(clone(dval)));
        return list;
    }
}
