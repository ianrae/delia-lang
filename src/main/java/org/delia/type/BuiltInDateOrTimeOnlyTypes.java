package org.delia.type;

/**
 * There are two flavours of date
 *
 * @author Ian Rae
 */
public enum BuiltInDateOrTimeOnlyTypes {
    DATE_DATE_ONLY,
    DATE_TIME_ONLY;

    public static boolean isIsDateOrTimeOnlyType(String typeName) {
        switch (typeName) {
            case "DATE_DATE_ONLY":
            case "DATE_TIME_ONLY":
                return true;
            default:
                return false;
        }
    }
}

