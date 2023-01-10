package org.delia.tok;

import org.delia.compiler.ast.Exp;
import org.delia.type.DValue;
import org.delia.valuebuilder.ScalarValueBuilder;

import java.util.List;

public class TokWhereClauseUtils {

    public static Tok.WhereTok buildTrueWhereClause(ScalarValueBuilder scalarBuilder) {
        Tok.ValueTok vexp = new Tok.ValueTok();
        vexp.value = scalarBuilder.buildBoolean(true);
        Tok.DottedTok dexp = new Tok.DottedTok(vexp);
        Tok.WhereTok whereClause = new Tok.WhereTok(dexp);
        return whereClause;
    }

    public static Tok.WhereTok buildPKWhereClause(ScalarValueBuilder scalarBuilder, String pkStr) {
        Tok.ValueTok vexp = new Tok.ValueTok();
        vexp.value = scalarBuilder.buildInt(pkStr);  //TODO: support string,long,etc
        Tok.DottedTok dexp = new Tok.DottedTok(vexp);
        Tok.WhereTok whereClause = new Tok.WhereTok(dexp);
        return whereClause;
    }
    public static Tok.WhereTok buildPKWhereClause(DValue pkval) {
        Tok.ValueTok vexp = new Tok.ValueTok();
        vexp.value = pkval;
        Tok.DottedTok dexp = new Tok.DottedTok(vexp);
        Tok.WhereTok whereClause = new Tok.WhereTok(dexp);
        return whereClause;
    }
    public static Tok.WhereTok buildEqWhereClause(String fieldName, DValue dvalTarget, Exp.JoinInfo joinInfo) {
        Tok.ValueTok vexp = new Tok.ValueTok();
        vexp.value = dvalTarget;
        Tok.DottedTok d1 = new Tok.DottedTok(vexp);

        Tok.FieldTok f1 = new Tok.FieldTok(fieldName);
        f1.joinInfo = joinInfo;
        Tok.DottedTok d2 = new Tok.DottedTok(f1);

        Tok.OperatorTok opexp = new Tok.OperatorTok();
        opexp.op1 = d2;
        opexp.op2 = d1;
        opexp.op = "==";

        Tok.WhereTok whereClause = new Tok.WhereTok(opexp);
        return whereClause;
    }

    public static DValue extractPKWhereClause(Tok.WhereTok whereClause) {
        if (whereClause.where instanceof Tok.DottedTok) {
            Tok.DottedTok dexp = (Tok.DottedTok) whereClause.where;
            if (dexp.chainL.size() == 1) {
                Tok.DToken el = dexp.chainL.get(0);
                if (el instanceof Tok.ValueTok) {
                    Tok.ValueTok vexp = (Tok.ValueTok) el;
                    return vexp.value;
                }
            }
        }
        return null;
    }

    public static Tok.WhereTok buildInWhereClause(String fieldName, List<DValue> dvalTargets, Exp.JoinInfo joinInfo) {
        Tok.DottedTok d1 = new Tok.DottedTok();
        for(DValue dval: dvalTargets) {
            Tok.ValueTok vexp = new Tok.ValueTok();
            vexp.value = dval;
            d1.chainL.add(vexp);
        }

        Tok.FieldTok f1 = new Tok.FieldTok(fieldName);
        f1.joinInfo = joinInfo;
        Tok.DottedTok d2 = new Tok.DottedTok(f1);

        Tok.OperatorTok opexp = new Tok.OperatorTok();
        opexp.op1 = d2;
        opexp.op2 = d1;
        opexp.op = "in";

        Tok.WhereTok whereClause = new Tok.WhereTok(opexp);
        return whereClause;
    }
}
