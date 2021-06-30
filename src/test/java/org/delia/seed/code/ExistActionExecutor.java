package org.delia.seed.code;

import org.delia.db.sql.StrCreator;
import org.delia.seed.DeliaSeedTests;
import org.delia.type.DStructType;
import org.delia.type.DValue;
import org.delia.type.TypePair;

import java.util.StringJoiner;

public class ExistActionExecutor extends ActionExecutorBase {
    @Override
    public void executeAction(DeliaSeedTests.SdAction action, StrCreator sc, DeliaSeedTests.SdExecutionResults res) {
        for (DValue dval : action.getData()) {
            sc.o("upsert %s[%s] ", action.getTable(), getKey(dval, action));
            sc.o("{ %s } ", buildDataValues(dval));
            sc.nl();
        }
    }

    private String buildDataValues(DValue dval) {
        StringJoiner joiner = new StringJoiner(", ");
        DStructType structType = dval.asStruct().getType(); //want in declared order
        for (TypePair pair : structType.getAllFields()) {
            String fieldName = pair.name;
            DValue inner = dval.asStruct().getField(fieldName);
            String s = String.format("%s: %s", fieldName, SeedDValueHelper.renderAsDelia(inner));
            joiner.add(s);
        }
        return joiner.toString();
    }

    private String getKey(DValue dval, DeliaSeedTests.SdAction action) {
        if (action.getKey() != null) {
            String fieldName = action.getKey(); //TODO handle multiple keys later
            String deliaStrExpr = getFieldAsDelia(dval, fieldName);
            return String.format("%s==%s", fieldName, deliaStrExpr);
        }
        //TODO: support schema.table. parcels.address
        DStructType structType = (DStructType) registry.getType(action.getTable());
        String fieldName = structType.getPrimaryKey().getFieldName(); //already validated that its not null
        return getFieldAsDelia(dval, fieldName);
    }

    private String getFieldAsDelia(DValue dvalParent, String fieldName) {
        return SeedDValueHelper.getFieldAsDelia(dvalParent, fieldName);
    }

}
