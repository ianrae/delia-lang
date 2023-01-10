package org.delia.type;

/**
 * There are four int types for the various sizeof
 *
 * @author Ian Rae
 */
public enum BuiltInSizeofIntTypes {
    INTEGER_8,
    INTEGER_16,
    INTEGER_32,
    INTEGER_64;

    public static boolean isSizeofType(String typeName) {
        switch (typeName) {
            case "INTEGER_8":
            case "INTEGER_16":
            case "INTEGER_32":
            case "INTEGER_64":
                return true;
            default:
                return false;
        }
    }
}

