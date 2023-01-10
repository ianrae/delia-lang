package org.delia.dbimpl.mem;

import org.delia.tok.Tok;

public class AliasRemoverTokVisitor implements Tok.TokVisitor {

    @Override
    public void visit(Tok.TokBase exp, Tok.TokBase parent) {
        if (exp instanceof Tok.FieldTok) {
            Tok.FieldTok fexp = (Tok.FieldTok) exp;
            fexp.alias = null;
        }

    }
}
