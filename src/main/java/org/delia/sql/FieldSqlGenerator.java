package org.delia.sql;

import org.delia.DeliaOptions;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.dval.DValueExConverter;
import org.delia.lld.LLD;
import org.delia.migrationparser.RelationDetails;
import org.delia.relation.RelationCardinality;
import org.delia.relation.RelationInfo;
import org.delia.rule.RuleOperand;
import org.delia.rule.rules.UniqueFieldsRule;
import org.delia.type.DStructType;
import org.delia.type.DValue;
import org.delia.type.PrimaryKey;
import org.delia.type.TypePair;
import org.delia.util.DRuleHelper;
import org.delia.util.DValueHelper;
import org.delia.util.ListWalker;
import org.delia.util.StrCreator;
import org.delia.valuebuilder.ScalarValueBuilder;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class FieldSqlGenerator extends ServiceBase {

    private final SqlTypeConverter sqlTypeConverter;
    private final SqlTableNameMapper sqlTableNameMapper;
    private final SqlValueRenderer sqlValueRenderer;
    private final ScalarValueBuilder valueBuilder;

    public FieldSqlGenerator(FactoryService factorySvc, DeliaOptions deliaOptions, SqlTableNameMapper sqlTableNameMapper,
                             ScalarValueBuilder valueBuilder) {
        super(factorySvc);
        this.sqlTypeConverter = new SqlTypeConverter(deliaOptions);
        this.sqlTableNameMapper = sqlTableNameMapper;
        this.sqlValueRenderer = new SqlValueRenderer(factorySvc);
        this.valueBuilder = valueBuilder;
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
                //30Sep2023:fix bug. one-way relation is not UNIQUE
                boolean isUnique = relinfo.cardinality.equals(RelationCardinality.ONE_TO_ONE) && !relinfo.isOneWayRelation();
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
        String tblName2 = sqlTableNameMapper.calcSqlTableName((DStructType) pair.type);
        sc.o(" CONSTRAINT %s FOREIGN KEY (%s) REFERENCES %s (%s)", constraintName, pair.name,
                tblName2, pkpair.name);
    }

    private String calcTableName(DStructType structType) {
        return sqlTableNameMapper.calcSqlTableNameOnly(structType); //name w/o schema
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
        boolean isCompositePK = physicalType.getPrimaryKey().isMultiple();
        if (hasFlag(changeFlags, "S")) {
            isSerial = true;
        }
        if (hasFlag(changeFlags, "P")) {
            isPrimaryKey = true;
        }
        Optional<String> defaultValue = physicalType.fieldHasDefaultValue(fieldName);

        if (isSerial) {
            sc.o("  %s", fieldName);
        } else {
            sc.o("  %s %s", fieldName, sqlType);
        }

        //TODO delete!!!!!!!!
//        if (fieldName.equals("cust")) {
//            System.out.println("sdf");
//        }

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
            if (defaultValue.isPresent()) {
                DValueExConverter converter = new DValueExConverter(factorySvc, valueBuilder.getRegistry());
                TypePair pair = DValueHelper.findField(physicalType, fieldName);
                DValue inner = converter.buildFromObject(defaultValue.get(), pair.type);
                String ss = sqlValueRenderer.renderAsSql(inner, pair.type, physicalType);
                sc.o(" DEFAULT %s", ss);
            }
        }
        if (isSerial) {
            sc.o(" SERIAL");
        }
        if (isPrimaryKey && !isCompositePK) {
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
            UniqueFieldsRule rule = rawRule;
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

    public void generateCompositePrimaryKey(StrCreator sc, DStructType physicalType) {
        if (!physicalType.getPrimaryKey().isMultiple()) {
            return;
        }

        /* CONSTRAINT constraint_name PRIMARY KEY(column_1, column_2,...);
         */
        String constraintName = String.format("pk_constraint");
        sc.o("CONSTRAINT %s PRIMARY KEY(", constraintName);

        PrimaryKey primaryKey = physicalType.getPrimaryKey();

        ListWalker<TypePair> walker1 = new ListWalker<>(primaryKey.getKeys());
        while (walker1.hasNext()) {
            TypePair pair = walker1.next();
            sc.o(" %s", pair.name);
            walker1.addIfNotLast(sc, ", ");
        }

        sc.o(")");
        sc.nl();
    }
}
