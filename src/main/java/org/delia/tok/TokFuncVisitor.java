package org.delia.tok;

import java.util.ArrayList;
import java.util.List;

public class TokFuncVisitor implements Tok.TokVisitor {

    public List<Tok.FunctionTok> allFuncs = new ArrayList<>();

    @Override
    public void visit(Tok.TokBase exp, Tok.TokBase parent) {
        if (exp instanceof Tok.FunctionTok) {
            Tok.FunctionTok fexp = (Tok.FunctionTok) exp;
            allFuncs.add(fexp);
        }

    }
}
