package org.delia.lld.processor;

import org.delia.compiler.ast.Exp;
import org.delia.type.DValue;
import org.delia.validation.ValidationRunner;
import org.delia.valuebuilder.ScalarValueBuilder;

public class WhereClauseUtils {

    public static Exp.WhereClause buildTrueWhereClause(ScalarValueBuilder scalarBuilder) {
        Exp.ValueExp vexp = new Exp.ValueExp();
        vexp.value = scalarBuilder.buildBoolean(true);
        Exp.DottedExp dexp = new Exp.DottedExp(vexp);
        Exp.WhereClause whereClause = new Exp.WhereClause(dexp);
        return whereClause;
    }

    public static Exp.WhereClause buildPKWhereClause(ScalarValueBuilder scalarBuilder, String pkStr) {
        Exp.ValueExp vexp = new Exp.ValueExp();
        vexp.value = scalarBuilder.buildInt(pkStr);  //TODO: support string,long,etc
        Exp.DottedExp dexp = new Exp.DottedExp(vexp);
        Exp.WhereClause whereClause = new Exp.WhereClause(dexp);
        return whereClause;
    }
    public static Exp.WhereClause buildPKWhereClause(ScalarValueBuilder scalarBuilder, DValue pkval) {
        Exp.ValueExp vexp = new Exp.ValueExp();
        vexp.value = pkval;
        Exp.DottedExp dexp = new Exp.DottedExp(vexp);
        Exp.WhereClause whereClause = new Exp.WhereClause(dexp);
        return whereClause;
    }
    public static Exp.WhereClause buildEqWhereClause(ScalarValueBuilder scalarBuilder, String typeName, String fieldName, DValue dvalTarget, Exp.JoinInfo joinInfo) {
        Exp.ValueExp vexp = new Exp.ValueExp();
        vexp.value = dvalTarget;
        Exp.DottedExp d1 = new Exp.DottedExp(vexp);

        Exp.FieldExp f1 = new Exp.FieldExp(fieldName, joinInfo);
        Exp.DottedExp d2 = new Exp.DottedExp(f1);

        Exp.OperatorExp opexp = new Exp.OperatorExp();
        opexp.op1 = d2;
        opexp.op2 = d1;
        opexp.op = "==";

        Exp.WhereClause whereClause = new Exp.WhereClause(opexp);
        return whereClause;
    }

    public static DValue extractPKWhereClause(Exp.WhereClause whereClause) {
        if (whereClause.where instanceof Exp.DottedExp) {
            Exp.DottedExp dexp = (Exp.DottedExp) whereClause.where;
            if (dexp.chainL.size() == 1) {
                Exp.ElementExp el = dexp.chainL.get(0);
                if (el instanceof Exp.ValueExp) {
                    Exp.ValueExp vexp = (Exp.ValueExp) el;
                    return vexp.value;
                }
            }
        }
        return null;
    }
}
