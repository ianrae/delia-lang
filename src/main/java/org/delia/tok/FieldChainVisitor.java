package org.delia.tok;

import org.delia.compiler.ast.Exp;

import java.util.Stack;

public class FieldChainVisitor implements Exp.ExpVisitor {
    public static final String SCALAR_FIELD = "$__scalar__";

    public static final String IS_DEFERRED_WHERE_CLAUSE = "$$WHERE$$";

    public Exp.ExpBase top;
    public Exp.ExpBase prev;
    private int argCountdown = -1;
    public Stack<Tok.FieldTok> fieldStack = new Stack<>();
    public Tok.DToken oneVal; //val or null
    public Tok.ListTok listTok;
    private boolean isWhereClause = false;

    public FieldChainVisitor() {
    }

    public FieldChainVisitor(boolean isWhereClause) {
        this.isWhereClause = isWhereClause;
    }

    @Override
    public void visit(Exp.ExpBase exp) {
        if (argCountdown >= 0) {
            argCountdown--;
        }

        if (exp instanceof Exp.DottedExp) {
            Exp.DottedExp dexp = (Exp.DottedExp) exp;
            if (dexp == top) {
            }
        } else if (exp instanceof Exp.FunctionExp) {
            Exp.FunctionExp fexp = (Exp.FunctionExp) exp;
            argCountdown = fexp.argsL.size();
            String fieldName = fexp.prefix;
            if (fieldName == null) {
                fieldName = SCALAR_FIELD;
            } else {
                String[] ar = fexp.prefix.split("\\.");
                for (int i = 0; i < ar.length - 1; i++) {
                    Tok.FieldTok field = new Tok.FieldTok(ar[i]);
                    fieldStack.push(field);
                }
                fieldName = ar[ar.length - 1];
            }

            Tok.FieldTok field = new Tok.FieldTok(fieldName);
            Tok.FunctionTok func = new Tok.FunctionTok(fexp.fnName);
            func.negFlag = fexp.negFlag;
            field.funcL.add(func);

            fieldStack.push(field);
        } else if (exp instanceof Exp.ListExp) {
            Exp.ListExp vexp = (Exp.ListExp) exp;
            listTok = new Tok.ListTok();
        } else if (exp instanceof Exp.ValueExp) {
            Exp.ValueExp vexp = (Exp.ValueExp) exp;
            Tok.ValueTok valtok = new Tok.ValueTok();
            valtok.value = vexp.value;
            addToFuncOrOneValue(valtok);
        } else if (exp instanceof Exp.NullExp) {
            Exp.NullExp nexp = (Exp.NullExp) exp;
            Tok.NullTok ntok = new Tok.NullTok();
            addToFuncOrOneValue(ntok);
        } else if (exp instanceof Exp.FieldExp) {
            Exp.FieldExp fexp = (Exp.FieldExp) exp;
            Tok.FieldTok field = new Tok.FieldTok(fexp.fieldName);
            addToFuncOrOneValue(field);
        }
        prev = exp;
    }

    private void addToFuncOrOneValue(Tok.DToken tok) {
        if (tok instanceof Tok.FieldTok && argCountdown < 0) {
            Tok.FieldTok field = (Tok.FieldTok)tok;
            fieldStack.push(field);
            return;
        }

        if (!fieldStack.isEmpty()) {
            if (isWhereClause) {
                Tok.FieldTok field = fieldStack.peek();
                Tok.FunctionTok func;
                if (field.funcL.isEmpty()) {
                    func = new Tok.FunctionTok(IS_DEFERRED_WHERE_CLAUSE);
                    field.funcL.add(func);
                } else {
                    func = field.funcL.get(0);
                }
                func.argsL.add(tok);
            } else {
                Tok.FieldTok field = fieldStack.peek();
                Tok.FunctionTok func = field.funcL.get(field.funcL.size() - 1);
                func.argsL.add(tok);
            }
        } else if (listTok != null) {
            listTok.listL.add(tok);
        } else {
            oneVal = tok;
        }
    }
}
