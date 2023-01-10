package org.delia.migration.action;

import org.delia.type.DStructType;

public class RenameTableAction extends MigrationActionBase {
    public String newName;
    public boolean isAssocTbl;

    public RenameTableAction(DStructType structType) {
        super(structType);
    }

    @Override
    public String toString() {
        String str = structType.getTypeName().toString();
        return String.format("rTBL(%s):%s", str, newName);
    }
}
