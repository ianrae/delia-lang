package org.delia.dbimpl;

import org.delia.compiler.ast.AST;
import org.delia.compiler.ast.Exp;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.hld.DeliaExecutable;
import org.delia.hld.HLD;
import org.delia.log.DeliaLog;
import org.delia.runner.ExpHelper;
import org.delia.runner.SimpleDValueBuilder;
import org.delia.tok.Tok;
import org.delia.valuebuilder.ScalarValueBuilder;

import java.util.Arrays;

public class ExpTestHelper extends ServiceBase {

    private final SimpleDValueBuilder dvalBuilder;
    private final ExpHelper helper;

    public ExpTestHelper(FactoryService factorySvc) {
        super(factorySvc);
        this.dvalBuilder = new SimpleDValueBuilder(factorySvc);
        this.helper = new ExpHelper(factorySvc);
    }

    public Tok.OperatorTok buildExp1(ScalarValueBuilder scalarBuilder) {
        Tok.ValueTok v1 = new Tok.ValueTok();
        v1.value = scalarBuilder.buildInt("5");

        Tok.FieldTok f1 = new Tok.FieldTok("x");

        Tok.DottedTok d1 = new Tok.DottedTok(v1);
        Tok.DottedTok d2 = new Tok.DottedTok(f1);

        Tok.OperatorTok opexp = new Tok.OperatorTok();
        opexp.op1 = d1;
        opexp.op2 = d2;
        opexp.op = "<";

        return opexp;
    }

    public AST.DeliaScript buildScript(ScalarValueBuilder scalarBuilder) {
        AST.DeliaScript script = buildScriptStart(scalarBuilder);

        AST.LetStatementAst letStmt = new AST.LetStatementAst();
        letStmt.typeName = "Person";
        letStmt.whereClause = buildPKWhereClause(scalarBuilder, "10"); //"10";
        script.add(letStmt);

        return script;
    }

    public AST.DeliaScript buildScriptStart(ScalarValueBuilder scalarBuilder) {
        return buildScriptStart(true);
    }

    public AST.DeliaScript buildScriptStart(boolean withSchema) {
        AST.DeliaScript script = new AST.DeliaScript();
        if (withSchema) {
            script.add(new AST.SchemaAst("alpha"));
        }

        AST.TypeAst type = new AST.TypeAst("Person");
        type.baseName = "struct";
        AST.TypeFieldAst field = new AST.TypeFieldAst("id");
        field.isPrimaryKey = true;
        field.typeName = "int";
        type.fields.add(field);
        field = new AST.TypeFieldAst("firstName");
        field.isOptional = true;
        field.typeName = "string";
        type.fields.add(field);
        script.add(type);

        AST.InsertStatementAst ins = new AST.InsertStatementAst();
        ins.typeName = "Person";
        ins.fields = Arrays.asList(buildInsertFieldInt("id", 7), buildInsertField("firstName", "bob"));
        script.add(ins);

        return script;
    }

    public AST.DeliaScript buildScriptStartTwo(ScalarValueBuilder scalarBuilder) {
        AST.DeliaScript script = buildScriptStart(scalarBuilder);

        AST.InsertStatementAst ins = new AST.InsertStatementAst();
        ins.typeName = "Person";
        ins.fields = Arrays.asList(buildInsertFieldInt("id", 8), buildInsertField("firstName", "sue"));
        script.add(ins);

        return script;
    }

    public AST.InsertFieldStatementAst buildInsertField(String fieldName, String s) {
        return helper.buildInsertField(fieldName, s);
    }

    public AST.InsertFieldStatementAst buildInsertFieldInt(String fieldName, Integer nval) {
        return helper.buildInsertFieldInt(fieldName, nval);
    }

    public static Exp.WhereClause buildPKWhereClause(ScalarValueBuilder scalarBuilder, String pkStr) {
        return ExpHelper.buildPKWhereClause(scalarBuilder, pkStr);
    }

    public static Exp.WhereClause buildTrueWhereClause(ScalarValueBuilder scalarBuilder) {
        return ExpHelper.buildTrueWhereClause(scalarBuilder);
    }

    public static Exp.WhereClause buildLTWhereClause(ScalarValueBuilder scalarBuilder, String typeName, String fieldName, String valStr, Exp.JoinInfo joinInfo) {
        return ExpHelper.buildLTWhereClause(scalarBuilder, typeName, fieldName, valStr, joinInfo);
    }

    public static void dumpExec(DeliaExecutable executable, DeliaLog log) {
        log.log("--exec-dump--");
        for (HLD.HLDStatement hld : executable.hldStatements) {
            log.log(hld.toString());
        }
        log.log("--exec-dump end.--");
    }

}
