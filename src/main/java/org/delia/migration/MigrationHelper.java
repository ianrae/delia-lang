package org.delia.migration;

import org.delia.rule.DRule;
import org.delia.rule.rules.RelationManyRule;
import org.delia.rule.rules.RelationOneRule;
import org.delia.rule.rules.SizeofRule;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.Shape;
import org.delia.type.TypePair;
import org.delia.util.DRuleHelper;
import org.delia.util.DValueHelper;
import org.delia.util.StrCreator;

public class MigrationHelper {
    public static String makeFieldFlags(DStructType cur, String fieldName) {
        StrCreator sc = new StrCreator();
        if (cur.fieldIsOptional(fieldName)) {
            sc.addStr("O");
        }
        if (cur.fieldIsUnique(fieldName)) {
            sc.addStr("U");
        }
        if (cur.fieldIsPrimaryKey(fieldName)) {
            sc.addStr("P");
        }
        if (cur.fieldIsSerial(fieldName)) {
            sc.addStr("S");
        }
        return sc.toString();
    }

    public static String mergeFlags(String flags1, String flags2) {
        StrCreator sc = new StrCreator();

        String s = "OUPS";
        for (int i = 0; i < s.length(); i++) {
            char flagChar = s.charAt(i);
            if (flags1.indexOf(flagChar) >= 0 && flags2.indexOf(flagChar) < 0) {
                sc.o("-%s", String.valueOf(flagChar));
            } else if (flags1.indexOf(flagChar) < 0 && flags2.indexOf(flagChar) >= 0) {
                sc.o("+%s", String.valueOf(flagChar));
            }
        }
        return sc.toString();
    }

    public static boolean relationsAreTheSame(DStructType cur, DStructType structType2, String fieldName) {
        String relFlags1 = buildRelationFlags(cur, fieldName);
        String relFlags2 = buildRelationFlags(structType2, fieldName);
        return relFlags1.equals(relFlags2);
    }
    public static String buildRelationFlags(DStructType structType, String fieldName) {
        RelationOneRule oneRule = DRuleHelper.findOneRule(structType, fieldName);
        RelationManyRule manyRule = DRuleHelper.findManyRule(structType, fieldName);
        boolean isOne = oneRule != null;
        boolean isMany = manyRule != null;
        boolean isParent = (oneRule != null && oneRule.isParent());
        String relFlags = String.format("%s%s%s", boolToStr(isOne), boolToStr(isMany), boolToStr(isParent));
        return relFlags;
    }
    public static Object boolToStr(boolean b) {
        return b ? "Y" : "N";
    }

    public static boolean areTypesEqual(DType dtype1, DType dtype2) {
        if (dtype1.getTypeName().toString().equals(dtype2.getTypeName().toString())) {
            return true;
        }
        return false;
    }

    public static boolean areFieldTypesEqual(DStructType cur, DStructType structType2, String fieldName) {
        TypePair pair1 = DValueHelper.findField(cur, fieldName);
        TypePair pair2 = DValueHelper.findField(structType2, fieldName);
        return areTypesEqual(pair1.type, pair2.type);
    }

    public static boolean areFieldSizeofEqual(DStructType cur, DStructType structType2, String fieldName) {
        TypePair pair1 = DValueHelper.findField(cur, fieldName);
        TypePair pair2 = DValueHelper.findField(structType2, fieldName);
        if (pair1.type.isShape(Shape.STRING) && pair2.type.isShape(Shape.STRING)) {
            DRule rule1 = DRuleHelper.findSizeofRule(cur, cur.getRawRules(), fieldName);
            DRule rule2 = DRuleHelper.findSizeofRule(structType2, structType2.getRawRules(), fieldName);
            int sizeof1 = DRuleHelper.getSizeofAmount(rule1, 0);
            int sizeof2 = DRuleHelper.getSizeofAmount(rule2, 0);
            return sizeof1 == sizeof2;
        }
        String str1 = pair1.type.toString();
        String str2 = pair1.type.toString();
        //default int is 64
        if (str1.equals("INTEGER_SHAPE") && str2.equals("INTEGER_64")) {
            return true;
        } else if (str2.equals("INTEGER_SHAPE") && str1.equals("INTEGER_64")) {
            return true;
        }

        //might be "INTEGER_SHAPE", "INTEGER_8", etc
        return pair1.type.toString().equals(pair2.type.toString());
    }

    public static int calcFieldSize(DStructType structType, String fieldName) {
        TypePair pair1 = DValueHelper.findField(structType, fieldName);
        if (pair1.type.isShape(Shape.INTEGER)) {
            String str1 = pair1.type.toString();
            switch (str1) {
                case "INTEGER_SHAPE":
                case "INTEGER_64":
                    return 64;
                case "INTEGER_32":
                    return 32;
                case "INTEGER_16":
                    return 16;
                case "INTEGER_8":
                    return 8;
                default:
                    return 0;
            }
        } else if (pair1.type.isShape(Shape.STRING)) {
            DRule rule1 = DRuleHelper.findSizeofRule(structType, structType.getRawRules(), fieldName);
            int sizeof1 = DRuleHelper.getSizeofAmount(rule1, 0);
            return sizeof1;
        } else {
            return 0;
        }
    }

    public static DType getTypeOrPKType(DStructType structType, String fieldName) {
        DType previousType = DValueHelper.findFieldType(structType, fieldName);
        if (previousType.isStructShape()) {
            TypePair pkpair = DValueHelper.findPrimaryKeyFieldPair(structType);
            if (pkpair != null) {
                return pkpair.type;
            }
        }
        return previousType;
    }
}
