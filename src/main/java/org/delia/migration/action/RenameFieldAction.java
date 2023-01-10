package org.delia.migration.action;

import org.delia.type.DStructType;

public class RenameFieldAction extends MigrationActionBase {
    public String fieldName;
    public String newName;

    public RenameFieldAction(DStructType structType) {
        super(structType);
    }

    @Override
    public String toString() {
        String str = structType.getTypeName().toString();
        return String.format("rFLD(%s.%s:%s)", str, fieldName, newName);
    }
}
