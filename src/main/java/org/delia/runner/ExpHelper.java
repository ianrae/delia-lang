package org.delia.runner;

import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.hld.DeliaExecutable;
import org.delia.compiler.ast.AST;
import org.delia.compiler.ast.Exp;
import org.delia.hld.HLD;
import org.delia.log.DeliaLog;
import org.delia.type.DValue;
import org.delia.valuebuilder.ScalarValueBuilder;

public class ExpHelper extends ServiceBase {


    private final SimpleDValueBuilder dvalBuilder;

    public ExpHelper(FactoryService factorySvc) {
        super(factorySvc);
        this.dvalBuilder = new SimpleDValueBuilder(factorySvc);
    }


    public AST.InsertFieldStatementAst buildInsertField(String fieldName, String s) {
        AST.InsertFieldStatementAst field = new AST.InsertFieldStatementAst();
        field.fieldName = fieldName;
        Exp.ValueExp valueExp = new Exp.ValueExp();
        valueExp.value = buildDValueString(s);
        field.valueExp = valueExp;
        return field;
    }
    public AST.InsertFieldStatementAst buildInsertFieldInt(String fieldName, Integer nval) {
        AST.InsertFieldStatementAst field = new AST.InsertFieldStatementAst();
        field.fieldName = fieldName;
        Exp.ValueExp valueExp = new Exp.ValueExp();
        valueExp.value = dvalBuilder.buildDValueInt(nval.toString());
        field.valueExp = valueExp;
        return field;
    }
    public DValue buildDValueString(String s) {
        return dvalBuilder.buildDValueString(s);
    }

    public static Exp.WhereClause buildPKWhereClause(ScalarValueBuilder scalarBuilder, String pkStr) {
        Exp.ValueExp vexp = new Exp.ValueExp();
        vexp.value = scalarBuilder.buildInt(pkStr);
        Exp.DottedExp dexp = new Exp.DottedExp(vexp);
        Exp.WhereClause whereClause = new Exp.WhereClause(dexp);
        return whereClause;
    }
    public static Exp.WhereClause buildTrueWhereClause(ScalarValueBuilder scalarBuilder) {
        Exp.ValueExp vexp = new Exp.ValueExp();
        vexp.value = scalarBuilder.buildBoolean(true);
        Exp.DottedExp dexp = new Exp.DottedExp(vexp);
        Exp.WhereClause whereClause = new Exp.WhereClause(dexp);
        return whereClause;
    }
    public static Exp.WhereClause buildLTWhereClause(ScalarValueBuilder scalarBuilder, String typeName, String fieldName, String valStr, Exp.JoinInfo joinInfo) {
        Exp.ValueExp vexp = new Exp.ValueExp();
        vexp.value = scalarBuilder.buildInt(valStr);
        Exp.DottedExp d1 = new Exp.DottedExp(vexp);

        Exp.FieldExp f1 = new Exp.FieldExp(fieldName, joinInfo);
        Exp.DottedExp d2 = new Exp.DottedExp(f1);

        Exp.OperatorExp opexp = new Exp.OperatorExp();
        opexp.op1 = d2;
        opexp.op2 = d1;
        opexp.op = "<";

        Exp.WhereClause whereClause = new Exp.WhereClause(opexp);
        return whereClause;
    }

    public static void dumpExec(DeliaExecutable executable, DeliaLog log) {
        log.log("--exec-dump--");
        for (HLD.HLDStatement hld : executable.hldStatements) {
            log.log(hld.toString());
        }
        log.log("--exec-dump end.--");
    }


}
