package org.delia.sql;

import org.delia.DeliaOptions;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.SqlStatement;
import org.delia.hld.dat.DatService;
import org.delia.lld.LLD;
import org.delia.rule.rules.UniqueFieldsRule;
import org.delia.util.ListWalker;
import org.delia.util.StrCreator;
import org.delia.valuebuilder.ScalarValueBuilder;

import java.util.ArrayList;
import java.util.List;

public class CreateTableSqlGenerator extends ServiceBase {

    private final SqlValueRenderer sqlValueRenderer;
    private final ScalarValueBuilder valueBuilder;
    private final DatService datSvc;
    private final SqlTypeConverter sqlTypeConverter;
    private final DeliaOptions options;
    private final FieldSqlGenerator fieldGen;
    private final SqlTableNameMapper sqlTableNameMapper;

    public CreateTableSqlGenerator(FactoryService factorySvc, SqlValueRenderer sqlValueRenderer, ScalarValueBuilder valueBuilder, 
                                   DatService datSvc, DeliaOptions deliaOptions, SqlTableNameMapper sqlTableNameMapper) {
        super(factorySvc);
        this.sqlValueRenderer = sqlValueRenderer;
        this.valueBuilder = valueBuilder;
        this.datSvc = datSvc;
        this.sqlTypeConverter = new SqlTypeConverter(deliaOptions);
        this.options = deliaOptions;
        this.fieldGen = new FieldSqlGenerator(factorySvc, deliaOptions, sqlTableNameMapper, valueBuilder);
        this.sqlTableNameMapper = sqlTableNameMapper;
    }

    public SqlStatement render(LLD.LLCreateTable statement) {
        StrCreator sc = new StrCreator();
        sc.o("CREATE TABLE IF NOT EXISTS %s (", statement.getTableName());
        sc.nl();
        List<LLD.LLField> fieldsNeedingConstraints = buildConstraintList(statement);
        List<UniqueFieldsRule> uniqueFieldsList = fieldGen.buildUniqueFields(statement);
        boolean isMoreAfterFields = fieldsNeedingConstraints.size() > 0 || uniqueFieldsList.size() > 0;

        ListWalker<LLD.LLField> walker = new ListWalker<>(buildFieldList(statement));
        while (walker.hasNext()) {
            LLD.LLField field = walker.next();
            fieldGen.renderField(sc, statement.table.physicalType, field.physicalPair);
//            if (field.physicalPair.type.isStructShape()) {
//                RelationInfo relinfo = DRuleHelper.findMatchingRuleInfo(statement.table.physicalType, field.getFieldName());
//                if (relinfo.isParent || relinfo.isManyToMany()) {
//                } else {
//                    TypePair pkpair = DValueHelper.findPrimaryKeyFieldPair(field.physicalPair.type);
//                    String sqlType = sqlTypeConverter.getSqlType(field.physicalPair.type, pkpair.name); //Address.id
//                    boolean isUnique = relinfo.cardinality.equals(RelationCardinality.ONE_TO_ONE);
//                    fieldGen.doScalarField(sc, field.physicalTable.physicalType, field.getFieldName(), sqlType, isUnique);
//                }
//            } else {
//                String sqlType = sqlTypeConverter.getSqlType(field.physicalTable.physicalType, field.getFieldName());
//                fieldGen.doScalarField(sc, field.physicalTable.physicalType, field.getFieldName(), sqlType, false);
//            }

            if (isMoreAfterFields || walker.hasNext()) {
                sc.addStr(", ");
            }
            sc.nl();
        }

        //constraints
        isMoreAfterFields = uniqueFieldsList.size() > 0;
        walker = new ListWalker<>(fieldsNeedingConstraints);
        while (walker.hasNext()) {
            LLD.LLField field = walker.next();
            fieldGen.renderFKConstraint(sc, statement.table.physicalType, field.physicalPair);
//            TypePair pkpair = DValueHelper.findPrimaryKeyFieldPair(field.physicalPair.type);
//            String tblName = DTypeNameUtil.formatNoDots(statement.table.physicalType.getTypeName());
//            String constraintName = String.format("FK_%s_%s", tblName, field.physicalPair.name).toUpperCase(Locale.ROOT);
//            String tblName2 = DTypeNameUtil.formatSqlTableName(field.physicalPair.type.getTypeName());
//            sc.o(" CONSTRAINT %s FOREIGN KEY (%s) REFERENCES %s (%s)", constraintName, field.physicalPair.name,
//                    tblName2, pkpair.name);

            if (isMoreAfterFields || walker.hasNext()) {
                sc.addStr(", ");
            }
            sc.nl();
        }

        //constraints
        fieldGen.doUniqueFields(sc, uniqueFieldsList);

        sc.o(");");

        SqlStatement sqlStatement = new SqlStatement();
        sqlStatement.sql = sc.toString();
        return sqlStatement;
    }

    private List<LLD.LLField> buildFieldList(LLD.LLCreateTable statement) {
        List<LLD.LLField> fields = new ArrayList<>();
        for(LLD.LLField field: statement.fields) {
            if (fieldGen.isPhysicalField(statement.table.physicalType, field.physicalPair)) {
                fields.add(field);
            }
//            if (field.physicalPair.type.isStructShape()) {
//                RelationInfo relinfo = DRuleHelper.findMatchingRuleInfo(statement.table.physicalType, field.getFieldName());
//                if (relinfo.isParent || relinfo.isManyToMany()) {
//                } else {
//                }
//            } else {
//                fields.add(field);
//            }
        }
        return fields;
    }

    private List<LLD.LLField> buildConstraintList(LLD.LLCreateTable statement) {
        List<LLD.LLField> fieldsNeedingConstraints = new ArrayList<>();
        for(LLD.LLField field: statement.fields) {
            if (fieldGen.fieldNeedsConstraints(statement.table.physicalType, field.physicalPair)) {
                fieldsNeedingConstraints.add(field);
            }
//            if (field.physicalPair.type.isStructShape()) {
//                RelationInfo relinfo = DRuleHelper.findMatchingRuleInfo(statement.table.physicalType, field.getFieldName());
//                if (relinfo.isParent || relinfo.isManyToMany()) {
//                } else {
//                    fieldsNeedingConstraints.add(field);
//                }
//            }
        }
        return fieldsNeedingConstraints;
    }



}
