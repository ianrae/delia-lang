package org.delia.migration.action;

import org.delia.type.DStructType;

public class RemoveFieldAction extends MigrationActionBase {
    public String fieldName;
    public boolean isPhysicalField;

    public RemoveFieldAction(DStructType structType) {
        super(structType);
    }

    @Override
    public String toString() {
        String str = structType.getTypeName().toString();
        return String.format("-FLD(%s.%s)", str, fieldName);
    }
}
