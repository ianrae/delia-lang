package org.delia.hld;

import org.delia.type.DStructType;
import org.delia.type.DType;

public class AddedFieldInfo {
    HLD.HLDField hldField;
    DStructType structType;
    String joinField;

    public DStructType getFromType() {
        if (hldField == null) {
            return structType;
        }

        DType fieldType = hldField.pair.type;
        if (fieldType.isStructShape()) {
            return (DStructType) fieldType;
        }
        return structType;
    }
}

