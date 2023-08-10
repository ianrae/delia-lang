package org.delia.util;

import org.delia.type.DTypeName;

import java.util.List;
import java.util.Locale;
import java.util.StringJoiner;

public class DTypeNameUtil {

    public static String flatten(List<DTypeName> list) {
        StringJoiner joiner = new StringJoiner(",");
        for(DTypeName typeName: list) {
            String s = formatLowerCaseTableName(typeName);
            joiner.add(s.trim());
        }
        return joiner.toString();
    }

    public static String formatLowerCaseTableName(DTypeName typeName) {
        String s = (typeName.getSchema() == null) ? typeName.getTypeName() :
                String.format("%s.%s", typeName.getSchema(), typeName.getTypeName());
        s = s.toLowerCase(Locale.ROOT); //our sql tablename is lowercase
        return s;
    }
    public static String formatForDisplay(DTypeName typeName) {
        String s = (typeName.getSchema() == null) ? typeName.getTypeName() :
                String.format("%s.%s", typeName.getSchema(), typeName.getTypeName());
        return s;
    }
    public static String formatNoDots(DTypeName typeName) {
        String s = (typeName.getSchema() == null) ? typeName.getTypeName() :
                String.format("%s_%s", typeName.getSchema(), typeName.getTypeName());
        return s;
    }

}
