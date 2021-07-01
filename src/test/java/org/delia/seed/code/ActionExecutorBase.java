package org.delia.seed.code;

import org.delia.DeliaSession;
import org.delia.db.sql.StrCreator;
import org.delia.seed.DeliaSeedTests;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.type.TypePair;
import org.delia.util.DValueHelper;

import java.util.StringJoiner;

public abstract class ActionExecutorBase implements ActionExecutor {
    protected DeliaSeedTests.DBInterface dbInterface;
    protected DTypeRegistry registry;
    protected DeliaSession sess;

    @Override
    public void init(DeliaSeedTests.DBInterface dbInterface, DTypeRegistry registry, DeliaSession sess) {
        this.dbInterface = dbInterface;
        this.registry = registry;
        this.sess = sess;
    }

    @Override
    public abstract void executeAction(DeliaSeedTests.SdAction action, StrCreator sc, DeliaSeedTests.SdExecutionResults res);

    protected String buildDataValues(DValue dval) {
        return buildDataValues(dval, true);
    }
    protected String buildDataValues(DValue dval, boolean includePkField) {
        StringJoiner joiner = new StringJoiner(", ");
        DStructType structType = dval.asStruct().getType(); //want in declared order
        TypePair pkpair = DValueHelper.findPrimaryKeyFieldPair(structType);

        for (TypePair pair : structType.getAllFields()) {
            if (! includePkField && (pair.name.equals(pkpair.name))) {
                continue;
            }
            String fieldName = pair.name;
            DValue inner = dval.asStruct().getField(fieldName);
            String s = String.format("%s: %s", fieldName, SeedDValueHelper.renderAsDelia(inner));
            joiner.add(s);
        }
        return joiner.toString();
    }

    protected String getKey(DValue dval, DeliaSeedTests.SdAction action) {
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

    protected String getFieldAsDelia(DValue dvalParent, String fieldName) {
        return SeedDValueHelper.getFieldAsDelia(dvalParent, fieldName);
    }


}
