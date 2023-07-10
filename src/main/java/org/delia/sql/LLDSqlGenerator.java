package org.delia.sql;

import org.delia.DeliaOptions;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.SqlStatement;
import org.delia.hld.dat.DatService;
import org.delia.lld.LLD;
import org.delia.tok.Tok;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.util.DValueHelper;
import org.delia.util.ListWalker;
import org.delia.util.StrCreator;
import org.delia.valuebuilder.ScalarValueBuilder;
import org.delia.varevaluator.VarEvaluator;

import java.util.List;
import java.util.stream.Collectors;

public class LLDSqlGenerator extends ServiceBase implements LLD.LLStatementRenderer {

    private final SqlValueRenderer sqlValueRenderer;
    private final DeliaOptions deliaOptions;
    private final ScalarValueBuilder valueBuilder;
    private final AssocSqlGenerator assocSqlGenerator;
    private final DatService datSvc;
    private final UpsertSqlGenerator upsertSqlGenerator;
    private final CreateTableSqlGenerator createTableSqlGenerator;
    private final SqlTypeConverter sqlTypeConverter;
    private final LetSqlGenerator letSqlGenerator;
    private final CreateAssocTableSqlGenerator createAssocTableSqlGenerator;
    private final LLDInsertGenerator insertGenerator;
    private final VarEvaluator varEvaluator;

    public LLDSqlGenerator(FactoryService factorySvc, DeliaOptions deliaOptions, DTypeRegistry registry, DatService datSvc, VarEvaluator varEvaluator) {
        super(factorySvc);
        this.sqlValueRenderer = new SqlValueRenderer(factorySvc, varEvaluator);
        this.valueBuilder = new ScalarValueBuilder(factorySvc, registry);
        this.deliaOptions = deliaOptions;
        this.assocSqlGenerator = new AssocSqlGenerator(factorySvc, sqlValueRenderer, valueBuilder, datSvc);
        this.upsertSqlGenerator = new UpsertSqlGenerator(factorySvc, sqlValueRenderer, valueBuilder, datSvc);
        this.createTableSqlGenerator = new CreateTableSqlGenerator(factorySvc, sqlValueRenderer, valueBuilder, datSvc, deliaOptions);
        this.createAssocTableSqlGenerator = new CreateAssocTableSqlGenerator(factorySvc, sqlValueRenderer, valueBuilder, datSvc, deliaOptions);
        this.letSqlGenerator = new LetSqlGenerator(factorySvc, sqlValueRenderer, valueBuilder, datSvc, deliaOptions);
        this.datSvc = datSvc;
        this.sqlTypeConverter = new SqlTypeConverter(deliaOptions);
        this.insertGenerator = new LLDInsertGenerator(factorySvc, deliaOptions, registry, datSvc, varEvaluator);
        this.varEvaluator = varEvaluator;
    }

    public SqlStatement generateSql(LLD.LLStatement statement) {
        return statement.render(this);
    }

    @Override
    public SqlStatement render(LLD.LLSelect statement) {
        return letSqlGenerator.render(statement);
    }


    @Override
    public SqlStatement render(LLD.LLDelete statement) {
        StrCreator sc = new StrCreator();
        sc.o("DELETE ");
        sc.o(" FROM %s", statement.table.getSQLName());

        SqlStatement sqlStatement = new SqlStatement();
        if (statement.whereTok != null) {
//            MyWhereSqlVisitor visitor = new MyWhereSqlVisitor(factorySvc);
            NewWhereSqlVisitor visitor = new NewWhereSqlVisitor(factorySvc);
            //visitor.sc = new StrCreator(); //local one
            visitor.top = statement.whereTok.where;
            visitor.pkpair = DValueHelper.findPrimaryKeyFieldPair(statement.table.physicalType);
            visitor.mainAlias = statement.table.alias;
//            visitor.statement = statement;
            statement.whereTok.visit(visitor, null);
//            String str = visitor.sc.toString();
            String str = visitor.render();
            if (!str.isEmpty()) {
                sc.addStr(" WHERE ");
                sc.addStr(str);
            }

            SqlParamVisitor visitor2 = new SqlParamVisitor();
            visitor2.top = statement.whereTok.where;
            visitor2.pkpair = DValueHelper.findPrimaryKeyFieldPair(statement.table.physicalType);
            statement.whereTok.visit(visitor2, null);
            int i = 0;
            for (DValue dval : visitor2.sqlParams) {
                Tok.ValueTok vexp = visitor2.fieldValues.get(i++);
                DValue realVal = sqlValueRenderer.noRenderSqlParam(dval, vexp.hintPair == null ? null : vexp.hintPair.type, sqlStatement.typeHintL);
                sqlStatement.paramL.add(realVal);
            }
        }

        sqlStatement.sql = sc.toString();
        return sqlStatement;
    }

    @Override
    public SqlStatement render(LLD.LLUpdate statement) {
        StrCreator sc = new StrCreator();
        sc.o("UPDATE ");
        sc.o(" %s", statement.table.getSQLName());

        SqlStatement sqlStatement = new SqlStatement();
        sc.o(" SET ");
        //the fieldL is all the logical fields involved.
        //Because of ManyToMany relations, some of those fields may be in an assoc table, not in the statement's table
        List<LLD.LLFieldValue> fieldsToInsert = statement.fieldL.stream()
                .filter(fld -> !fld.field.isAssocField).collect(Collectors.toList());
        if (fieldsToInsert.isEmpty()) {
            return null;
        }

        ListWalker<LLD.LLFieldValue> walker = new ListWalker<>(fieldsToInsert);
        while (walker.hasNext()) {
            LLD.LLFieldValue field = walker.next();
            sc.o("%s=", field.field.getFieldName());
            sc.o("?");
            //TODO: what about field.dvallist??
            DValue realVal = this.sqlValueRenderer.noRenderSqlParam(field.dval, field.field.physicalPair.type, sqlStatement.typeHintL);
            sqlStatement.paramL.add(realVal);
            walker.addIfNotLast(sc, ", ");
        }
        sc.nl();

        if (statement.whereTok != null) {
//            MyWhereSqlVisitor visitor = new MyWhereSqlVisitor(factorySvc);
            NewWhereSqlVisitor visitor = new NewWhereSqlVisitor(factorySvc);
            //visitor.sc = new StrCreator(); //local one
            visitor.top = statement.whereTok.where;
            visitor.pkpair = DValueHelper.findPrimaryKeyFieldPair(statement.table.physicalType);
            visitor.mainAlias = statement.table.alias;
//            visitor.statement = statement;
            statement.whereTok.visit(visitor, null);
//            String str = visitor.sc.toString();
            String str = visitor.render();
            if (!str.isEmpty()) {
                sc.addStr(" WHERE ");
                sc.addStr(str);
            }


            SqlParamVisitor visitor2 = new SqlParamVisitor();
            visitor2.top = statement.whereTok.where;
            visitor2.pkpair = DValueHelper.findPrimaryKeyFieldPair(statement.table.physicalType);
            statement.whereTok.visit(visitor2, null);
            int i = 0;
            for (DValue dval : visitor2.sqlParams) {
                Tok.ValueTok vexp = visitor2.fieldValues.get(i++);
                DValue realVal = sqlValueRenderer.noRenderSqlParam(dval, vexp.hintPair == null ? null : vexp.hintPair.type, sqlStatement.typeHintL);
                sqlStatement.paramL.add(realVal);
            }
        }

        sqlStatement.sql = sc.toString();
        return sqlStatement;
    }

    @Override
    public SqlStatement render(LLD.LLUpsert statement) {
        return upsertSqlGenerator.renderInsertSubQuery(statement);
    }

    @Override
    public SqlStatement render(LLD.LLInsert statement) {
        return insertGenerator.render(statement);
    }

    @Override
    public SqlStatement render(LLD.LLBulkInsert statement) {
        return insertGenerator.render(statement);
    }

    @Override
    public SqlStatement render(LLD.LLCreateSchema statement) {
        if (!shouldGenerateDDLSql()) {
            return null;
        }

        if (statement.schema == null) return null;

        //CREATE SCHEMA [IF NOT EXISTS] schema_name;
        StrCreator sc = new StrCreator();
        sc.o("CREATE SCHEMA IF NOT EXISTS %s", statement.schema);
        SqlStatement sqlStatement = new SqlStatement();
        sqlStatement.sql = sc.toString();
        return sqlStatement;
    }

    //only create schemas and tables if not migrationAction.NONE
    private boolean shouldGenerateDDLSql() {
        switch (deliaOptions.migrationAction) {
            case NONE:
                return false;
            case GENERATE:
            case AUTO_MIGRATE:
            default:
                return true;
        }
    }

    @Override
    public SqlStatement render(LLD.LLCreateTable statement) {
        if (!shouldGenerateDDLSql()) {
            return null;
        }
        return createTableSqlGenerator.render(statement);
    }

    @Override
    public SqlStatement render(LLD.LLCreateAssocTable statement) {
        if (!shouldGenerateDDLSql()) {
            return null;
        }
        return createAssocTableSqlGenerator.render(statement);
    }

    private String getSqlType(DType dtype) {
        return sqlTypeConverter.getSqlType(dtype);
    }
}
