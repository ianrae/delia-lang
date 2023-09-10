package org.delia.sql;

import org.delia.DeliaOptions;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.SqlStatement;
import org.delia.hld.dat.DatService;
import org.delia.lld.LLD;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.util.ListWalker;
import org.delia.util.StrCreator;
import org.delia.valuebuilder.ScalarValueBuilder;
import org.delia.varevaluator.VarEvaluator;

import java.util.List;
import java.util.stream.Collectors;

public class LLDInsertGenerator extends ServiceBase {

    private final SqlValueRenderer sqlValueRenderer;
    private final DeliaOptions deliaOptions;
    private final ScalarValueBuilder valueBuilder;
    private final AssocSqlGenerator assocSqlGenerator;

    public LLDInsertGenerator(FactoryService factorySvc, DeliaOptions deliaOptions, DTypeRegistry registry, DatService datSvc, VarEvaluator varEvaluator, SqlTableNameMapper sqlTableNameMapper) {
        super(factorySvc);
        this.sqlValueRenderer = new SqlValueRenderer(factorySvc);
        this.valueBuilder = new ScalarValueBuilder(factorySvc, registry);
        this.deliaOptions = deliaOptions;
        this.assocSqlGenerator = new AssocSqlGenerator(factorySvc, sqlValueRenderer, valueBuilder, datSvc, sqlTableNameMapper);
    }

    public SqlStatement render(LLD.LLInsert statement) {
        if (statement.subQueryInfo != null) {
            return assocSqlGenerator.renderInsertSubQuery(statement);
        }

        /* INSERT INTO table_name(column1, column2, …) VALUES (value1, value2, …); */
        //the fieldL is all the logical fields involved.
        //Because of ManyToMany relations, some of those fields may be in an assoc table, not in the statement's table
        List<LLD.LLFieldValue> fieldsToInsert = statement.fieldL.stream()
                .filter(fld -> !fld.field.isAssocField).collect(Collectors.toList());
        if (fieldsToInsert.isEmpty() && !statement.areFieldsToInsert()) {
            return null;
        }

        SqlStatement sqlStatement = new SqlStatement();
        StrCreator sc = new StrCreator();
        if (fieldsToInsert.isEmpty()) {
            sc.o("INSERT INTO %s  VALUES(DEFAULT", statement.getTableName());
        } else {
            sc.o("INSERT INTO %s (", statement.getTableName());
            ListWalker<LLD.LLFieldValue> walker = new ListWalker<>(fieldsToInsert);
            while (walker.hasNext()) {
                LLD.LLFieldValue field = walker.next();
                sc.o("%s", field.field.getFieldName());
                walker.addIfNotLast(sc, ", ");
            }
            sc.o(")");
            sc.nl();
            sc.o(" VALUES(");

            walker = new ListWalker<>(fieldsToInsert);
            while (walker.hasNext()) {
                LLD.LLFieldValue field = walker.next();
//            sc.o("%s", renderAsSql(field.dval, field.field.physicalPair.type, field.field.physicalTable.physicalType));
                sc.o("?");
                DValue realVal = this.sqlValueRenderer.preRenderSqlParam(field.dval, field.field.physicalPair.type, sqlStatement.typeHintL);
                sqlStatement.paramL.add(realVal);
                walker.addIfNotLast(sc, ", ");
            }
        }
        sc.o(");");

        sqlStatement.sql = sc.toString();
        return sqlStatement;
    }

    public SqlStatement render(LLD.LLBulkInsert statement) {
//        if (statement.subQueryInfo != null) {
//            return assocSqlGenerator.renderInsertSubQuery(statement);
//        }

        /* INSERT INTO table_name(column1, column2, …) VALUES (value1, value2, …); */
        //the fieldL is all the logical fields involved.
        //Because of ManyToMany relations, some of those fields may be in an assoc table, not in the statement's table
        List<LLD.LLFieldValue> fieldsToInsert = statement.first.fieldL.stream()
                .filter(fld -> !fld.field.isAssocField).collect(Collectors.toList());
        if (fieldsToInsert.isEmpty() && !statement.areFieldsToInsert()) {
            return null;
        }

        SqlStatement sqlStatement = new SqlStatement();
        StrCreator sc = new StrCreator();
        if (fieldsToInsert.isEmpty()) {
            sc.o("INSERT INTO %s  VALUES(DEFAULT", statement.getTableName());
        } else {
            sc.o("INSERT INTO %s (", statement.getTableName());
            ListWalker<LLD.LLFieldValue> walker = new ListWalker<>(fieldsToInsert);
            while (walker.hasNext()) {
                LLD.LLFieldValue field = walker.next();
                sc.o("%s", field.field.getFieldName());
                walker.addIfNotLast(sc, ", ");
            }
            sc.o(")");
            sc.nl();
            sc.o(" VALUES(");

            walker = new ListWalker<>(fieldsToInsert);
            while (walker.hasNext()) {
                LLD.LLFieldValue field = walker.next();
//            sc.o("%s", renderAsSql(field.dval, field.field.physicalPair.type, field.field.physicalTable.physicalType));
                sc.o("?");
                DValue realVal = this.sqlValueRenderer.preRenderSqlParam(field.dval, field.field.physicalPair.type, sqlStatement.typeHintL);
                sqlStatement.paramL.add(realVal);
                walker.addIfNotLast(sc, ", ");
            }
        }
        sc.o(")");
        //and the rest
        for (int i = 1; i < statement.insertStatements.size(); i++) {
            sc.o(", (");
            LLD.LLInsert stmt = statement.insertStatements.get(i);
            ListWalker<LLD.LLFieldValue> walker = new ListWalker<>(stmt.fieldL);
            while (walker.hasNext()) {
                LLD.LLFieldValue field = walker.next();
                sc.o("?");
                DValue realVal = this.sqlValueRenderer.preRenderSqlParam(field.dval, field.field.physicalPair.type, sqlStatement.typeHintL);
                sqlStatement.paramL.add(realVal);
                walker.addIfNotLast(sc, ", ");
            }
            sc.o(")");
        }
        sc.addStr(";");

        sqlStatement.sql = sc.toString();
        return sqlStatement;
    }

}
