package org.delia.sql;

import org.delia.DeliaOptions;
import org.delia.rule.DRule;
import org.delia.rule.rules.SizeofRule;
import org.delia.type.DType;
import org.delia.type.EffectiveShape;
import org.delia.type.TypePair;
import org.delia.util.DRuleHelper;
import org.delia.util.DValueHelper;

import java.util.Collections;
import java.util.List;

public class SqlTypeConverter {

    private final DeliaOptions options;
    private final String stringType;

    public SqlTypeConverter(DeliaOptions deliaOptions) {
        this.options = deliaOptions;
        this.stringType = String.format("VARCHAR(%d)", deliaOptions.defaultStringColumnLength);
    }

    public String getSqlType(DType dtype) {
        switch (dtype.getShape()) {
            case INTEGER: {
                DRule rule = findMatchingRule(dtype, Collections.emptyList(), null);
                return getIntegerSqlType(dtype, rule);
            }
//            case LONG:
//                return "BIGINT";
            case NUMBER:
                return "DOUBLE PRECISION";
            case DATE:
                return calcDateOrTimeType(dtype); //"TIMESTAMP";
            case STRING: {
                DRule rule = findMatchingRule(dtype, Collections.emptyList(), null);
                return getStringSqlType(dtype, rule);
            }
            case BOOLEAN:
                return "BOOLEAN";
            default:
                return "VARCHAR(100)"; //TODO fix
        }
    }

    public String getSqlType(DType structType, String fieldName) {
        TypePair pair = DValueHelper.findField(structType, fieldName);
        switch (pair.type.getShape()) {
            case INTEGER: {
                DRule rule = findMatchingRule(pair.type, structType.getRawRules(), fieldName);
                return getIntegerSqlType(pair.type, rule);
            }
//            case LONG:
//                return "BIGINT";
            case NUMBER:
                return "DOUBLE PRECISION";
            case DATE:
                return calcDateOrTimeType(pair.type); //"TIMESTAMP";
            case STRING: {
                DRule rule = findMatchingRule(pair.type, structType.getRawRules(), fieldName);
                return getStringSqlType(pair.type, rule);
            }
            case BOOLEAN:
                return "BOOLEAN";
            case BLOB:
                return "bytea";
            default:
                return "VARCHAR(100)"; //TODO fix
        }
    }

    public String getSqlType(TypePair pair, int sizeofAmount) {
        switch (pair.type.getShape()) {
            case INTEGER:
                return getIntegerSqlType(sizeofAmount);
//            case LONG:
//                return "BIGINT";
            case NUMBER:
                return "DOUBLE PRECISION";
            case DATE:
                return calcDateOrTimeType(pair.type); //"TIMESTAMP";
            case STRING: {
                return getStringSqlType(sizeofAmount);
            }
            case BOOLEAN:
                return "BOOLEAN";
            case BLOB:
                return "bytea";
            default:
                return "VARCHAR(100)"; //TODO fix
        }
    }

    private String getIntegerSqlType(DType dtype, DRule rule) {
        if (rule == null) {
            return "INTEGER";
        } else {
            int sizeofAmount = ((SizeofRule) rule).getSizeofAmount();
            return getIntegerSqlType(sizeofAmount);
        }
    }

    private String getIntegerSqlType(int sizeofAmount) {
        switch (sizeofAmount) {
            case 8:
            case 16:
                return "SMALLINT";
            case 64:
                return "BIGINT";
            default:
            case 32:
                return "INTEGER";
        }
    }

    private String getStringSqlType(DType dtype, DRule rule) {
        if (rule == null) {
            return stringType;
        } else {
            int sizeofAmount = ((SizeofRule) rule).getSizeofAmount();
            return String.format("VARCHAR(%d)", sizeofAmount);
        }
    }
    private String getStringSqlType(int sizeofAmount) {
        if (sizeofAmount == 0) {
            return stringType;
        } else {
            return String.format("VARCHAR(%d)", sizeofAmount);
        }
    }

    private DRule findMatchingRule(DType dtype, List<DRule> structRules, String fieldName) {
        return DRuleHelper.findSizeofRule(dtype, structRules, fieldName);
    }

    private String calcDateOrTimeType(DType type) {
        if (type.getEffectiveShape() == null) {
            return "TIMESTAMP";
        } else if (type.getEffectiveShape().equals(EffectiveShape.EFFECTIVE_DATE_ONLY)) {
            return "DATE";
        } else if (type.getEffectiveShape().equals(EffectiveShape.EFFECTIVE_TIME_ONLY)) {
            return "TIME";
        } else {
            return "TIMESTAMP";
        }
    }

}
