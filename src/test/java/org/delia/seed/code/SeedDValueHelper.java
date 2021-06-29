package org.delia.seed.code;

import org.delia.type.DRelation;
import org.delia.type.DType;
import org.delia.type.DValue;
import org.delia.util.DValueHelper;

import java.util.StringJoiner;

public class SeedDValueHelper {

    public static String getFieldAsDelia(DValue dvalParent, String fieldName) {
        DValue dval = dvalParent.asStruct().getField(fieldName);
        if (dval == null) {
            return "null";
        }
        DType fieldType = DValueHelper.findFieldType(dvalParent.getType(), fieldName);

        String str = null;
        switch (fieldType.getShape()) {
            case RELATION: {
                DRelation rel = dval.asRelation();
                if (rel.isMultipleKey()) {
                    return buildMultipleRef(rel);
                }
                str = rel.getForeignKey().asString();
            }
            break;
            case STRING:
            case DATE: {
                String s = dval.asString();
                str = String.format("'%s'", s);
            }
            break;
            default:
                str = dval.asString();
                break;
        }
        return str.trim();
    }

    public static String renderAsDelia(DValue dval) {
        if (dval == null) {
            return "null";
        }
        DType fieldType = dval.getType();

        String str = null;
        switch (fieldType.getShape()) {
            case RELATION: {
                DRelation rel = dval.asRelation();
                if (rel.isMultipleKey()) {
                    return buildMultipleRef(rel);
                }
                str = rel.getForeignKey().asString();
            }
            break;
            case STRING:
            case DATE: {
                String s = dval.asString();
                str = String.format("'%s'", s);
            }
            break;
            default:
                str = dval.asString();
                break;
        }
        return str.trim();
    }

    private static String buildMultipleRef(DRelation rel) {
        StringJoiner joiner = new StringJoiner(",");
        for (DValue key : rel.getMultipleKeys()) {
            joiner.add(key.asString());
        }
        return String.format("FIX!{[%s]}", joiner.toString()); //TODO fix!!
    }
}
