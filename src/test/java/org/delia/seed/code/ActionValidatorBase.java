package org.delia.seed.code;

import org.delia.seed.DeliaSeedTests;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.type.TypePair;
import org.delia.util.DValueHelper;

public abstract class ActionValidatorBase implements ActionValidator {
    protected DeliaSeedTests.DBInterface dbInterface;
    protected DTypeRegistry registry;

    @Override
    public void init(DeliaSeedTests.DBInterface dbInterface, DTypeRegistry registry) {
        this.dbInterface = dbInterface;
        this.registry = registry;
    }

    @Override
    public abstract void validateAction(DeliaSeedTests.SdAction action, DeliaSeedTests.SdValidationResults res);


    protected void validateTableExists(DeliaSeedTests.SdAction action, DeliaSeedTests.SdValidationResults res) {
        if (!dbInterface.tableExists(action.getTable())) {
            res.errors.add(new SbError("unknown.table", String.format("unknown table: '%s'", action.getTable())));
        }
    }

    protected void validateKeyOrPK(DeliaSeedTests.SdAction action, DeliaSeedTests.SdValidationResults res) {
        DStructType structType = (DStructType) registry.getType(action.getTable());
        if (action.getKey() != null) {
            if (!dbInterface.columnExists(action.getTable(), action.getKey())) {
                res.errors.add(new SbError("key.unknown.column", String.format("key references unknown column '%s' in table: '%s'", action.getKey(), action.getTable())));
            }
        } else if (structType.getPrimaryKey() == null) {
            res.errors.add(new SbError("key.missing", String.format("table: '%s' has no primary key. Action.key must not be empty", action.getTable())));
        } else {
            TypePair pkpair = DValueHelper.findPrimaryKeyFieldPair(structType);
            int missingCount = 0;
            for(DValue dval: action.getData()) {
                if (! dval.asStruct().hasField(pkpair.name)) {
                    missingCount++;
                }
            }
            if (missingCount > 0) {
                res.errors.add(new SbError("pk.missing", String.format("table: '%s'. Data rows must have a value for the primary key. %d rows are missing one", action.getTable(), missingCount)));
            }
        }
    }
}
