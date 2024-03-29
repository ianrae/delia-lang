package org.delia.hld;

import org.delia.tok.Tok;
import org.delia.type.DStructType;
import org.delia.type.TypePair;
import org.delia.util.DValueHelper;

public class HLDTokVisitor implements Tok.TokVisitor {
    private Tok.OperandTok top;
    private DStructType ownerType;
    public Tok.FieldTok deferredFieldTok;

    public HLDTokVisitor(Tok.OperandTok top, DStructType structType) {
        this.top = top;
        this.ownerType = structType;
    }

    @Override
    public void visit(Tok.TokBase exp, Tok.TokBase parent) {
        if (parent == null) {
            if (exp instanceof Tok.DottedTok) {
                Tok.DottedTok dexp = (Tok.DottedTok) exp;
                if (dexp.chainL.size() == 1 && dexp.chainL.get(0) instanceof Tok.FieldTok) {
                    Tok.FieldTok fieldTok = (Tok.FieldTok) dexp.chainL.get(0);
                    deferredFieldTok = fieldTok;
                }
            } else if (exp instanceof Tok.PKWhereTok) {
                Tok.PKWhereTok pkexp = (Tok.PKWhereTok) exp;
                pkexp.pkOwnerType = ownerType;
                TypePair pkpair = DValueHelper.findPrimaryKeyFieldPair(pkexp.pkOwnerType);
                pkexp.physicalFieldName = pkpair.name;
            }
        }
    }
}
