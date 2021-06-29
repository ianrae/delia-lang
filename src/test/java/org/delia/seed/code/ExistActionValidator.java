package org.delia.seed.code;

import org.delia.seed.DeliaSeedTests;
import org.delia.type.*;
import org.delia.util.DValueHelper;

public class ExistActionValidator extends ActionValidatorBase {

    @Override
    public void validateAction(DeliaSeedTests.SdAction action, DeliaSeedTests.SdValidationResults res) {
        validateTableExists(action, res);
        validateKeyOrPK(action, res);

        DStructType structType = (DStructType) registry.getType(action.getTable());
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
