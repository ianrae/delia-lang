package org.delia.migration.action;

import org.delia.type.DStructType;

import java.util.List;

public class AlterUniqueFieldsConstraint extends AddFieldAction {
    List<String> fields;
    List<String> newFields; //use to create/delete/alter

    public AlterUniqueFieldsConstraint(DStructType structType) {
        super(structType);
    }
}
