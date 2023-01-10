package org.delia.lld.processor;

import org.delia.compiler.ast.Exp;

public class VisitorUtils {

    public static String getPossibleFieldName(Exp.OperandExp exp) {
        if (exp instanceof Exp.FieldExp) {
            Exp.FieldExp fexp = (Exp.FieldExp) exp;
            return fexp.fieldName;
        } else if (exp instanceof Exp.DottedExp) {
            String fieldName = getSingleChainField(exp);
            return fieldName;
        }
        return null;
    }
    public static Exp.FieldExp getPossibleFieldExp(Exp.OperandExp exp) {
        if (exp instanceof Exp.FieldExp) {
            Exp.FieldExp fexp = (Exp.FieldExp) exp;
            return fexp;
        } else if (exp instanceof Exp.DottedExp) {
            return getSingleChainFieldExp(exp);
        }
        return null;
    }

    public static String getSingleChainField(Exp.OperandExp exp) {
        Exp.FieldExp fexp =  getSingleChainFieldExp(exp);
        return fexp == null ? null : fexp.fieldName;
    }
    public static Exp.FieldExp getSingleChainFieldExp(Exp.OperandExp exp) {
        if (exp instanceof Exp.DottedExp) {
            Exp.DottedExp dexp = (Exp.DottedExp) exp;
            if (dexp.chainL.size() == 1) {
                Exp.ElementExp inner = dexp.chainL.get(0);
                if (inner instanceof Exp.FieldExp) {
                    Exp.FieldExp fexp = (Exp.FieldExp) inner;
                    return fexp;
                }
            } //TODO support chain > 1 long
        }
        return null;
    }

    public static Exp.ValueExp getSingleChainValue(Exp.OperandExp exp) {
        if (exp instanceof Exp.DottedExp) {
            Exp.DottedExp dexp = (Exp.DottedExp) exp;
            if (dexp.chainL.size() == 1) {
                Exp.ElementExp inner = dexp.chainL.get(0);
                if (inner instanceof Exp.ValueExp) {
                    Exp.ValueExp fexp = (Exp.ValueExp) inner;
                    return fexp;
                }
            } //TODO support chain > 1 long
        }
        return null;
    }
    public static int getDottedSize(Exp.OperandExp exp) {
        if (exp instanceof Exp.DottedExp) {
            Exp.DottedExp dexp = (Exp.DottedExp) exp;
            int n = 0;
            for(Exp.ElementExp inner: dexp.chainL) {
                if (inner instanceof Exp.ListExp) {
                    Exp.ListExp lexp = (Exp.ListExp) inner;
                    n += lexp.listL.size();
                } else {
                    n++;
                }
            }
            return n;
        }
        return 0;
    }

}
