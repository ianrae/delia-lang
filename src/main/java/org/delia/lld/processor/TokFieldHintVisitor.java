package org.delia.lld.processor;

import org.delia.tok.Tok;
import org.delia.type.DStructType;
import org.delia.type.TypePair;
import org.delia.util.DValueHelper;
import org.delia.util.DeliaExceptionHelper;

import java.util.ArrayList;
import java.util.List;

public class TokFieldHintVisitor implements Tok.TokVisitor {

    private final boolean addHints;
    private DStructType structType;
    public List<Tok.FieldTok> allFields = new ArrayList<>();

    public TokFieldHintVisitor(DStructType structType, boolean addHints) {
        this.structType = structType;
        this.addHints = addHints;
    }

    @Override
    public void visit(Tok.TokBase exp, Tok.TokBase parent) {
        if (parent == null) {
            if (exp instanceof Tok.DottedTok) {
                Tok.DottedTok dexp = (Tok.DottedTok) exp;
                addFieldHints(dexp);
            } else if (exp instanceof Tok.OperatorTok) {
                Tok.OperatorTok oexp = (Tok.OperatorTok) exp;
                //do fields first, then values
                Tok.FieldTok lastField = null;
                if (oexp.op1 instanceof Tok.DottedTok) {
                    lastField = addFieldHints((Tok.DottedTok) oexp.op1);
                }
                if (oexp.op2 instanceof Tok.DottedTok) {
                    Tok.FieldTok tmp = addFieldHints((Tok.DottedTok) oexp.op2);
                    if (lastField == null) {
                        lastField = tmp;
                    }
                }

                //now values
                if (oexp.op1 instanceof Tok.DottedTok) {
                    addValueHints((Tok.DottedTok) oexp.op1, lastField);
                }
                if (oexp.op2 instanceof Tok.DottedTok) {
                    addValueHints((Tok.DottedTok) oexp.op2, lastField);
                }
            }
        }
    }

    private Tok.FieldTok addFieldHints(Tok.DottedTok dexp) {
        DStructType currentType = structType;
        Tok.FieldTok lastField = null;
        for (Tok.DToken tok : dexp.chainL) {
            if (tok instanceof Tok.FieldTok) {
                Tok.FieldTok fieldTok = (Tok.FieldTok) tok;
                allFields.add(fieldTok);

                if (addHints) {
                    lastField = fieldTok;
                    fieldTok.ownerType = currentType;

                    TypePair pair = DValueHelper.findField(currentType, fieldTok.fieldName);
                    if (pair == null) {
//                        DeliaExceptionHelper.throwNotImplementedError("sdf444");
                        String msg = String.format("select. Type '%s' does not have a field named '%s'", currentType.getName(), fieldTok.fieldName);
                        DeliaExceptionHelper.throwError("unknown-field", msg);
                    } else if (pair.type.isStructShape()) {
                        currentType = (DStructType) pair.type;
                    }
                }
            }
        }
        return lastField;
    }

    private void addValueHints(Tok.DottedTok dexp, Tok.FieldTok lastField) {
        if (! addHints) return;
        for (Tok.DToken tok : dexp.chainL) {
            if (tok instanceof Tok.ValueTok) {
                Tok.ValueTok vexp = (Tok.ValueTok) tok;
                vexp.hintPair = DValueHelper.findField(lastField.ownerType, lastField.fieldName);
            }
        }
    }
}
