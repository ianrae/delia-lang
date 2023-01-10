package org.delia.tok;

import java.util.ArrayList;
import java.util.List;

public class TokValueVisitor implements Tok.TokVisitor {

    public List<Tok.ValueTok> allValues = new ArrayList<>();

    @Override
    public void visit(Tok.TokBase exp, Tok.TokBase parent) {
        if (exp instanceof Tok.ValueTok) {
            Tok.ValueTok fexp = (Tok.ValueTok) exp;
            allValues.add(fexp);
        }

    }
}
