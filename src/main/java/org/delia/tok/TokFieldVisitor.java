package org.delia.tok;

import java.util.ArrayList;
import java.util.List;

public class TokFieldVisitor implements Tok.TokVisitor {
    public boolean onlyJoinFields;

    public List<Tok.FieldTok> allFields = new ArrayList<>();

    @Override
    public void visit(Tok.TokBase exp, Tok.TokBase parent) {
        if (exp instanceof Tok.FieldTok) {
            Tok.FieldTok fexp = (Tok.FieldTok) exp;
            if (onlyJoinFields) {
                if (fexp.joinInfo != null) {
                    this.allFields.add(fexp);
                }
            } else {
                this.allFields.add(fexp);
            }
        }

    }
}
