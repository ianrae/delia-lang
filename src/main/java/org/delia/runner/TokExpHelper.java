package org.delia.runner;

import org.delia.compiler.ast.Exp;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.hld.DeliaExecutable;
import org.delia.hld.HLD;
import org.delia.log.DeliaLog;
import org.delia.tok.Tok;
import org.delia.type.DValue;
import org.delia.valuebuilder.ScalarValueBuilder;

public class TokExpHelper extends ServiceBase {


    private final SimpleDValueBuilder dvalBuilder;

    public TokExpHelper(FactoryService factorySvc) {
        super(factorySvc);
        this.dvalBuilder = new SimpleDValueBuilder(factorySvc);
    }


//    public AST.InsertFieldStatementAst buildInsertField(String fieldName, String s) {
//        AST.InsertFieldStatementAst field = new AST.InsertFieldStatementAst();
//        field.fieldName = fieldName;
//        Tok.ValueTok valueExp = new Tok.ValueTok();
//        valueExp.value = buildDValueString(s);
//        field.valueExp = valueExp;
//        return field;
//    }
//    public AST.InsertFieldStatementAst buildInsertFieldInt(String fieldName, Integer nval) {
//        AST.InsertFieldStatementAst field = new AST.InsertFieldStatementAst();
//        field.fieldName = fieldName;
//        Tok.ValueTok valueExp = new Tok.ValueTok();
//        valueExp.value = dvalBuilder.buildDValueInt(nval.toString());
//        field.valueExp = valueExp;
//        return field;
//    }
    public DValue buildDValueString(String s) {
        return dvalBuilder.buildDValueString(s);
    }

    public static Tok.WhereTok buildPKWhereClause(ScalarValueBuilder scalarBuilder, String pkStr) {
        Tok.ValueTok vexp = new Tok.ValueTok();
        vexp.value = scalarBuilder.buildInt(pkStr);
        Tok.DottedTok dexp = new Tok.DottedTok(vexp);
        Tok.WhereTok whereClause = new Tok.WhereTok(dexp);
        return whereClause;
    }
    public static Tok.WhereTok buildTrueWhereClause(ScalarValueBuilder scalarBuilder) {
        Tok.ValueTok vexp = new Tok.ValueTok();
        vexp.value = scalarBuilder.buildBoolean(true);
        Tok.DottedTok dexp = new Tok.DottedTok(vexp);
        Tok.WhereTok whereClause = new Tok.WhereTok(dexp);
        return whereClause;
    }
    public static Tok.WhereTok buildLTWhereClause(ScalarValueBuilder scalarBuilder, String typeName, String fieldName, String valStr, Exp.JoinInfo joinInfo) {
        Tok.ValueTok vexp = new Tok.ValueTok();
        vexp.value = scalarBuilder.buildInt(valStr);
        Tok.DottedTok d1 = new Tok.DottedTok(vexp);

        Tok.FieldTok f1 = new Tok.FieldTok(fieldName);
        f1.joinInfo = joinInfo;
        Tok.DottedTok d2 = new Tok.DottedTok(f1);

        Tok.OperatorTok opexp = new Tok.OperatorTok();
        opexp.op1 = d2;
        opexp.op2 = d1;
        opexp.op = "<";

        Tok.WhereTok whereClause = new Tok.WhereTok(opexp);
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
