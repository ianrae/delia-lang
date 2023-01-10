package org.delia.sql;

import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.SqlStatement;
import org.delia.hld.dat.DatService;
import org.delia.lld.LLD;
import org.delia.type.DValue;
import org.delia.util.ListWalker;
import org.delia.util.StrCreator;
import org.delia.valuebuilder.ScalarValueBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class UpsertSqlGenerator extends ServiceBase {

    private final SqlValueRenderer sqlValueRenderer;
    private final ScalarValueBuilder valueBuilder;
    private final DatService datSvc;

    public UpsertSqlGenerator(FactoryService factorySvc, SqlValueRenderer sqlValueRenderer, ScalarValueBuilder valueBuilder, DatService datSvc) {
        super(factorySvc);
        this.sqlValueRenderer = sqlValueRenderer;
        this.valueBuilder = valueBuilder;
        this.datSvc = datSvc;
    }

    public SqlStatement renderInsertSubQuery(LLD.LLUpsert statement) {
        /*
        insert into Customer ...values...
        on conflict (id)  //pk
         do nothing        //this is updateOnly. insert if not there, else do nothing
           do update set .... where
           -can use EXCLUDED.someField to use the field values from the insert part
         */

        /* INSERT INTO table_name(column1, column2, …) VALUES (value1, value2, …); */
        //the fieldL is all the logical fields involved.
        //Because of ManyToMany relations, some of those fields may be in an assoc table, not in the statement's table
        List<LLD.LLFieldValue> fieldsToUpdate = statement.fieldL.stream()
                .filter(fld -> !fld.field.isAssocField).collect(Collectors.toList());
//        if (fieldsToUpdate.isEmpty()) {
//            return null;
//        }

        SqlStatement sqlStatement = new SqlStatement();
        StrCreator sc = new StrCreator();
        sc.o("INSERT INTO %s (", statement.getTableName());
        List<LLD.LLFieldValue> fullList = new ArrayList<>();
        fullList.add(statement.pkField);
        fullList.addAll(fieldsToUpdate);
//        DValue tmpVal = this.sqlValueRenderer.renderSqlParam(statement.pkField.dval, statement.pkField.field.physicalPair.type, valueBuilder);
//        sqlStatement.paramL.add(tmpVal);

        ListWalker<LLD.LLFieldValue> walker = new ListWalker<>(fullList);
        while (walker.hasNext()) {
            LLD.LLFieldValue field = walker.next();
            sc.o("%s", field.field.getFieldName());
            walker.addIfNotLast(sc, ", ");
        }
        sc.o(")");
        sc.nl();
        sc.o(" VALUES(");

        walker = new ListWalker<>(fullList);
        while (walker.hasNext()) {
            LLD.LLFieldValue field = walker.next();
            sc.o("?");
            DValue realVal = this.sqlValueRenderer.renderSqlParam(field.dval, field.field.physicalPair.type, valueBuilder);
            sqlStatement.paramL.add(realVal);
            walker.addIfNotLast(sc, ", ");
        }
        sc.o(")");

        sc.o(" ON CONFLICT (%s)", statement.pkField.field.getFieldName());
        if (statement.noUpdateFlag || fieldsToUpdate.isEmpty()) {
            sc.o(" DO NOTHING");
        } else {
            sc.o(" DO UPDATE SET ");
            walker = new ListWalker<>(fieldsToUpdate);
            while (walker.hasNext()) {
                LLD.LLFieldValue field = walker.next();
                String fieldName = field.field.getFieldName();
                sc.o("%s=EXCLUDED.%s", fieldName, fieldName);
                walker.addIfNotLast(sc, ", ");
            }
            sc.nl();
        }

        sqlStatement.sql = sc.toString();
        return sqlStatement;
    }
}
