package org.delia.migrationparser;

import org.apache.commons.lang3.StringUtils;
import org.delia.type.DStructType;
import org.delia.type.DTypeName;

/**
 * Represents a type + fieldName being migrated
 */
public class MigrationField {
    public DStructType structType;
    public String fieldName;

    public MigrationField(DStructType structType, String fieldName) {
        this.structType = structType;
        this.fieldName = fieldName;
    }

    public String makeKey() {
        String s = structType.getTypeName().toString();
        return String.format("%s:%s", s, fieldName);
    }

    public static DTypeName getTypeNameFromKey(String key) {
        String s = StringUtils.substringBefore(key, ":");
        if (s.contains(".")) {
            String[] ar = s.split("\\.");
            return new DTypeName(ar[0], ar[1]);
        } else {
            return new DTypeName(null, s);
        }
    }

    public static String getFieldNameFromKey(String key) {
        String s = StringUtils.substringAfter(key, ":");
        return s;
    }

    public boolean isSameType(DStructType structType) {
        String key = makeKey();
        DTypeName dTypeName = MigrationField.getTypeNameFromKey(key);
        return dTypeName.equals(structType.getTypeName());
    }
}
