package org.delia.tok;

import org.delia.type.DStructType;
import org.delia.type.TypePair;
import org.delia.util.DValueHelper;

/**
 * Detects the type of fields mentioned in whereExp
 * eg. Customer[birthDate < '2019']  we need to know that '2019' is a date, not a string
 */
public class MyTokHintVisitor implements Tok.TokVisitor {
    public TypePair pkpair;
    public Tok.OperandTok top;
    public Tok.TokBase prev;
    public DStructType structType;

    @Override
    public void visit(Tok.TokBase exp, Tok.TokBase parent) {
        if (exp instanceof Tok.ValueTok) {
            Tok.ValueTok vexp = (Tok.ValueTok) exp;
            if ((prev == top) && pkpair != null) {
                if (vexp.strValue().equals("true")) {
                    return;
                }
                vexp.hintPair = pkpair;
            }
        } else if (exp instanceof Tok.OperatorTok) {
            Tok.OperatorTok oexp = (Tok.OperatorTok) exp;
            //this handles x < 55.
            //TODO add support for subfields and functions
            String fieldName1 = getPossibleFieldName(oexp.op1);
            String fieldName2 = getPossibleFieldName(oexp.op2);
            if (fieldName1 != null) {
                addHint(oexp.op2, fieldName1);
            } else {
                addHint(oexp.op1, fieldName2);
            }
        }
        prev = exp;
    }

    private String getPossibleFieldName(Tok.OperandTok exp) {
        return TokVisitorUtils.getPossibleFieldName(exp);
    }

    private void addHint(Tok.OperandTok exp, String fieldName) {
        if (exp instanceof Tok.ValueTok) {
            Tok.ValueTok vexp = (Tok.ValueTok) exp;
            vexp.hintPair = DValueHelper.findField(structType, fieldName);
        } else if (exp instanceof Tok.DottedTok) {
            Tok.ValueTok vexp = getSingleChainValue(exp);
            if (vexp != null) {
                vexp.hintPair = DValueHelper.findField(structType, fieldName);
            }
        }
    }

    private Tok.ValueTok getSingleChainValue(Tok.OperandTok exp) {
        return TokVisitorUtils.getSingleChainValue(exp);
    }

}
