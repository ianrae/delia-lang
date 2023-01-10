package org.delia.tok;

public class TokVisitorUtils {

    public static String getPossibleFieldName(Tok.OperandTok exp) {
        if (exp instanceof Tok.FieldTok) {
            Tok.FieldTok fexp = (Tok.FieldTok) exp;
            return fexp.fieldName;
        } else if (exp instanceof Tok.DottedTok) {
            String fieldName = getSingleChainField(exp);
            return fieldName;
        }
        return null;
    }

    public static Tok.FieldTok getPossibleFieldExp(Tok.OperandTok exp) {
        if (exp instanceof Tok.FieldTok) {
            Tok.FieldTok fexp = (Tok.FieldTok) exp;
            return fexp;
        } else if (exp instanceof Tok.DottedTok) {
            return getSingleChainFieldExp(exp);
        }
        return null;
    }

    public static String getSingleChainField(Tok.OperandTok exp) {
        Tok.FieldTok fexp = getSingleChainFieldExp(exp);
        return fexp == null ? null : fexp.fieldName;
    }

    public static Tok.FieldTok getSingleChainFieldExp(Tok.OperandTok exp) {
        if (exp instanceof Tok.DottedTok) {
            Tok.DottedTok dexp = (Tok.DottedTok) exp;
            if (dexp.chainL.size() == 1) {
                Tok.DToken inner = dexp.chainL.get(0);
                if (inner instanceof Tok.FieldTok) {
                    Tok.FieldTok fexp = (Tok.FieldTok) inner;
                    return fexp;
                }
            } //TODO support chain > 1 long
        }
        return null;
    }

    public static Tok.ValueTok getSingleChainValue(Tok.OperandTok exp) {
        if (exp instanceof Tok.DottedTok) {
            Tok.DottedTok dexp = (Tok.DottedTok) exp;
            if (dexp.chainL.size() == 1) {
                Tok.DToken inner = dexp.chainL.get(0);
                if (inner instanceof Tok.ValueTok) {
                    Tok.ValueTok fexp = (Tok.ValueTok) inner;
                    return fexp;
                }
            } //TODO support chain > 1 long
        }
        return null;
    }

    public static boolean isWhereAll(Tok.OperandTok exp) {
        Tok.ValueTok singleWhereVal = TokVisitorUtils.getSingleChainValue(exp);
        if (singleWhereVal != null && singleWhereVal.strValue().equals("true")) {
            return true;
        }
        return false;
    }

    public static boolean isWherePK(Tok.OperandTok exp) {
        return exp instanceof Tok.PKWhereTok;
    }
    public static Tok.PKWhereTok getIfWherePK(Tok.OperandTok exp) {
        if (exp instanceof Tok.PKWhereTok) {
            return (Tok.PKWhereTok) exp;
        }
        return null;
    }

    public static int getDottedSize(Tok.OperandTok exp) {
        if (exp instanceof Tok.DottedTok) {
            Tok.DottedTok dexp = (Tok.DottedTok) exp;
            int n = 0;
            for (Tok.DToken inner : dexp.chainL) {
                if (inner instanceof Tok.ListTok) {
                    Tok.ListTok lexp = (Tok.ListTok) inner;
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
