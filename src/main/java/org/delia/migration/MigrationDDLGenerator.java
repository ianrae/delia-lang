package org.delia.migration;

import org.delia.Delia;
import org.delia.DeliaSession;
import org.delia.api.DeliaSessionImpl;
import org.delia.core.FactoryService;
import org.delia.db.SqlStatement;
import org.delia.hld.dat.DatService;
import org.delia.lld.LLD;
import org.delia.log.DeliaLog;
import org.delia.migration.action.*;
import org.delia.migrationparser.RelationDetails;
import org.delia.migrationparser.parser.RelationGenerator;
import org.delia.rule.rules.UniqueFieldsRule;
import org.delia.sql.*;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.TypePair;
import org.delia.util.DValueHelper;
import org.delia.util.StrCreator;
import org.delia.valuebuilder.ScalarValueBuilder;
import org.delia.varevaluator.DoNothingVarEvaluator;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

public class MigrationDDLGenerator {

    private final DeliaLog log;
    private StrCreator sc;
    private final SqlTypeConverter sqlTypeConverter;
    private final FactoryService factorySvc;
    private final Delia delia;
    private CreateTableSqlGenerator createTableSqlGenerator;
    private FieldSqlGenerator fieldGen;
    private CreateAssocTableSqlGenerator createAssocTableSqlGenerator;

    public MigrationDDLGenerator(DeliaLog log, Delia delia) {
        this.log = log;
        this.delia = delia;
        this.sc = new StrCreator();
        this.sqlTypeConverter = new SqlTypeConverter(delia.getOptions());
        this.factorySvc = delia.getFactoryService();
    }

    //TODO: need topoSort on migration actions
    public List<SqlStatement> generateSql(SchemaMigration schemaMigration, MigrationActionBuilder migrationBuilder) {
        List<SqlStatement> list = new ArrayList<>();
        for (MigrationActionBase act : schemaMigration.actions) {
            sc = new StrCreator();
            if (act instanceof DeleteTableAction) {
                doDeleteTable((DeleteTableAction) act, list);
            } else if (act instanceof CreateTableAction) {
                doCreateTable((CreateTableAction) act, migrationBuilder.getFinalSess(), list);
            } else if (act instanceof RenameTableAction) {
                doRenameTable((RenameTableAction) act, list);
            } else if (act instanceof RenameFieldAction) {
                doRenameField((RenameFieldAction) act, migrationBuilder.getFinalSess(), list);
            } else if (act instanceof RemoveFieldAction) {
                doRemoveField((RemoveFieldAction) act, migrationBuilder.getFinalSess(), list);
            } else if (act instanceof AlterFieldAction) {
                doAlterField((AlterFieldAction) act, migrationBuilder.getFinalSess(), list);
            } else if (act instanceof AddFieldAction) {
                doAddField((AddFieldAction) act, migrationBuilder.getFinalSess(), list);
            }
            //TODO for AlterUniqueFieldsConstraint
        }
        return list;
    }

    // -- tables --
    private void doCreateTable(CreateTableAction action, DeliaSession sess, List<SqlStatement> list) {
        createTablSqlGenIfNeeded(sess);
        if (action.isAssocTbl) {
            LLD.LLCreateAssocTable createTable = findCreateAssocTableLLD(sess, action.structType);
            SqlStatement stmt = createAssocTableSqlGenerator.render(createTable);
            list.add(stmt);
            return;
        }
        LLD.LLCreateTable createTable = findCreateTableLLD(sess, action.structType);
        SqlStatement stmt = createTableSqlGenerator.render(createTable);
        list.add(stmt);
    }

    private void createTablSqlGenIfNeeded(DeliaSession sess) {
        if (createTableSqlGenerator != null) return;
        //TODO i don't think any vars exist when we do migration so a DoNothingVarEvaluator() should be fine
        SqlValueRenderer sqlValueRenderer = new SqlValueRenderer(factorySvc, new DoNothingVarEvaluator());
        ScalarValueBuilder valueBuilder = new ScalarValueBuilder(factorySvc, sess.getRegistry());
        DatService datSvc = sess.getDatIdMap();
        this.createTableSqlGenerator = new CreateTableSqlGenerator(factorySvc, sqlValueRenderer, valueBuilder, datSvc, delia.getOptions());
        this.createAssocTableSqlGenerator = new CreateAssocTableSqlGenerator(factorySvc, sqlValueRenderer, valueBuilder, datSvc, delia.getOptions());
        this.fieldGen = new FieldSqlGenerator(factorySvc, delia.getOptions());
    }

    private void doDeleteTable(DeleteTableAction action, List<SqlStatement> list) {
        SqlStatement stmt = createStatement(list);
        stmt.sql = String.format("DROP TABLE IF EXISTS %s;", buildTableName(action.structType));
    }

    private void doRenameTable(RenameTableAction action, List<SqlStatement> list) {
        //TODO only support rename within its current schema
        //note we use renderFieldName even though its a tablename
        DStructType structType = action.structType;
        SqlStatement stmt = createStatement(list);
        stmt.sql = String.format("ALTER TABLE %s RENAME TO %s;", buildTableName(structType), renderFieldName(action.newName));
    }


    // -- fields --
    private void doAddField(AddFieldAction action, DeliaSession sess, List<SqlStatement> list) {
        createTablSqlGenIfNeeded(sess);
        TypePair pair = new TypePair(action.fieldName, action.type);
        //action.fieldName does not yet exist in structType
        RelationDetails relDetails = action.buildDetails();
        if (!fieldGen.isPhysicalField(relDetails, pair)) {
            return;
        }

        List<TypePair> fieldsNeedingConstraints = new ArrayList<>();
        if (fieldGen.fieldNeedsConstraints(action.structType, pair)) {
            fieldsNeedingConstraints.add(pair);
        }
        List<UniqueFieldsRule> uniqueFieldsList = UniqueFieldsRuleHelper.buildUniqueFields(action.structType);

        sc.o("ALTER TABLE %s ADD COLUMN", buildTableName(action.structType));
        fieldGen.renderField(sc, action.structType, pair, true, action.sizeOf, action.changeFlags);
        sc.addStr(";");
        generateStatementIfNeeded(list);

        //constraints
        if (!fieldsNeedingConstraints.isEmpty()) {
            fieldGen.renderFKConstraint(sc, action.structType, pair);
        }
        fieldGen.doUniqueFields(sc, uniqueFieldsList);
        generateStatementIfNeeded(list);
    }

    private void doRemoveField(RemoveFieldAction action, DeliaSession sess, List<SqlStatement> list) {
        createTablSqlGenIfNeeded(sess);
        if (! action.isPhysicalField) {
            return;
        }
//        TypePair pair = DValueHelper.findField(action.structType, action.fieldName);
//        if (!fieldGen.isPhysicalField(action.structType, pair)) {
//            return;
//        }
        sc.o("ALTER TABLE %s", buildTableName(action.structType));
        sc.o(" DROP COLUMN %s;", action.fieldName);
        SqlStatement stmt = createStatement(list);
        stmt.sql = sc.toString();
    }

    private void doRenameField(RenameFieldAction action, DeliaSession sess, List<SqlStatement> list) {
        createTablSqlGenIfNeeded(sess);
        DStructType finalStructType = sess.getRegistry().getStructType(action.structType.getTypeName());
        TypePair pair = DValueHelper.findField(finalStructType, action.newName);
        if (!fieldGen.isPhysicalField(finalStructType, pair)) {
            return;
        }
        sc.o("ALTER TABLE %s", buildTableName(finalStructType));
        sc.o(" RENAME COLUMN %s TO %s;", action.fieldName, action.newName);
        SqlStatement stmt = createStatement(list);
        stmt.sql = sc.toString();
    }

    private void doAlterField(AlterFieldAction action, DeliaSession sess, List<SqlStatement> list) {
        createTablSqlGenIfNeeded(sess);
        TypePair pair = new TypePair(action.fieldName, action.type);

//            List<TypePair> fieldsNeedingConstraints = new ArrayList<>();
//            if (fieldGen.fieldNeedsConstraints(action.structType, pair)) {
//                fieldsNeedingConstraints.add(pair);
//            }
//            List<UniqueFieldsRule> uniqueFieldsList = UniqueFieldsRuleHelper.buildUniqueFields(action.structType);

        String prefix = String.format("ALTER TABLE %s ALTER COLUMN %s", buildTableName(action.structType), renderFieldName(action.fieldName));

        //    public String changeFlags; //eg +O+U
        //TODO: P and S and U
        if (action.changeFlags.contains("+O")) {
            sc.addStr(prefix);
            sc.o(" DROP NOT NULL;");
        } else if (action.changeFlags.contains("-O")) {
            sc.addStr(prefix);
            sc.o(" SET NOT NULL;");
        }
        generateStatementIfNeeded(list);

        DType previousTypeOrPkType = MigrationHelper.getTypeOrPKType(action.structType, action.fieldName);
        int sizeofAmount1 = MigrationHelper.calcFieldSize(action.structType, action.fieldName);
        int sizeofAmount2 = action.sizeOf;
        if ((action.type != null && action.type != previousTypeOrPkType) || (sizeofAmount1 != sizeofAmount2)) {
            //when relation-to-int we don't want to do anything here
            sc.addStr(prefix);
            String sqlType;
            if (pair.type.isStructShape()) {
                sqlType = sqlTypeConverter.getSqlType(action.structType, pair.name);
            } else {
                sqlType = fieldGen.getScalarType(action.structType, pair, action.sizeOf);
            }

            sc.o(" TYPE %s;", sqlType);
            generateStatementIfNeeded(list);
        }

        TypePair prevPair = DValueHelper.findField(action.structType, action.fieldName);
        RelationDetails relDetails = RelationGenerator.buildRelationDetails(action.structType, prevPair, null);
        boolean isScalarToRelation = false;
        if (relDetails == null && action.buildDetails().anyAreSet()) {
            isScalarToRelation = true;
        }
        if (!isScalarToRelation &&
                (relDetails == null || (action.isParent == relDetails.isParent && action.isOne == relDetails.isOne && action.isMany == relDetails.isMany))) {
            //no change
        } else {
            sc.o("ALTER TABLE %s", buildTableName(action.structType));
            sc.o(" DROP CONSTRAINT IF EXISTS %s ;", fieldGen.buildFKConstraintName(action.structType, prevPair));
            generateStatementIfNeeded(list);

            //TODO fix this when one-to-one are rendered unique
            if (pair.type.isStructShape()) {
                fieldGen.renderFKConstraint(sc, action.structType, pair);
                generateStatementIfNeeded(list);
            }
        }

        //constraints
//            fieldGen.doUniqueFields(sc, uniqueFieldsList);
    }

    private void generateStatementIfNeeded(List<SqlStatement> list) {
        if (!sc.toString().isEmpty()) {
            SqlStatement stmt = createStatement(list);
            stmt.sql = sc.toString();
            sc = new StrCreator();
        }
    }


    private LLD.LLCreateTable findCreateTableLLD(DeliaSession sess, DStructType structType) {
        DeliaSessionImpl sessimpl = (DeliaSessionImpl) sess;
        List<LLD.LLCreateTable> list = sessimpl.mostRecentExecutable.lldStatements.stream()
                .filter(x -> x instanceof LLD.LLCreateTable).map(x -> (LLD.LLCreateTable) x).collect(Collectors.toList());
        for (LLD.LLCreateTable createTable : list) {
            if (createTable.table.physicalType == structType) {
                return createTable;
            }
        }
        return null;
    }

    private LLD.LLCreateAssocTable findCreateAssocTableLLD(DeliaSession sess, DStructType structType) {
        DeliaSessionImpl sessimpl = (DeliaSessionImpl) sess;
        List<LLD.LLCreateAssocTable> list = sessimpl.mostRecentExecutable.lldStatements.stream()
                .filter(x -> x instanceof LLD.LLCreateAssocTable).map(x -> (LLD.LLCreateAssocTable) x).collect(Collectors.toList());
        for (LLD.LLCreateAssocTable createTable : list) {
            if (createTable.getTableName().equals(structType.getName())) {
                return createTable;
            }
        }
        return null;
    }

    private SqlStatement createStatement(List<SqlStatement> list) {
        SqlStatement stmt = new SqlStatement();
        list.add(stmt);
        return stmt;
    }

    private String renderFieldName(String name) {
        return name.toLowerCase(Locale.ROOT);
    }

    private String buildTableName(DStructType structType) {
        String schema = structType.getSchema();
        if (isNull(schema)) {
            return structType.getName().toLowerCase(Locale.ROOT);
        } else {
            String s = String.format("%s.%s", schema, structType.getName());
            return s.toLowerCase(Locale.ROOT);
        }
    }
}
