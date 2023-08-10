package org.delia.sql;

import org.delia.DeliaOptions;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.lld.LLD;
import org.delia.migrationparser.RelationDetails;
import org.delia.relation.RelationCardinality;
import org.delia.relation.RelationInfo;
import org.delia.rule.RuleOperand;
import org.delia.rule.rules.UniqueFieldsRule;
import org.delia.type.DStructType;
import org.delia.type.TypePair;
import org.delia.util.*;

import java.util.List;
import java.util.Locale;

public class FieldSqlGenerator extends ServiceBase {

    private final SqlTypeConverter sqlTypeConverter;
    private final SqlTableNameMapper sqlTableNameMapper;

    public FieldSqlGenerator(FactoryService factorySvc, DeliaOptions deliaOptions, SqlTableNameMapper sqlTableNameMapper) {
        super(factorySvc);
        this.sqlTypeConverter = new SqlTypeConverter(deliaOptions);
        this.sqlTableNameMapper = sqlTableNameMapper;
    }

    public void renderField(StrCreator sc, DStructType structType, TypePair pair) {
        renderField(sc, structType, pair, false, 0, null);
    }

    public void renderField(StrCreator sc, DStructType structType, TypePair pair, boolean isNewField, int sizeofAmount, String changeFlags) {
        if (pair.type.isStructShape()) {
            RelationInfo relinfo = DRuleHelper.findMatchingRuleInfo(structType, pair.name);
            if (relinfo.isParent || relinfo.isManyToMany()) {
            } else {
                TypePair pkpair = DValueHelper.findPrimaryKeyFieldPair(pair.type);
                String sqlType = sqlTypeConverter.getSqlType(pair.type, pkpair.name); //Address.id
                boolean isUnique = relinfo.cardinality.equals(RelationCardinality.ONE_TO_ONE);
                doScalarField(sc, structType, pair.name, sqlType, isUnique, changeFlags);
            }
        } else {
            int n = isNewField ? sizeofAmount : 0;
            if (isNewField && n == 0) {
                n = 1; //hack to force .getSqlType(pair, sizeofAmount) which will return "INTEGER"
            }
            String sqlType = getScalarType(structType, pair, n);
            doScalarField(sc, structType, pair.name, sqlType, false, changeFlags);
        }
    }

    public String getScalarType(DStructType structType, TypePair pair, int sizeofAmount) {
        String sqlType = (sizeofAmount > 0) ? sqlTypeConverter.getSqlType(pair, sizeofAmount) : sqlTypeConverter.getSqlType(structType, pair.name);
        return sqlType;
    }

    //TODO: i think one-to-one should also have UNIQUE
    public void renderFKConstraint(StrCreator sc, DStructType structType, TypePair pair) {
        TypePair pkpair = DValueHelper.findPrimaryKeyFieldPair(pair.type);
        String constraintName = buildFKConstraintName(structType, pair);
        String tblName2 = calcTableName((DStructType) pair.type);
        sc.o(" CONSTRAINT %s FOREIGN KEY (%s) REFERENCES %s (%s)", constraintName, pair.name,
                tblName2, pkpair.name);
    }

    private String calcTableName(DStructType structType) {
        return sqlTableNameMapper.calcSqlTableName(structType);
    }

    public String buildFKConstraintName(DStructType structType, TypePair pair) {
        String tblName = calcTableName(structType);
        String constraintName = String.format("FK_%s_%s", tblName, pair.name).toUpperCase(Locale.ROOT);
        return constraintName;
    }

    public void doScalarField(StrCreator sc, DStructType physicalType, String fieldName, String sqlType, boolean isUniqueOverride,
                              String changeFlags) {
        boolean isSerial = physicalType.fieldIsSerial(fieldName);
        boolean isPrimaryKey = physicalType.fieldIsPrimaryKey(fieldName);
        if (hasFlag(changeFlags, "S")) {
            isSerial = true;
        }
        if (hasFlag(changeFlags, "P")) {
            isPrimaryKey = true;
        }

        if (isSerial) {
            sc.o("  %s", fieldName);
        } else {
            sc.o("  %s %s", fieldName, sqlType);
        }

        if (!isPrimaryKey) {
            boolean isOptional = physicalType.fieldIsOptional(fieldName);
            if (hasFlag(changeFlags, "O")) {
                isOptional = true;
            }
            boolean isUnique = physicalType.fieldIsUnique(fieldName);
            if (hasFlag(changeFlags, "U")) {
                isUnique = true;
            }

            if (!isSerial && !isOptional) {
                sc.o(" NOT NULL");
            }
            if (isUniqueOverride || isUnique) {
                sc.o(" UNIQUE");
            }
        }
        if (isSerial) {
            sc.o(" SERIAL");
        }
        if (isPrimaryKey) {
            sc.o(" PRIMARY KEY");
        }
    }

    private boolean hasFlag(String changeFlags, String s) {
        if (changeFlags == null) return false;
        return changeFlags.contains(s);
    }

    public boolean isPhysicalField(DStructType structType, TypePair pair) {
        if (pair.type.isStructShape()) {
            RelationInfo relinfo = DRuleHelper.findMatchingRuleInfo(structType, pair.name);
            if (relinfo.isParent || relinfo.isManyToMany()) {
            } else {
                return true;
            }
        } else {
            return true;
        }
        return false;
    }

    public boolean isPhysicalField(RelationDetails relDetails, TypePair pair) {
        if (pair.type.isStructShape()) {
            //NOTE: isMany could be M:1 or M:N. In this case (of checking if field is physical field in db)
            //that's ok - the many side is never physical
            if (relDetails.isParent || relDetails.isMany) {
            } else {
                return true;
            }
        } else {
            return true;
        }
        return false;
    }

    public boolean fieldNeedsConstraints(DStructType structType, TypePair pair) {
        if (pair.type.isStructShape()) {
            return isPhysicalField(structType, pair);
        } else {
            return false;
        }
    }

    public List<UniqueFieldsRule> buildUniqueFields(LLD.LLCreateTable statement) {
        return UniqueFieldsRuleHelper.buildUniqueFields(statement.table.physicalType);
    }

    public void doUniqueFields(StrCreator sc, List<UniqueFieldsRule> uniqueFieldsList) {
        if (uniqueFieldsList.isEmpty()) return;

        ListWalker<UniqueFieldsRule> walker1 = new ListWalker<>(uniqueFieldsList);
        while (walker1.hasNext()) {
            sc.o(" UNIQUE(");
            UniqueFieldsRule rawRule = walker1.next();
            ;
            UniqueFieldsRule rule = (UniqueFieldsRule) rawRule;
            ListWalker<RuleOperand> walker = new ListWalker<>(rule.getOperList());
            while (walker.hasNext()) {
                RuleOperand oper = walker.next();
                String fieldName = oper.getSubject();
                sc.o("%s", fieldName);
                walker.addIfNotLast(sc, ", ");
            }
            sc.o(")");
            walker1.addIfNotLast(sc, ", ");
            sc.nl();
        }
    }
}
