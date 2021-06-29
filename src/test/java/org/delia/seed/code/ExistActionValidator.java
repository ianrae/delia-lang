package org.delia.seed.code;

import org.delia.seed.DeliaSeedTests;
import org.delia.type.*;
import org.delia.util.DValueHelper;

public class ExistActionValidator implements ActionValidator {

    private DeliaSeedTests.DBInterface dbInterface;
    private DTypeRegistry registry;

    @Override
    public void init(DeliaSeedTests.DBInterface dbInterface, DTypeRegistry registry) {
        this.dbInterface = dbInterface;
        this.registry = registry;
    }

    @Override
    public void validateAction(DeliaSeedTests.SdAction action, DeliaSeedTests.SdValidationResults res) {
        if (!dbInterface.tableExists(action.getTable())) {
            res.errors.add(new SbError("unknown.table", String.format("unknown table: '%s'", action.getTable())));
        }

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

//            DStructType structType = dbInterface.getTypeSchema(action.getTable());

        for (DValue dval : action.getData()) {
            validateDValue(dval, structType, res);
        }

    }
    private void validateDValue(DValue dval, DStructType structType, DeliaSeedTests.SdValidationResults res) {
        //dval will always be the correct dtype typeName
        //idea here is that dval's stype is simply a structural type built from the data provided
        //We compare against the actual db schema structType
        DStructHelper helper = dval.asStruct();
        for (TypePair pair : helper.getType().getAllFields()) {
            String fieldName = pair.name;
            DValue fieldVal = helper.getField(fieldName);
            if (fieldVal == null) {
                if (!structType.fieldIsOptional(fieldName)) {
                    res.errors.add(new SbError("data.missing.value", String.format("data column '%s': null not allowed", fieldName)));
                }
            } else {
                DType dataType = pair.type;
                DType typeInDB = DValueHelper.findFieldType(structType, fieldName);
                if (!areCompatible(dataType, typeInDB)) {
                    res.errors.add(new SbError("data.wrong.type", String.format("data column '%s': wrong type in value: '%s'", fieldName, fieldVal.asString())));
                }

                //for fk values we will let the db validate those
            }
        }
    }

    private boolean areCompatible(DType dataType, DType typeInDB) {
        //TODO need complete logic here
        return dataType.getShape().equals(typeInDB.getShape()); //simple for now
    }

}
