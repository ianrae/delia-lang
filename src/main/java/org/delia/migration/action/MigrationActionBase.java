package org.delia.migration.action;

import org.delia.type.DStructType;

public class MigrationActionBase {
    public DStructType structType; //raw. may not exist in session (eg. when adding new table)

    public MigrationActionBase(DStructType structType) {
        this.structType = structType;
    }
}
