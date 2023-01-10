package org.delia.migration.action;

import org.delia.type.DStructType;

public class DeleteTableAction extends MigrationActionBase {
    public boolean isAssocTbl;

    public DeleteTableAction(DStructType structType) {
        super(structType);
    }

    @Override
    public String toString() {
        String str = structType.getTypeName().toString();
        return String.format("-TBL(%s)", str);
    }
}
